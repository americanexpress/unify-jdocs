/*
 * Copyright 2020 American Express Travel Related Services Company, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.americanexpress.unify.jdocs;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
 * @author Deepak Arora
 */
public class JDocument implements Document {

  // a map to store the doc models in use. In future we could use an ExpiryMap
  private static Map<String, Document> docModels = new ConcurrentHashMap<>();

  // for each model document, store a map of the constraint string and the corresponding JsonNode
  private static Map<String, JsonNode> docModelPaths = new ConcurrentHashMap<>();

  // for each regular expression pattern, store the compiled pattern
  private static Map<String, Pattern> compiledPatterns = new ConcurrentHashMap<>();

  // type of the document
  private String type = "";

  // logger
  private static Logger logger = LogManager.getLogger(JDocument.class);

  // root json node of the document
  protected JsonNode rootNode = null;

  // one and only one object mapper -> object mappers are thread safe!!!
  protected static ObjectMapper objectMapper = new ObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true);

  public JDocument() {
    try {
      rootNode = objectMapper.readTree("{}");
    }
    catch (IOException ex) {
      throw new UnifyException("jdoc_err_1", ex);
    }
  }

  public JDocument(String json) {
    try {
      rootNode = objectMapper.readTree(json);
    }
    catch (IOException ex) {
      throw new UnifyException("jdoc_err_1", ex);
    }
  }

  public JDocument(String type, String json) {
    if ((type == null) || (type.isEmpty())) {
      throw new UnifyException("jdoc_err_56");
    }

    try {
      this.type = type;
      if (json == null) {
        rootNode = objectMapper.readTree("{}");
      }
      else {
        rootNode = objectMapper.readTree(json);
      }
      validate(type);
    }
    catch (IOException ex) {
      throw new UnifyException("jdoc_err_1", ex);
    }
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public DataType getLeafNodeDataType(String path, String... vargs) {
    if (isTyped() == false) {
      throw new UnifyException("jdoc_err_60");
    }
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    String format = getFieldFormat(path, modelPath, false);
    JsonNode node = getFormatNode(type, path, format);
    String type = node.get(CONSTS_JDOCS.FORMAT_FIELDS.TYPE).asText();
    if (type == null) {
      throw new UnifyException("jdoc_err_61", modelPath);
    }
    return DataType.valueOf(type.toUpperCase());
  }

  @Override
  public DataType getArrayValueLeafNodeDataType(String path, String... vargs) {
    if (isTyped() == false) {
      throw new UnifyException("jdoc_err_60");
    }
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    String format = getFieldFormat(path, modelPath, true);
    JsonNode node = getFormatNode(type, path, format);
    String type = node.get(CONSTS_JDOCS.FORMAT_FIELDS.TYPE).asText();
    if (type == null) {
      throw new UnifyException("jdoc_err_61", modelPath);
    }
    return DataType.valueOf(type.toUpperCase());
  }

  @Override
  public boolean isTyped() {
    if (type.isEmpty()) {
      return false;
    }
    else {
      return true;
    }
  }

  @Override
  public void setType(String type) {
    if ((type == null) || (type.isEmpty())) {
      type = "";
    }
    else {
      // validate the contents here
      validate(type);
      this.type = type;
    }
  }

  // Base document methods

  /**
   *
   */
  @Override
  public void empty() {
    try {
      rootNode = objectMapper.readTree("{}");
    }
    catch (IOException ex) {
      throw new UnifyException("jdoc_err_1", ex);
    }
  }

  private final void validate(String type) {
    // function to validate the contents of the whole document
    Document md = docModels.get(type);
    List<String> errorList = validate(((JDocument)md).rootNode, rootNode, "$.", type);
    processErrors(errorList);
  }

  private static JsonNode getMatchingArrayElementByField(ArrayNode node, String field, String value) {
    JsonNode matchedNode = null;

    int size = node.size();
    for (int i = 0; i < size; i++) {
      JsonNode elementNode = node.get(i);
      JsonNode fieldNode = elementNode.get(field);
      if (fieldNode != null) {
        String fieldValue = fieldNode.asText();
        if (fieldValue.equals(value)) {
          matchedNode = elementNode;
          break;
        }
      }
    }

    return matchedNode;
  }

  private static int getMatchingArrayElementIndex(ArrayNode node, String field, String value) {
    JsonNode matchedNode = null;

    int size = node.size();
    int index = -1;
    for (int i = 0; i < size; i++) {
      JsonNode elementNode = node.get(i);
      JsonNode fieldNode = elementNode.get(field);
      if (fieldNode != null) {
        String fieldValue = fieldNode.asText();
        if (fieldValue.equals(value)) {
          index = i;
          break;
        }
      }
    }

    return index;
  }

  @Override
  public void merge(Document d, List<String> pathsToDelete) {
    if (isTyped()) {
      JDocument td = (JDocument)d;
      if (type.equals(td.type) == false) {
        throw new UnifyException("jdoc_err_55");
      }

      // first delete the paths
      if (pathsToDelete != null) {
        pathsToDelete.stream().forEach(s -> deletePath(s));
      }

      // now merge
      JsonNode modelNode = null;
      JDocument bd = (JDocument)getDocumentModel(td.getType());
      modelNode = bd.rootNode;
      merge(rootNode, ((JDocument)d).rootNode, modelNode);
    }
    else {
      throw new UnifyException("jdoc_err_2");
    }
  }

  protected boolean pathExists(String path, List<Token> tokenList) {
    boolean exists = false;
    while (true) {
      if (tokenList.isEmpty()) {
        break;
      }

      JsonNode node = traverse(rootNode, tokenList, false);

      if (node == null) {
        break;
      }

      exists = true;
      break;
    }

    return exists;
  }

  /**
   * @param path
   * @return
   * @throws UnifyException
   */
  @Override
  public boolean pathExists(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.PATH_EXISTS, PathAccessType.OBJECT, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      checkPathExistsInModel(getModelPath(path));
    }
    return pathExists(path, tokenList);
  }

  /**
   * @param path
   * @return
   */
  protected JsonNode getJsonNode(String path) {
    List<Token> tokenList = parse(path);
    JsonNode node = traverse(rootNode, tokenList, false);
    return node;
  }

  protected int getArrayIndex(String path, List<Token> tokenList) {
    // check that the last token is an array of name value type
    Token lastToken = null;
    if (tokenList.size() == 1) {
      lastToken = tokenList.get(0);
    }
    else {
      lastToken = tokenList.get(tokenList.size() - 1);
    }
    if (lastToken.isArray() == false) {
      throw new UnifyException("jdoc_err_3");
    }
    if (((ArrayToken)lastToken).getFilter().getType() != ArrayToken.FilterType.NAME_VALUE) {
      throw new UnifyException("jdoc_err_4");
    }

    // start traversal here
    JsonNode parentNode = null;
    if (tokenList.size() == 1) {
      parentNode = rootNode;
    }
    else {
      // get the node till the last but one token
      List<Token> tokens = tokenList.subList(0, tokenList.size() - 1);
      parentNode = traverse(rootNode, tokens, false);
    }

    // handle the last token here
    if (parentNode == null) {
      return -1;
    }

    // find the parent now that has the same field name
    JsonNode node = parentNode.get(lastToken.getField());

    if (node == null) {
      return -1;
    }

    if (node.getNodeType() != JsonNodeType.ARRAY) {
      throw new UnifyException("jdoc_err_5", path);
    }

    ArrayNode arrayNode = (ArrayNode)node;

    // here look the the value in the elements
    int ret = -1;
    int index = -1;
    Iterator<JsonNode> iter = arrayNode.elements();
    while (iter.hasNext()) {
      index++;
      node = iter.next();
      JsonNode node1 = node.get(((ArrayToken)lastToken).getFilter().getField());
      if (node1 != null) {
        // compare value here
        String value = node1.asText();
        if (value.equals(((ArrayToken)lastToken).getFilter().getValue())) {
          ret = index;
          break;
        }
      }
    }

    return ret;
  }

  @Override
  public int getArrayIndex(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_INDEX, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      checkPathExistsInModel(getModelPath(path));
    }
    return getArrayIndex(path, tokenList);
  }

  protected int getArraySize(String path, List<Token> tokenList) {
    JsonNode node = traverse(rootNode, tokenList, false);

    if (node == null) {
      // we do not throw an exception here as it may be a valid path but not present
      return 0;
    }

    if (node.getNodeType() != JsonNodeType.ARRAY) {
      throw new UnifyException("jdoc_err_6", path);
    }

    return ((ArrayNode)node).size();
  }

  /**
   * @param path
   * @return
   * @throws UnifyException
   */
  @Override
  public int getArraySize(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_SIZE, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      checkPathExistsInModel(getModelPath(path));
    }
    return getArraySize(path, tokenList);
  }

  @Override
  public String getJson() {
    String s = null;

    try {
      s = objectMapper.writeValueAsString(rootNode);
    }
    catch (JsonProcessingException ex) {
      throw new UnifyException("jdoc_err_1", ex);
    }
    return s;
  }

  @Override
  public String getPrettyPrintJson() {
    String s = null;

    try {
      s = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
    }
    catch (JsonProcessingException ex) {
      throw new UnifyException("jdoc_err_1", ex);
    }

    return s;
  }

  private static JsonNode traverseObject(JsonNode node, Token token, boolean createNode) {
    JsonNode retNode = null;
    JsonNode objectNode = node.get(token.getField());

    while (true) {
      if (objectNode == null) {
        if (createNode) {
          retNode = ((ObjectNode)node).putObject(token.getField());
          break;
        }

        retNode = null;
        break;
      }

      retNode = objectNode;
      break;
    }

    return retNode;
  }

  private static JsonNode traverseArrayEmpty(JsonNode node, ArrayToken token, boolean createNode) {
    JsonNode retNode = null;
    JsonNode arrayNode = node.get(token.getField());

    while (true) {
      if (arrayNode == null) {
        if (createNode) {
          retNode = ((ObjectNode)node).putArray(token.getField());
          break;
        }

        node = null;
        break;
      }

      // check that it is indeed an array
      if (arrayNode.isArray() == false) {
        throw new UnifyException("jdoc_err_7" + token.getField());
      }

      retNode = arrayNode;
      break;
    }

    return retNode;
  }

  private static JsonNode traverseArrayIndex(JsonNode node, ArrayToken token, boolean createNode) {
    JsonNode retNode = null;
    int index = token.getFilter().getIndex();

    while (true) {
      JsonNode arrayNode = traverseArrayEmpty(node, token, createNode);

      if (arrayNode == null) {
        retNode = null;
        break;
      }

      // at this point we have a valid arrayNode. Now we check for index out of bounds
      if (createNode == true) {
        if (index > arrayNode.size()) {
          throw new UnifyException("jdoc_err_8", token.getField());
        }
      }
      else {
        if (index >= arrayNode.size()) {
          throw new UnifyException("jdoc_err_8", token.getField());
        }
      }

      // now we get the index element node
      JsonNode indexNode = arrayNode.get(index);

      // check if it exists
      if (indexNode == null) {
        if (createNode) {
          retNode = ((ArrayNode)arrayNode).addObject();
          break;
        }

        retNode = null;
        break;
      }

      // at this point we have a valid index node
      retNode = indexNode;
      break;
    }

    return retNode;
  }

  private static JsonNode traverseArrayNameValue(JsonNode node, ArrayToken token, boolean createNode) {
    JsonNode retNode = null;
    JsonNode arrayNode = traverseArrayEmpty(node, token, createNode);

    while (true) {
      if (arrayNode == null) {
        retNode = null;
        break;
      }

      // we need to go in and fetch the element that matches the value from arrayNode
      JsonNode objectNode = getMatchingArrayElementByField((ArrayNode)arrayNode, token.getFilter().getField(), token.getFilter().getValue());

      if (objectNode == null) {
        if (createNode) {
          objectNode = ((ArrayNode)arrayNode).addObject();
          retNode = ((ObjectNode)objectNode).put(token.getFilter().getField(), token.getFilter().getValue());
          break;
        }

        retNode = null;
        break;
      }

      retNode = objectNode;
      break;
    }

    return retNode;
  }

  private static JsonNode traverse(JsonNode rootNode, List<Token> tokenList, boolean createNode) {
    JsonNode node = rootNode;

    for (Token token : tokenList) {
      while (true) {
        // handle object node
        if (token.isArray() == false) {
          node = traverseObject(node, token, createNode);
          break;
        }

        // handle empty [] array node
        ArrayToken arrayToken = (ArrayToken)token;
        if (arrayToken.getFilter().getType() == ArrayToken.FilterType.EMPTY) {
          node = traverseArrayEmpty(node, arrayToken, createNode);
          break;
        }

        // handle index node
        if (arrayToken.getFilter().getType() == ArrayToken.FilterType.INDEX) {
          node = traverseArrayIndex(node, arrayToken, createNode);
          break;
        }

        // handle name value array node
        if (arrayToken.getFilter().getType() == ArrayToken.FilterType.NAME_VALUE) {
          node = traverseArrayNameValue(node, arrayToken, createNode);
          break;
        }

        throw new UnifyException("jdoc_err_9", arrayToken.getFilter().getType().name());
      }

      if (node == null) {
        break;
      }
    }

    return node;
  }

  private static boolean isArrayTokenDefinite(ArrayToken arrayToken) {
    boolean isDefinite = false;

    while (true) {
      ArrayToken.Filter filter = arrayToken.getFilter();

      if (filter.getType() == ArrayToken.FilterType.EMPTY) {
        isDefinite = false;
        break;
      }

      if (filter.getType() == ArrayToken.FilterType.INDEX) {
        isDefinite = true;
        break;
      }

      if (filter.getType() == ArrayToken.FilterType.NAME_VALUE) {
        isDefinite = true;
        break;
      }

      throw new UnifyException("jdoc_err_9", filter.getType().name());
    }

    return isDefinite;
  }

  private void validatePath1(String path, CONSTS_JDOCS.API api, List<Token> tokenList, PathAccessType pat) {

    int size = tokenList.size();

    // check that a non leaf token cannot be indefinite
    for (Token token : tokenList) {
      if (token.isArray()) {
        boolean isDefinite = isArrayTokenDefinite((ArrayToken)token);
        if ((isDefinite == false) && (token.isLeaf() == false)) {
          throw new UnifyException("jdoc_err_11", token.getField());
        }
      }
    }

    while (true) {
      if (pat == PathAccessType.VALUE) {
        // leaf node checking
        Token token = tokenList.get(size - 1);
        switch (api) {
          case GET_ARRAY_SIZE:
            // leaf token needs to be an array and empty i.e. last two characters should be []
            if (token.isArray() == false) {
              throw new UnifyException("jdoc_err_5", path);
            }

            if (((ArrayToken)token).getFilter().getType() != ArrayToken.FilterType.EMPTY) {
              throw new UnifyException("jdoc_err_47", path);
            }
            break;

          case GET_ARRAY_INDEX:
            // leaf token needs to be an array and name value type
            if (token.isArray() == false) {
              throw new UnifyException("jdoc_err_5", path);
            }

            if (((ArrayToken)token).getFilter().getType() != ArrayToken.FilterType.NAME_VALUE) {
              throw new UnifyException("jdoc_err_48", path);
            }
            break;

          case GET:
          case SET:
            // leaf token cannot be an array
            if (token.isArray()) {
              throw new UnifyException("jdoc_err_49", path);
            }
            break;

          case GET_ARRAY_VALUE:
          case SET_ARRAY_VALUE:
            // leaf token has to be a definite index array
            if (token.isArray() == false) {
              throw new UnifyException("jdoc_err_5", path);
            }

            if (((ArrayToken)token).getFilter().getType() != ArrayToken.FilterType.INDEX) {
              throw new UnifyException("jdoc_err_50", path);
            }
            break;

          default:
            break;
        }

        break;
      }

      if (pat == PathAccessType.OBJECT) {
        // leaf node checking -> nothing to do at present
        break;
      }

      // throw exception
      throw new UnifyException("jdoc_err_12", pat.name());
    }
  }

  protected Object getValue(String path, Class clazz, List<Token> tokenList) {
    JsonNode node = null;
    Object value = null;

    while (true) {
      node = traverse(rootNode, tokenList, false);

      if (node == null) {
        value = null;
        break;
      }

      switch (node.getNodeType()) {
        case NUMBER:
          if (clazz == null) {
            if (node.isInt()) {
              value = node.asInt();
              break;
            }

            if (node.isLong()) {
              value = node.asLong();
              break;
            }

            if (node.isDouble()) {
              value = node.decimalValue();
              break;
            }

            throw new UnifyException("jdoc_err_13", path);
          }

          if (clazz == Integer.class) {
            value = node.asInt();
            break;
          }

          if (clazz == Long.class) {
            value = node.asLong();
            break;
          }

          if (clazz == BigDecimal.class) {
            value = node.decimalValue();
            break;
          }

          throw new UnifyException("jdoc_err_13", path);

        case STRING:
          if (clazz == null) {
            value = node.asText();
            break;
          }

          if (clazz == String.class) {
            value = node.asText();
            break;
          }

          throw new UnifyException("jdoc_err_13", path);

        case BOOLEAN:
          if (clazz == null) {
            value = node.asBoolean();
            break;
          }

          if (clazz == Boolean.class) {
            value = node.asBoolean();
            break;
          }

          throw new UnifyException("jdoc_err_13", path);

        case NULL:
          value = null;
          break;

        default:
          throw new UnifyException("jdoc_err_14", path);
      }

      break;
    }

    return value;
  }

  private void setLeafNode(ObjectNode node, String field, Object value) {

    if (value instanceof String) {
      node.put(field, (String)value);
    }
    else if (value instanceof Integer) {
      node.put(field, (Integer)value);
    }
    else if (value instanceof Long) {
      node.put(field, (Long)value);
    }
    else if (value instanceof BigDecimal) {
      node.put(field, (BigDecimal)value);
    }
    else if (value instanceof Boolean) {
      node.put(field, (Boolean)value);
    }
    else {
      throw new UnifyException("jdoc_err_15", value.getClass().getCanonicalName());
    }

  }

  private void setArrayIndexValue(ArrayNode node, int index, Object value) {

    int size = node.size();

    while (true) {

      if (index < size) {
        // remove and insert
        node.remove(index);

        if (value instanceof String) {
          node.insert(index, (String)value);
        }
        else if (value instanceof Integer) {
          node.insert(index, (Integer)value);
        }
        else if (value instanceof Long) {
          node.insert(index, (Long)value);
        }
        else if (value instanceof BigDecimal) {
          node.insert(index, (BigDecimal)value);
        }
        else if (value instanceof Boolean) {
          node.insert(index, (Boolean)value);
        }
        else {
          throw new UnifyException("jdoc_err_15", value.getClass().getCanonicalName());
        }

        break;
      }

      if (index == size) {
        // add at the end
        if (value instanceof String) {
          node.add((String)value);
        }
        else if (value instanceof Integer) {
          node.add((Integer)value);
        }
        else if (value instanceof Long) {
          node.add((Long)value);
        }
        else if (value instanceof BigDecimal) {
          node.add((BigDecimal)value);
        }
        else if (value instanceof Boolean) {
          node.add((Boolean)value);
        }
        else {
          throw new UnifyException("jdoc_err_15", value.getClass().getCanonicalName());
        }

        break;
      }

      // if we reach here then it is an invalid index
      throw new UnifyException("jdoc_err_16");
    }

  }

  private JsonNode setObjectNode(ObjectNode node, String field) {
    JsonNode node1 = node.get(field);

    if (node1 == null) {
      node1 = node.putObject(field);
    }

    return node1;
  }

  private JsonNode setArrayNode(ObjectNode node, String field) {
    JsonNode node1 = node.get(field);

    if (node1 == null) {
      node1 = node.putArray(field);
    }

    return node1;
  }

  private JsonNode setArrayIndexNode(ArrayNode arrayNode, ArrayToken token, String path, String tokenPath) {
    JsonNode filterNode = null;
    boolean found = false;
    String filterField = token.getFilter().getField();
    String filterValue = token.getFilter().getValue();
    int index = token.getFilter().getIndex();

    if (token.getFilter().getType() == ArrayToken.FilterType.INDEX) {
      int size = arrayNode.size();
      if (index > size) {
        throw new UnifyException("jdoc_err_17", token.getField());
      }

      if (token.isLeaf()) {
        filterNode = arrayNode;
      }
      else {
        if (index == size) {
          // we need to create the object
          filterNode = arrayNode.addObject();
        }

        // we need to access an existing object
        filterNode = arrayNode.get(index);
      }
    }
    else {
      // iterate through the array node to identify the object / index we need to replace
      Iterator<JsonNode> iter = arrayNode.iterator();

      outer:
      while (iter.hasNext()) {
        // get filter node -> we should always find it as this will be the primary key on which we store objects in the array
        filterNode = iter.next();
        JsonNode fieldNode = filterNode.get(filterField);

        if (fieldNode == null) {
          continue;
        }

        switch (fieldNode.getNodeType()) {
          case BOOLEAN:
            boolean boolValue = fieldNode.asBoolean();
            if (boolValue == Boolean.valueOf(filterValue)) {
              found = true;
              break outer;
            }
            break;

          case NUMBER:
            if (fieldNode.isInt()) {
              int intValue = fieldNode.asInt();
              if (intValue == Integer.valueOf(filterValue)) {
                found = true;
                break outer;
              }
            }
            else if (fieldNode.isLong()) {
              long longValue = fieldNode.asLong();
              if (longValue == Long.valueOf(filterValue)) {
                found = true;
                break outer;
              }
            }
            else if (fieldNode.isDouble()) {
              BigDecimal bigDecimalValue = fieldNode.decimalValue();
              if (bigDecimalValue == new BigDecimal(filterValue)) {
                found = true;
                break outer;
              }
            }
            else {
              throw new UnifyException("jdoc_err_45", filterField, fieldNode.toString(), filterValue);
            }

            break;

          case STRING:
            String docValue = fieldNode.asText();
            if (docValue.equals(filterValue)) {
              found = true;
              break outer;
            }
            break;

          default:
            throw new UnifyException("jdoc_err_18", filterField);
        }
      }

      if (found == false) {
        filterNode = arrayNode.addObject();
        if (isTyped()) {
          // we need to create the appropriate type of the node and for this we need to get the data type from the model
          String modelPath = tokenPath + "." + filterField;
          setFilterFieldNode((ObjectNode)filterNode, filterField, filterValue, path, modelPath);
        }
        else {
          ((ObjectNode)filterNode).put(filterField, filterValue);
        }

      }
    }
    return filterNode;
  }

  protected final void setValue(String path, List<Token> tokenList, Object value) {
    JsonNode node = rootNode;
    String tokenPath = "$";

    if (value == null) {
      throw new UnifyException("jdoc_err_20", path);
    }

    // traverse the document. If we find a node corresponding to the path token and it matches the type
    // i.e. array or object or value node we go inside
    // if we do not find the token in the document, we create it and move inside
    // we do this till we reach the leaf token at which point of time we set the value
    for (Token token : tokenList) {
      tokenPath = tokenPath + "." + token.getField();

      while (true) {
        String field = token.getField();

        // do array handling
        if (token.isArray()) {
          tokenPath = tokenPath + "[0]";

          // first get / set the array node under which we need to search for the filter field
          node = setArrayNode((ObjectNode)node, field);

          // get / set the node at which we need to make the change
          node = setArrayIndexNode((ArrayNode)node, (ArrayToken)token, path, tokenPath);

          if (token.isLeaf() == false) {
            break;
          }
        }

        // leaf handling
        if (token.isLeaf()) {
          if (token.isArray()) {
            // set the value in the array
            ArrayToken at = (ArrayToken)token;
            setArrayIndexValue((ArrayNode)node, at.getFilter().getIndex(), value);
          }
          else {
            setLeafNode((ObjectNode)node, field, value);
          }

          break;
        }

        // node is an object. Move inside creating it if it does not
        node = setObjectNode((ObjectNode)node, field);
        break;
      }
    }

  }

  protected String getStaticPath(String path, String... vargs) {
    if (vargs.length == 0) {
      return path;
    }

    int size = path.length();
    StringBuffer sb = new StringBuffer();
    int counter = 0;

    for (int i = 0; i < size; i++) {
      char c = path.charAt(i);

      if (c == '%') {
        if (i == 0) {
          sb.append(BaseUtils.escapeChars(vargs[counter++], '\\', '.', '[', ']', '='));
        }
        else {
          if (sb.charAt(i - 1) == '\\') {
            sb.append(c);
          }
          else {
            sb.append(BaseUtils.escapeChars(vargs[counter++], '\\', '.', '[', ']', '='));
          }
        }
      }
      else {
        sb.append(c);
      }
    }

    return sb.toString();
  }

  protected List<Token> validatePath(String path, CONSTS_JDOCS.API api, PathAccessType pat, String... vargs) {
    List<Token> tokenList = parse(path);
    validatePath1(path, api, tokenList, pat);
    return tokenList;
  }

  @Override
  public Object getValue(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      checkPathExistsInModel(getModelPath(path));
    }
    return getValue(path, null, tokenList);
  }

  @Override
  public String getString(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      checkPathExistsInModel(getModelPath(path));
    }
    return (String)getValue(path, String.class, tokenList);
  }

  @Override
  public Integer getInteger(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      checkPathExistsInModel(getModelPath(path));
    }
    return (Integer)getValue(path, Integer.class, tokenList);
  }

  @Override
  public Boolean getBoolean(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      checkPathExistsInModel(getModelPath(path));
    }
    return (Boolean)getValue(path, Boolean.class, tokenList);
  }

  @Override
  public Long getLong(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      checkPathExistsInModel(getModelPath(path));
    }
    return (Long)getValue(path, Long.class, tokenList);
  }

  @Override
  public BigDecimal getBigDecimal(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      checkPathExistsInModel(getModelPath(path));
    }
    return (BigDecimal)getValue(path, BigDecimal.class, tokenList);
  }

  @Override
  public String getArrayValueString(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      checkPathExistsInModel(getModelPath(path));
    }
    return (String)getValue(path, String.class, tokenList);
  }

  @Override
  public Integer getArrayValueInteger(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      checkPathExistsInModel(getModelPath(path));
    }
    return (Integer)getValue(path, Integer.class, tokenList);
  }

  @Override
  public Boolean getArrayValueBoolean(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      checkPathExistsInModel(getModelPath(path));
    }
    return (Boolean)getValue(path, Boolean.class, tokenList);
  }

  @Override
  public Long getArrayValueLong(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      checkPathExistsInModel(getModelPath(path));
    }
    return (Long)getValue(path, Long.class, tokenList);
  }

  @Override
  public BigDecimal getArrayValueBigDecimal(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      checkPathExistsInModel(getModelPath(path));
    }
    return (BigDecimal)getValue(path, BigDecimal.class, tokenList);
  }

  @Override
  public void setString(String path, String value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      validateField(path, value);
    }
    setValue(path, tokenList, value);
  }

  @Override
  public void setInteger(String path, int value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      validateField(path, value);
    }
    setValue(path, tokenList, value);
  }

  @Override
  public void setBoolean(String path, boolean value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      validateField(path, value);
    }
    setValue(path, tokenList, value);
  }

  @Override
  public void setLong(String path, long value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      validateField(path, value);
    }
    setValue(path, tokenList, value);
  }

  @Override
  public void setBigDecimal(String path, BigDecimal value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      validateField(path, value);
    }
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueString(String path, String value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      validateField(path, value, true);
    }
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueInteger(String path, int value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      validateField(path, value, true);
    }
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueBoolean(String path, boolean value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      validateField(path, value, true);
    }
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueLong(String path, long value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      validateField(path, value, true);
    }
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueBigDecimal(String path, BigDecimal value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      validateField(path, value, true);
    }
    setValue(path, tokenList, value);
  }

  @Override
  public void setContent(Document fromDoc, String fromPath, String toPath, String... vargs) {
    if (vargs.length > 0) {
      int count = BaseUtils.getCount(fromPath, '%');
      fromPath = getStaticPath(fromPath, vargs);
      String[] vargs1 = new String[vargs.length - count];
      for (int i = 0; i < vargs1.length; i++) {
        vargs1[i] = vargs[count];
      }
      toPath = getStaticPath(toPath, vargs1);
    }

    if (isTyped()) {
      validate(fromDoc, fromPath, toPath);
    }

    // this function copies the content from document to another document
    // if the value does not exist, nothing is done
    while (true) {
      // from node handling
      JsonNodeType fromNodeType = null;

      List<Token> tokenList = parse(fromPath);
      validatePath1(fromPath, CONSTS_JDOCS.API.CONTENT, tokenList, PathAccessType.OBJECT);

      JsonNode fromNode = traverse(((JDocument)fromDoc).rootNode, tokenList, false);
      if (fromNode == null) {
        throw new UnifyException("jdoc_err_21", fromPath);
      }
      fromNodeType = fromNode.getNodeType();
      if ((fromNodeType != JsonNodeType.ARRAY) && (fromNodeType != JsonNodeType.OBJECT)) {
        throw new UnifyException("jdoc_err_22", fromPath);
      }
      fromNode = fromNode.deepCopy();

      // to node handling
      JsonNodeType toNodeType = null;

      tokenList = parse(toPath);
      validatePath1(toPath, CONSTS_JDOCS.API.CONTENT, tokenList, PathAccessType.OBJECT);

      JsonNode toNode = traverse(rootNode, tokenList, true);
      toNodeType = toNode.getNodeType();
      if ((toNodeType != JsonNodeType.ARRAY) && (toNodeType != JsonNodeType.OBJECT)) {
        throw new UnifyException("jdoc_err_23", fromPath);
      }

      if (fromNodeType.equals(toNodeType) == false) {
        throw new UnifyException("jdoc_err_24");
      }

      if (fromNodeType == JsonNodeType.OBJECT) {
        ((ObjectNode)toNode).setAll((ObjectNode)fromNode);
      }
      else {
        ((ArrayNode)toNode).removeAll();
        ((ArrayNode)toNode).addAll((ArrayNode)fromNode);
      }
      break;
    }
  }

  @Override
  public synchronized Document deepCopy() {
    JDocument d = new JDocument();
    d.rootNode = rootNode.deepCopy();
    d.type = type;
    return d;
  }

  protected void deletePath(String path, List<Token> tokenList) {
    JsonNode node = null;
    while (true) {
      if (tokenList.isEmpty()) {
        // we need to empty out the document
        empty();
        break;
      }

      // get the last token
      Token token = tokenList.get(tokenList.size() - 1);

      // remove last token from token list
      tokenList.remove(tokenList.size() - 1);

      node = traverse(rootNode, tokenList, false);

      // token can be a Token or an ArrayToken
      if (token.isArray()) {
        JsonNode leafNode = null;
        ArrayToken arrayToken = (ArrayToken)token;

        leafNode = node.get(token.getField());
        if (leafNode != null) {
          if (leafNode.getNodeType() != JsonNodeType.ARRAY) {
            throw new UnifyException("jdoc_err_25", path);
          }

          int index = -1;

          switch (arrayToken.getFilter().getType()) {
            case EMPTY:
              ((ObjectNode)node).remove(token.getField());
              break;

            case INDEX:
              // check if the index is valid
              index = arrayToken.getFilter().getIndex();
              if (index >= ((ArrayNode)leafNode).size()) {
                throw new UnifyException("jdoc_err_17", token.getField());
              }

              ((ArrayNode)leafNode).remove(arrayToken.getFilter().getIndex());
              if (((ArrayNode)leafNode).size() == 0) {
                // remove the field itself
                ((ObjectNode)node).remove(token.getField());
              }
              break;

            case NAME_VALUE:
              index = getMatchingArrayElementIndex((ArrayNode)leafNode, arrayToken.getFilter().getField(), arrayToken.getFilter().getValue());
              if (index >= 0) {
                ((ArrayNode)leafNode).remove(index);
              }

              if (((ArrayNode)leafNode).size() == 0) {
                // remove the field itself
                ((ObjectNode)node).remove(token.getField());
              }
              break;

            default:
              throw new UnifyException("jdoc_err_26");
          }
        }

        break;
      }

      if (token.isArray() == false) {
        // do field handling handling
        JsonNode leafNode = node.get(token.getField());
        if (leafNode != null) {
          ((ObjectNode)node).remove(token.getField());
        }
        break;
      }

      // throw exception
      throw new UnifyException("jdoc_err_26");
    }
  }


  @Override
  public void deletePath(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.DELETE_PATH, PathAccessType.OBJECT, vargs);
    if (isTyped()) {
      validateFilterNames(path, tokenList);
      checkPathExistsInModel(getModelPath(path));
    }

    deletePath(path, tokenList);
  }

  protected List<Token> parse(String path) {
    return Parser.getTokens(path);
  }

  @Override
  public Document getContent(String path, boolean returnTypedDocument, boolean includeFullPath, String... vargs) {
    Document d = null;

    if (vargs.length > 0) {
      path = getStaticPath(path, vargs);
    }

    while (true) {
      JsonNodeType nodeType = null;
      List<Token> tokenList = parse(path);
      validatePath1(path, CONSTS_JDOCS.API.CONTENT, tokenList, PathAccessType.OBJECT);
      JsonNode node = traverse(this.rootNode, tokenList, false);
      if (node == null) {
        break;
      }

      nodeType = node.getNodeType();
      if ((nodeType != JsonNodeType.ARRAY) && (nodeType != JsonNodeType.OBJECT)) {
        throw new UnifyException("jdoc_err_22", path);
      }

      if (isTyped() && (returnTypedDocument == true)) {
        d = new JDocument(type, null);
      }
      else {
        d = new JDocument();
      }

      String toPath = "$";
      if (includeFullPath == true) {
        toPath = getToContentPath(tokenList);
      }
      d.setContent(this, path, toPath);
      break;
    }

    return d;
  }

  private String getToContentPath(List<Token> tokens) {
    String s = "$";
    for (Token t : tokens) {
      if (t.isArray()) {
        ArrayToken at = (ArrayToken)t;
        s += "." + at.getField() + "[";

        ArrayToken.FilterType ft = at.getFilter().getType();
        if (ft == ArrayToken.FilterType.EMPTY) {
          s += "]";
        }
        else if (ft == ArrayToken.FilterType.INDEX) {
          s += "0]";
        }
        else if (ft == ArrayToken.FilterType.NAME_VALUE) {
          s += "0]";
        }
      }
      else {
        s += "." + t.getField();
      }
    }
    return s;
  }

  // typed document methods

  public static void loadDocumentModel(String type, String json) {
    try {
      json = insertReferredModels(json);
    }
    catch (IOException ex) {
      logger.error("IO exception encountered for type {}, error message -> {}", type, ex.getMessage());
      System.exit(-1);
    }

    Document d = new JDocument(json);
    setDocumentModel(type, d);
    logger.info("Successfully loaded document model -> " + type);
  }

  public static void setDocumentModel(String type, Document model) {
    docModels.put(type, model);
  }

  public static void close() {
    if (docModels != null) {
      docModels = null;
      logger.info("Successfully unloaded document models");
    }
    else {
      logger.info("Document models have already been unloaded");
    }
  }

  private static Document getDocumentModel(String type) {
    return docModels.get(type);
  }

  private static String insertReferredModels(String json) throws IOException {
    while (true) {
      StringBuilder sb = new StringBuilder(1024);

      try (Scanner scanner = new Scanner(json)) {
        boolean isReferred = false;

        while (scanner.hasNextLine()) {
          String line = scanner.nextLine().trim();
          int indexPattern = line.indexOf("\"@here\"");

          if (indexPattern == 0) {
            isReferred = true;

            String tokens[] = line.split(":");
            String rfn = tokens[1].trim();
            int indexQuote = rfn.lastIndexOf('"');
            rfn = rfn.substring(1, indexQuote);
            String contents = BaseUtils.getResourceAsString(JDocument.class, rfn).trim();
            sb.append(contents.substring(1, contents.length() - 1));

            if (line.charAt(line.length() - 1) == ',') {
              sb.append(',');
              sb.append(CONSTS_JDOCS.NEW_LINE);
            }
          }
          else {
            sb.append(line);
            sb.append(CONSTS_JDOCS.NEW_LINE);
          }
        }

        json = sb.toString();

        if (isReferred == false) {
          break;
        }
      }
    }

    JsonNode node = objectMapper.readTree(json);
    json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    return json;
  }

  private String getFieldFormat(String path, String modelPath, boolean isValueArray) {
    // get the format string from the path
    Document md = docModels.get(type);
    if (md == null) {
      throw new UnifyException("jdoc_err_29", type);
    }

    String format = null;
    if (isValueArray) {
      format = md.getArrayValueString(modelPath);
    }
    else {
      format = md.getString(modelPath);
    }

    if (format == null) {
      throw new UnifyException("jdoc_err_38", type, path);
    }

    return format;
  }

  private void checkPathExistsInModel(String path) {
    Document md = docModels.get(type);
    boolean b = md.pathExists(path);
    if (b == false) {
      throw new UnifyException("jdoc_err_38", type, path);
    }
  }

  private void validateField(String path, Object value) {
    validateField(path, value, false);
  }

  private void validateField(String path, Object value, boolean isValueArray) {
    String modelPath = getModelPath(path);
    String format = getFieldFormat(path, modelPath, isValueArray);
    validateField(format, value, modelPath, type);
  }

  private void processErrors(List<String> errorList) {
    if (errorList.size() > 0) {
      StringBuffer sb = new StringBuffer();
      errorList.stream().forEach(s -> {
        s = s + CONSTS_JDOCS.NEW_LINE;
        sb.append(s);
      });
      throw new UnifyException("jdoc_err_28", sb.toString());
    }
  }

  private String getModelPath(String path) {
    String s = "";

    while (true) {
      int index = path.indexOf('[');
      if (index == -1) {
        s = s + path;
        break;
      }

      s = s + path.substring(0, index + 1) + "0]";

      index = path.indexOf(']');
      path = path.substring(index + 1);
    }

    return s;
  }

  private static void mergeArray(ArrayNode toNode, ArrayNode fromNode, ArrayNode modelNode, String field) {
    // check if it is a array value
    JsonNode node = modelNode.get(0);
    if (node instanceof ValueNode) {
      // there is no key node for such cases
      // we just append the elements
      toNode.addAll(fromNode);
    }
    else {
      // get the key field. If the key field is not defined in the model then we throw an exception
      // this means that key fields are mandatory if we want to use merge functionality
      String keyField = getKeyField(modelNode);
      if (keyField == null) {
        throw new UnifyException("jdoc_err_32", field);
      }

      // for each element of fromNode
      // look for the field corresponding to the key field in toNode
      // if found update that object
      // else add to the end of the array
      int size = fromNode.size();
      for (int i = 0; i < size; i++) {
        JsonNode fromElementNode = fromNode.get(i);
        JsonNode keyNode = fromElementNode.get(keyField);
        if (keyNode == null) {
          throw new UnifyException("jdoc_err_33", field);
        }
        String keyValue = keyNode.asText();
        JsonNode toMatchedNode = getMatchingArrayElementByKey(toNode, keyField, keyValue, field);
        if (toMatchedNode == null) {
          // add to the end of the array
          toNode.add(fromElementNode);
        }
        else {
          // merge into the to element
          merge(toMatchedNode, fromElementNode, modelNode.get(0));
        }
      }
    }
  }

  private static JsonNode getMatchingArrayElementByKey(ArrayNode node, String keyField, String fromKeyValue, String field) {
    JsonNode matchedNode = null;

    int size = node.size();
    for (int i = 0; i < size; i++) {
      JsonNode elementNode = node.get(i);
      JsonNode keyNode = elementNode.get(keyField);
      if (keyNode == null) {
        throw new UnifyException("jdoc_err_34", field);
      }
      String keyValue = keyNode.asText();
      if (keyValue.equals(fromKeyValue)) {
        matchedNode = elementNode;
        break;
      }
    }

    return matchedNode;
  }

  private static String getKeyField(ArrayNode modelNode) {
    String keyField = null;

    JsonNode node = modelNode.get(0).get(CONSTS_JDOCS.FORMAT_FIELDS.KEY);
    if (node != null) {
      String json = node.asText();
      try {
        keyField = objectMapper.readTree(json).get("field").asText();
      }
      catch (IOException ex) {
        throw new UnifyException("jdoc_err_1", ex);
      }
    }

    return keyField;
  }

  private static void merge(JsonNode toNode, JsonNode fromNode, JsonNode modelNode) {
    Iterator<Map.Entry<String, JsonNode>> mergeFromFieldIter = fromNode.fields();

    while (mergeFromFieldIter.hasNext()) {
      // for each field in the fromNode

      // get the from node details
      Map.Entry<String, JsonNode> entry = mergeFromFieldIter.next();
      String field = entry.getKey();
      JsonNode fromFieldNode = entry.getValue();

      // get the model node details
      JsonNode modelFieldNode = modelNode.get(field);

      // now start to node handling
      if (fromFieldNode.getNodeType().equals(JsonNodeType.OBJECT) || fromFieldNode.getNodeType().equals(JsonNodeType.ARRAY)) {
        JsonNode toFieldNode = toNode.get(field);
        if (toFieldNode == null) {
          ((ObjectNode)toNode).replace(field, fromFieldNode);
        }
        else {
          // check that the types should be the same else throw exception
          // this is a safety check but is not expected to happen as both the documents
          // are of the typed document type
          if (fromFieldNode.getNodeType().equals(toFieldNode.getNodeType()) == false) {
            throw new UnifyException("jdoc_err_35", field);
          }

          if (fromFieldNode.getNodeType().equals(JsonNodeType.OBJECT)) {
            merge(toFieldNode, fromFieldNode, modelFieldNode);
          }
          else {
            mergeArray((ArrayNode)toFieldNode, (ArrayNode)fromFieldNode, (ArrayNode)modelFieldNode, field);
          }
        }
      }

      ValueNode valueNode = null;
      switch (fromFieldNode.getNodeType()) {
        case STRING:
          valueNode = new TextNode(fromFieldNode.textValue());
          break;

        case NUMBER:
          if (fromFieldNode.isInt()) {
            valueNode = new IntNode(fromFieldNode.intValue());
          }
          else if (fromFieldNode.isLong()) {
            valueNode = new LongNode(fromFieldNode.longValue());
          }
          else if (fromFieldNode.isDouble()) {
            valueNode = new DecimalNode(fromFieldNode.decimalValue());
          }
          else {
            throw new UnifyException("jdoc_err_43", field);
          }
          break;

        case BOOLEAN:
          valueNode = BooleanNode.valueOf(fromFieldNode.booleanValue());
          break;

        case NULL:
          valueNode = NullNode.getInstance();
          break;

        default:
          // nothing to do
          break;
      }

      if (valueNode != null) {
        updateObject(toNode, valueNode, entry);
      }
    }
  }

  private static void updateObject(JsonNode mergeInTo, ValueNode valueToBePlaced, Map.Entry<String, JsonNode> toBeMerged) {
    boolean newEntry = true;
    Iterator<Map.Entry<String, JsonNode>> mergeIntoIter = mergeInTo.fields();
    while (mergeIntoIter.hasNext()) {
      Map.Entry<String, JsonNode> entry = mergeIntoIter.next();
      if (entry.getKey().equals(toBeMerged.getKey())) {
        newEntry = false;
        entry.setValue(valueToBePlaced);
      }
    }
    if (newEntry) {
      ((ObjectNode)mergeInTo).replace(toBeMerged.getKey(), toBeMerged.getValue());
    }
  }

  private static JsonNode getFormatNode(String type, String path, String format) {
    JsonNode node = docModelPaths.get(format);
    if (node == null) {
      try {
        node = objectMapper.readTree(format);
        docModelPaths.put(format, node);
      }
      catch (IOException e) {
        throw new UnifyException("jdoc_err_63", type, path, format);
      }
    }

    return node;
  }

  private static void validateField(String format, Object value, String path, String type) {
    // "{\"field\":\"field_name\"}"
    // "{\"type\":\"string\", \"regex\":\"\\\\w{17,17}\"}"
    // "{\"type\":\"date\", \"format\":\"yyyy-MM-dd HH:mm:ss.SSS GMT\"}"
    // key is optional
    // type is mandatory
    // format
    //   optional for string, boolean, numeric
    //   mandatory for date
    // max_len is optional
    // by default null value is not allowed. But if it is to be allowed then the property null_allowed should
    // be set to true as shown below
    // "{\"type\":\"string\", \"null_allowed\":true}"

    // get the model path node
    JsonNode node = getFormatNode(type, path, format);

    while (true) {
      // if the value is null, check if nulls are allowed
      if (value == null) {
        JsonNode node1 = node.get(CONSTS_JDOCS.FORMAT_FIELDS.NULL_ALLOWED);
        boolean isNullAllowed = false;
        if (node1 != null) {
          isNullAllowed = node.get(CONSTS_JDOCS.FORMAT_FIELDS.NULL_ALLOWED).booleanValue();
        }
        if (isNullAllowed == false) {
          throw new UnifyException("jdoc_err_36", path);
        }

        break;
      }

      // check data types
      DataType dataType = DataType.valueOf(node.get(CONSTS_JDOCS.FORMAT_FIELDS.TYPE).asText().toUpperCase());
      switch (dataType) {
        case STRING:
          if ((value instanceof String) == false) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        case BOOLEAN:
          if ((value instanceof Boolean) == false) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        case DATE:
          if ((value instanceof String) == false) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        case INTEGER:
          if (((value instanceof Integer) == false)) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        case LONG:
          if (((value instanceof Long) == false)) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        case DECIMAL:
          // Couchbase stores a decimal value of 10.00 as 10 in the json document
          // hence when we read the document and construct the typed document we
          // will need to check against int and long data types as well
          if (((value instanceof BigDecimal) == false) && ((value instanceof Integer) == false) && ((value instanceof Long) == false)) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        default:
          break;
      }

      // check against regex pattern and format
      switch (dataType) {
        case STRING:
        case BOOLEAN:
        case INTEGER:
        case LONG:
        case DECIMAL: {
          String s = value.toString();
          JsonNode node1 = node.get(CONSTS_JDOCS.FORMAT_FIELDS.REGEX);
          if (node1 != null) {
            String regex = node1.asText();
            Pattern pattern = compiledPatterns.get(regex);
            if (pattern == null) {
              pattern = Pattern.compile(regex);
              compiledPatterns.put(regex, pattern);
            }
            Matcher matcher = pattern.matcher(s);
            boolean matches = matcher.matches();
            if (matches == false) {
              throw new UnifyException("jdoc_err_54", path);
            }
          }
          break;
        }

        case DATE:
          // Match input date with the format provided
          String fieldValue = node.get(CONSTS_JDOCS.FORMAT_FIELDS.FORMAT).asText();
          if (fieldValue.isEmpty() == false) {
            try {
              DateTimeFormatter dfs = DateTimeFormatter.ofPattern(fieldValue);
              if (value.toString().isEmpty() == false) {
                dfs.parse(value.toString());
              }
            }
            catch (Exception e) {
              throw new UnifyException("jdoc_err_51", path);
            }
          }
          break;

        default:
          break;
      }

      break;
    }
  }

  private void validateFilterNames(String path, List<Token> tokenList) {
    String tokenPath = "$";

    for (int i = 0; i < tokenList.size(); i++) {
      Token token = tokenList.get(i);
      tokenPath = tokenPath + "." + token.getField();
      if (token.isArray() == true) {
        tokenPath = tokenPath + "[0]";
        ArrayToken arrayToken = (ArrayToken)token;
        ArrayToken.Filter filter = arrayToken.getFilter();
        if (filter.getType() == ArrayToken.FilterType.NAME_VALUE) {
          String fieldName = filter.getField();
          String fieldValue = filter.getValue();
          String modelPath = tokenPath + "." + fieldName;
          String format = getFieldFormat(path, modelPath, false);

          JsonNode node = getFormatNode(type, modelPath, format);
          DataType dataType = DataType.valueOf(node.get(CONSTS_JDOCS.FORMAT_FIELDS.TYPE).asText().toUpperCase());

          // this value is not used anywhere except to make sure that no exception is thrown in this method
          Object value = null;

          try {
            switch (dataType) {
              case STRING:
                value = fieldValue;
                break;

              case BOOLEAN:
                if (BaseUtils.compareWithMany(fieldValue, "true", "false") == true) {
                  value = new Boolean(fieldValue);
                }
                else {
                  throw new UnifyException("jdoc_err_53", fieldName, path);
                }
                break;

              case DATE:
                value = fieldValue;
                break;

              case INTEGER:
                value = new Integer(fieldValue);
                break;

              case LONG:
                value = new Long(fieldValue);
                break;

              case DECIMAL:
                value = new BigDecimal(fieldValue);
                break;

              default:
                break;
            }
          }
          catch (Exception e) {
            if ((e instanceof UnifyException) == false) {
              throw new UnifyException("jdoc_err_53", fieldName, path);
            }
            else {
              throw e;
            }
          }
        }
      }
    }
  }

  private JsonNode validatePath(Document doc, String path) {
    JsonNode modelNode = null;
    JDocument jd = (JDocument)doc;
    if (jd.isTyped()) {
      JDocument fromTypedDoc = jd;
      Document modelDoc = docModels.get(fromTypedDoc.type);
      String modelPath = getModelPath(path);
      modelNode = ((JDocument)modelDoc).getJsonNode(modelPath);
      if (modelNode == null) {
        throw new UnifyException("jdoc_err_38", fromTypedDoc.type, path);
      }
    }

    return modelNode;
  }

  private void validate(Document fromDoc, String fromPath, String toPath) {
    // check that the last character is not a .
    if (fromPath.indexOf('.') == (fromPath.length() - 1)) {
      throw new UnifyException("jdoc_err_39", fromPath);
    }

    if (toPath.indexOf('.') == (toPath.length() - 1)) {
      throw new UnifyException("jdoc_err_40", toPath);
    }

    // the function to validate the setContents call
    String toBasePath = toPath + ".";

    // validate the path that we want to write to
    JsonNode toModelNode = validatePath(this, toPath);

    // now validate the path we want to read from
    validatePath(fromDoc, fromPath);

    // get the node to copy
    JsonNode fromDocNode = ((JDocument)fromDoc).getJsonNode(fromPath);
    if (fromDocNode == null) {
      throw new UnifyException("jdoc_err_41", fromPath);
    }

    // validate the contents now
    List<String> errorList = validate(toModelNode, fromDocNode, toBasePath, type);

    processErrors(errorList);
  }

  private static List<String> validate(JsonNode modelNode, JsonNode docNode, String basePath, String type) {
    // function that invokes the recursive validation
    List<String> errorList = new ArrayList<>();
    validate(modelNode, docNode, basePath, errorList, type);
    return errorList;
  }

  private static void validate(JsonNode modelNode, JsonNode docNode, String basePath, List<String> errorList, String type) {
    // if the docNode is an array node then it will not have any fields and we need to handle it differently
    if (docNode.getNodeType() == JsonNodeType.ARRAY) {
      // running a loop for all elements of the updated ArrayNode
      for (int i = 0; i < docNode.size(); i++) {
        JsonNode docChildNode = docNode.get(i);
        JsonNode dmChildNode = modelNode;
        validate(dmChildNode, docChildNode, basePath + "[0]" + ".", errorList, type);
      }
    }
    else {
      // recursive function to validate the document
      Iterator<String> fieldNames = docNode.fieldNames();

      while (fieldNames.hasNext()) {
        String docFieldName = fieldNames.next();
        JsonNode docFieldNode = docNode.get(docFieldName);
        JsonNode modelFieldNode = modelNode.get(docFieldName);

        loop:
        while (true) {
          if (modelFieldNode == null) {
            // means that the field is not found in the data model
            errorList.add(basePath + docFieldName + " -> path not found in data model -> " + type);
            break loop;
          }

          // if node is an @ArrayNode
          if (docFieldNode.isArray() && modelFieldNode.isArray()) {
            // running a loop for all elements of the updated ArrayNode
            for (int i = 0; i < docFieldNode.size(); i++) {
              JsonNode docChildNode = docFieldNode.get(i);
              JsonNode dmChildNode = modelFieldNode.get(0);
              validate(dmChildNode, docChildNode, basePath + docFieldName + "[0]" + ".", errorList, type);
            }
            break loop;
          }

          if (docFieldNode.isObject() && modelFieldNode.isObject()) {
            validate(modelFieldNode, docFieldNode, basePath + docFieldName + ".", errorList, type);
            break loop;
          }

          if (docFieldNode instanceof ValueNode) {
            // we have reached a property object
            switch (docFieldNode.getNodeType()) {
              case BOOLEAN:
                validateField(modelFieldNode.asText(), docFieldNode.asBoolean(), basePath + docFieldName, type);
                break;

              case NUMBER:
                if (docFieldNode.isInt()) {
                  validateField(modelFieldNode.asText(), docFieldNode.asInt(), basePath + docFieldName, type);
                }
                else if (docFieldNode.isLong()) {
                  validateField(modelFieldNode.asText(), docFieldNode.asLong(), basePath + docFieldName, type);
                }
                else if (docFieldNode.isDouble()) {
                  validateField(modelFieldNode.asText(), docFieldNode.decimalValue(), basePath + docFieldName, type);
                }
                else {
                  throw new UnifyException("jdoc_err_44", basePath + docFieldName, docFieldNode.toString());
                }
                break;

              case STRING:
                validateField(modelFieldNode.asText(), docFieldNode.asText(), basePath + docFieldName, type);
                break;

              case NULL:
                validateField(modelFieldNode.asText(), null, basePath + docFieldName, type);
                break;

              default:
                throw new UnifyException("jdoc_err_42", type, basePath + docFieldName);
            }

            break loop;
          }

          errorList.add(basePath + docFieldName + " -> mismatch in object type between document and data model -> " + type);
          break loop;
        }
      }
    }
  }

  // protected as this method is called from the base class
  private void setFilterFieldNode(ObjectNode filterNode, String filterField, String filterValue, String path, String modelPath) {
    String format = getFieldFormat(path, modelPath, false);
    JsonNode node = getFormatNode(type, modelPath, format);
    DataType dataType = DataType.valueOf(node.get(CONSTS_JDOCS.FORMAT_FIELDS.TYPE).asText().toUpperCase());

    try {
      switch (dataType) {
        case STRING:
          filterNode.put(filterField, filterValue);
          break;

        case BOOLEAN:
          if (BaseUtils.compareWithMany(filterValue, "true", "false") == true) {
            filterNode.put(filterField, new Boolean(filterValue));
          }
          else {
            throw new UnifyException("jdoc_err_53", filterField, path);
          }
          break;

        case DATE:
          filterNode.put(filterField, filterValue);
          break;

        case INTEGER:
          filterNode.put(filterField, new Integer(filterValue));
          break;

        case LONG:
          filterNode.put(filterField, new Long(filterValue));
          break;

        case DECIMAL:
          filterNode.put(filterField, new BigDecimal(filterValue));
          break;

        default:
          // we will not come here
          break;
      }
    }
    catch (Exception e) {
      if ((e instanceof UnifyException) == false) {
        throw new UnifyException("jdoc_err_53", filterField, path);
      }
      else {
        throw e;
      }
    }
  }

  public List<String> flatten() {
    // this function will provide a list of all paths in the document
    List<PathValue> list = new LinkedList<>();
    List<String> list1 = new LinkedList<>();
    getJsonPaths(list, rootNode, "$", false);
    list.stream().forEach(s -> list1.add(s.getPath()));
    return list1;
  }

  public List<PathValue> flattenWithValues() {
    // this function will provide a list of all paths in the document along with the value as a string
    List<PathValue> list = new LinkedList<>();
    getJsonPaths(list, rootNode, "$", true);
    return list;
  }

  private void getJsonPaths(List<PathValue> list, JsonNode rootNode, String path, boolean getValue) {
    JsonNodeType nodeType = rootNode.getNodeType();
    if ((nodeType != JsonNodeType.ARRAY) && (nodeType != JsonNodeType.OBJECT)) {
      // it is an array value node
      processValueNode(list, path, "", rootNode, getValue);
    }
    else {
      Iterator<Map.Entry<String, JsonNode>> iter = rootNode.fields();
      while (iter.hasNext()) {
        Map.Entry<String, JsonNode> entry = iter.next();
        String fieldName = entry.getKey();
        JsonNode fieldNode = entry.getValue();

        switch (fieldNode.getNodeType()) {
          case ARRAY: {
            int size = fieldNode.size();
            for (int i = 0; i < size; i++) {
              JsonNode node = fieldNode.get(i);
              // recurse
              getJsonPaths(list, node, path + "." + fieldName + "[" + i + "]", getValue);
            }
          }
          break;

          case OBJECT:
            // recurse
            getJsonPaths(list, fieldNode, path + "." + fieldName, getValue);
            break;

          default:
            processValueNode(list, path, fieldName, fieldNode, getValue);
            break;
        }
      }
    }
  }

  private void processValueNode(List<PathValue> list, String path, String fieldName, JsonNode fieldNode, boolean getValue) {
    Object value = null;
    DataType dt = null;

    if (fieldName.isEmpty() != true) {
      path = path + "." + fieldName;
    }

    if (getValue == true) {
      if (isTyped() == true) {
        String mp = getModelPath(path);
        boolean isValueArray = false;
        if (path.charAt(path.length() - 1) == ']') {
          isValueArray = true;
        }
        String format = getFieldFormat(path, mp, isValueArray);
        JsonNode node = getFormatNode(type, path, format);
        String type = node.get(CONSTS_JDOCS.FORMAT_FIELDS.TYPE).asText();
        dt = DataType.valueOf(type.toUpperCase());
      }
      else {
        switch (fieldNode.getNodeType()) {
          case BOOLEAN:
            value = fieldNode.asBoolean();
            dt = DataType.BOOLEAN;
            break;

          case NULL:
            break;

          case NUMBER:
            if (fieldNode.isDouble() == true) {
              value = fieldNode.asDouble();
              dt = DataType.DECIMAL;
            }
            else if (fieldNode.isLong()) {
              value = fieldNode.asLong();
              dt = DataType.LONG;
            }
            else {
              value = fieldNode.asInt();
              dt = DataType.INTEGER;
            }
            break;

          case STRING:
            value = fieldNode.asText();
            dt = DataType.STRING;
            break;

          default:
            throw new UnifyException("jdoc_err_62", path);
        }
      }
    }

    list.add(new PathValue(path, value, dt));
  }

  private DiffInfo comparePaths(PathValue left, PathValue right) {
    PathDiffResult res = null;

    if ((left != null) && (right != null)) {
      // both paths exist
      Object ls = left.getValue();
      Object rs = right.getValue();
      if ((ls != null) && (rs != null)) {
        if (ls.equals(rs)) {
          res = PathDiffResult.EQUAL;
        }
        else {
          res = PathDiffResult.DIFFERENT;
        }
      }
      else if ((ls == null) && (rs != null)) {
        res = PathDiffResult.DIFFERENT;
      }
      else if ((ls != null) && (rs == null)) {
        res = PathDiffResult.DIFFERENT;
      }
      else {
        res = PathDiffResult.EQUAL;
      }
    }
    else if ((left == null) || (right != null)) {
      // left path does not exist
      Object rs = right.getValue();
      if (rs == null) {
        res = PathDiffResult.EQUAL;
      }
      else {
        res = PathDiffResult.ONLY_IN_RIGHT;
      }
    }
    else if ((left != null) && (right == null)) {
      // right path does not exist
      Object ls = left.getValue();
      if (ls == null) {
        res = PathDiffResult.EQUAL;
      }
      else {
        res = PathDiffResult.ONLY_IN_LEFT;
      }
    }
    else {
      // both values not present
      // cannot happen
    }
    return new DiffInfo(res, left, right);
  }

  public List<DiffInfo> getDifferences(Document right, boolean onlyDifferences) {
    List<DiffInfo> diffInfoList = new LinkedList<>();
    List<PathValue> leftPaths = flattenWithValues();
    List<PathValue> rightPaths = right.flattenWithValues();

    Map<String, PathValue> rightMap = new HashMap<>();
    rightPaths.stream().forEach(pv -> rightMap.put(pv.getPath(), pv));

    for (PathValue leftPv : leftPaths) {
      PathValue rightPv = rightMap.get(leftPv.getPath());
      DiffInfo di = comparePaths(leftPv, rightPv);
      if (onlyDifferences == true) {
        if (di.getDiffResult() != PathDiffResult.EQUAL) {
          diffInfoList.add(di);
        }
      }
      else {
        diffInfoList.add(di);
      }
      rightMap.remove(leftPv.getPath());
    }

    // now see if any right paths remain and process them if so
    if (rightMap.size() > 0) {
      rightPaths = rightMap.values().stream().collect(Collectors.toList());
      DiffInfo di = null;
      for (PathValue rightPv : rightPaths) {
        di = comparePaths(null, rightPv);
        if (onlyDifferences == true) {
          if (di.getDiffResult() != PathDiffResult.EQUAL) {
            diffInfoList.add(di);
          }
        }
        else {
          diffInfoList.add(di);
        }
      }
    }

    return diffInfoList;
  }

}
