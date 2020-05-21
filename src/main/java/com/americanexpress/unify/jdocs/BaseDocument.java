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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.americanexpress.unify.jdocs.BaseUtils.removeEscapeChars;

/*
 * @author Deepak Arora
 */
public class BaseDocument implements Document {

  // logger
  private static Logger logger = LogManager.getLogger(BaseDocument.class);

  // root json node of the document
  /**
   *
   */
  protected JsonNode rootNode = null;

  // one and only one object mapper -> object mappers are thread safe!!!
  /**
   *
   */
  public static ObjectMapper objectMapper = new ObjectMapper();

  /**
   *
   */
  public BaseDocument() {
    try {
      rootNode = objectMapper.readTree("{}");
    }
    catch (IOException ex) {
      throw new UnifyException("jdoc_err_1", ex);
    }
  }

  /**
   * @param json
   */
  public BaseDocument(String json) {
    try {
      rootNode = objectMapper.readTree(json);
    }
    catch (IOException ex) {
      throw new UnifyException("jdoc_err_1", ex);
    }
  }

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
    throw new UnifyException("jdoc_err_2");
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
          throw new UnifyException("jdocs_err_11", token.getField());
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
              value = node.asDouble();
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

          if (clazz == Double.class) {
            value = node.asDouble();
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
    else if (value instanceof Double) {
      node.put(field, (Double)value);
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
        else if (value instanceof Double) {
          node.insert(index, (Double)value);
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
        else if (value instanceof Double) {
          node.add((Double)value);
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
              double doubleValue = fieldNode.asDouble();
              if (doubleValue == Double.valueOf(filterValue)) {
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
        if (this instanceof TypedDocument) {
          // we need to create the appropriate type of the node and for this we need to get the data type from the model
          TypedDocument td = (TypedDocument)this;
          String modelPath = tokenPath + "." + filterField;
          td.setFilterFieldNode((ObjectNode)filterNode, filterField, filterValue, path, modelPath);
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
    return getValue(path, null, tokenList);
  }

  @Override
  public String getString(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    return (String)getValue(path, String.class, tokenList);
  }

  @Override
  public Integer getInteger(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    return (Integer)getValue(path, Integer.class, tokenList);
  }

  @Override
  public Boolean getBoolean(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    return (Boolean)getValue(path, Boolean.class, tokenList);
  }

  @Override
  public Long getLong(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    return (Long)getValue(path, Long.class, tokenList);
  }

  @Override
  public Double getDouble(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    return (Double)getValue(path, Double.class, tokenList);
  }

  @Override
  public String getArrayValueString(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    return (String)getValue(path, String.class, tokenList);
  }

  @Override
  public Integer getArrayValueInteger(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    return (Integer)getValue(path, Integer.class, tokenList);
  }

  @Override
  public Boolean getArrayValueBoolean(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    return (Boolean)getValue(path, Boolean.class, tokenList);
  }

  @Override
  public Long getArrayValueLong(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    return (Long)getValue(path, Long.class, tokenList);
  }

  @Override
  public Double getArrayValueDouble(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    return (Double)getValue(path, Double.class, tokenList);
  }

  @Override
  public void setString(String path, String value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    setValue(path, tokenList, value);
  }

  @Override
  public void setInteger(String path, int value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    setValue(path, tokenList, value);
  }

  @Override
  public void setBoolean(String path, boolean value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    setValue(path, tokenList, value);
  }

  @Override
  public void setLong(String path, long value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    setValue(path, tokenList, value);
  }

  @Override
  public void setDouble(String path, double value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueString(String path, String value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueInteger(String path, int value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueBoolean(String path, boolean value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueLong(String path, long value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueDouble(String path, double value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
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

    // this function copies the content from document to another document
    // if the value does not exist, nothing is done
    while (true) {
      // from node handling
      JsonNodeType fromNodeType = null;

      List<Token> tokenList = parse(fromPath);
      validatePath1(fromPath, CONSTS_JDOCS.API.SET_CONTENT, tokenList, PathAccessType.OBJECT);

      JsonNode fromNode = traverse(((BaseDocument)fromDoc).rootNode, tokenList, false);
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
      validatePath1(toPath, CONSTS_JDOCS.API.SET_CONTENT, tokenList, PathAccessType.OBJECT);

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
    BaseDocument d = new BaseDocument();
    d.rootNode = rootNode.deepCopy();
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
    deletePath(path, tokenList);
  }

  protected List<Token> parse(String path) {
    return Parser.getTokens(path);
  }

}

class Token {

  private String field;

  private boolean isLeaf;

  public Token(String field, boolean isLeaf) {
    this.field = field;
    this.isLeaf = isLeaf;
  }

  public String getField() {
    return field;
  }

  public boolean isArray() {
    return false;
  }

  public boolean isLeaf() {
    return isLeaf;
  }

}

class ArrayToken extends Token {

  public enum FilterType {

    NAME_VALUE, INDEX, EMPTY

  }

  public class Filter {

    private FilterType type = null;

    private String field = null;

    private String value = null;

    private int index = -1;

    public Filter(String field, String value) {
      this.field = field;
      this.value = value;
      type = FilterType.NAME_VALUE;
    }

    public Filter(int index) {
      this.index = index;
      type = FilterType.INDEX;
    }

    public Filter() {
      type = FilterType.EMPTY;
    }

    public FilterType getType() {
      return type;
    }

    public String getField() {
      return field;
    }

    public String getValue() {
      return value;
    }

    public int getIndex() {
      return index;
    }

  }

  private Filter filter = null;

  public ArrayToken(String name, String field, String value, boolean isLeaf) {
    super(name, isLeaf);
    filter = new Filter(field, value);
  }

  public ArrayToken(String name, int index, boolean isLeaf) {
    super(name, isLeaf);
    filter = new Filter(index);
  }

  public ArrayToken(String name, boolean isLeaf) {
    super(name, isLeaf);
    filter = new Filter();
  }

  @Override
  public boolean isArray() {
    return true;
  }

  public Filter getFilter() {
    return filter;
  }

}

enum PathAccessType {

  VALUE, OBJECT;

}

class Parser {

  public static List<Token> getTokens(String path) {
    List<String> strTokens = getStringTokens(path);
    List<Token> tokens = getTokens(strTokens);
    return tokens;
  }

  private static List<Token> getTokens(List<String> strTokens) {
    List<Token> tokens = new ArrayList<>();
    int size = strTokens.size();

    for (int i = 0; i < size; i++) {
      String strToken = strTokens.get(i);
      boolean isLeaf = false;

      if (i == (size - 1)) {
        isLeaf = true;
      }

      int first = isPresent(strToken, '[');
      if (first != -1) {
        tokens.add(getArrayToken(strToken, first, isLeaf));
      }
      else {
        String s = removeEscapeChars(strToken, '\\', '.', '[', ']', '=');
        tokens.add(new Token(s, isLeaf));
      }
    }

    return tokens;
  }

  private static ArrayToken getArrayToken(String s, int first, boolean isLeaf) {
    ArrayToken at = null;
    String name = removeEscapeChars(s.substring(0, first), '\\', '.', '[', ']', '=');

    while (true) {
      if (s.charAt(first + 1) == ']') {
        // it is a empty array token
        at = new ArrayToken(name, isLeaf);
        break;
      }

      {
        int pos = s.lastIndexOf(']');
        s = s.substring(first + 1, pos);
        pos = isPresent(s, '=');
        if (pos != -1) {
          // it is a key value pair
          String key = removeEscapeChars(s.substring(0, pos), '\\', '.', '[', ']', '=');
          String value = removeEscapeChars(s.substring(pos + 1), '\\', '.', '[', ']', '=');
          at = new ArrayToken(name, key, value, isLeaf);
        }
        else {
          // it is an index
          s = removeEscapeChars(s, '\\', '.', '[', ']', '=');
          at = new ArrayToken(name, Integer.parseInt(s), isLeaf);
        }
        break;
      }
    }

    return at;
  }

  private static int isPresent(String s, char symbol) {
    // return -1 means not present else present
    int pos = -1;

    int start = 0;
    while (true) {
      if (start >= s.length()) {
        break;
      }
      int i = s.indexOf(symbol, start);
      if (i != -1) {
        if (isEscaped(s, i, '\\') == false) {
          pos = i;
          break;
        }
        else {
          start = i + 1;
        }
      }
      else {
        break;
      }
    }

    return pos;
  }


  private static List<String> getStringTokens(String s) {
    List<String> paths = new ArrayList<>();
    int from = 2;

    for (int i = 2; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '.') {
        if (isEscaped(s, i, '\\') == false) {
          paths.add(s.substring(from, i));
          from = i + 1;
        }
      }
    }

    if (from < s.length()) {
      paths.add(s.substring(from));
    }

    return paths;
  }

  private static boolean isEscaped(String s, int pos, char ec) {
    if (pos == 0) {
      return false;
    }

    char c = s.charAt(pos - 1);
    if (c == ec) {
      return true;
    }
    else {
      return false;
    }
  }

}
