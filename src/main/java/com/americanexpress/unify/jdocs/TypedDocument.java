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

import com.americanexpress.unify.jdocs.CONSTS_JDOCS.FORMAT_FIELDS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * @author Deepak Arora
 */
public class TypedDocument extends BaseDocument {

  // a map to store the doc models in use. In future we could use an ExpiryMap
  private static Map<String, Document> docModels = new ConcurrentHashMap<>();

  // for each model document, store a map of the constraint string and the corresponding JsonNode
  private static Map<String, Map<String, JsonNode>> docModelPaths = new ConcurrentHashMap<>();

  // for each regular expression pattern, store the compiled pattern
  private static Map<String, Pattern> compiledPatterns = new ConcurrentHashMap<>();

  // logger
  private static Logger logger = LogManager.getLogger(TypedDocument.class);

  // type of the document
  private String type;

  public static void loadDocumentModel(String type, String json) {
    try {
      json = insertReferredModels(json);
    }
    catch (IOException ex) {
      logger.error("IO exception encountered for type {}, error message -> {}", type, ex.getMessage());
    }

    Document d = new BaseDocument(json);
    setDocumentModel(type, d);
    logger.info("Successfully loaded document model -> " + type);
  }

  /**
   * @param type
   */
  public TypedDocument(String type) {
    super();
    this.type = type;
  }

  /**
   * @param type
   * @param json
   */
  public TypedDocument(String type, String json) {
    super(json);
    this.type = type;
    validate();
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

  /**
   * @param type
   * @return
   */
  public static Document getDocumentModel(String type) {
    return docModels.get(type);
  }

  /**
   * @return
   */
  public String getType() {
    return type;
  }

  @Override
  public synchronized Document deepCopy() {
    TypedDocument d = new TypedDocument(type);
    d.rootNode = rootNode.deepCopy();
    return d;
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
      throw new UnifyException("jdoc_err_38", path, modelPath);
    }

    return format;
  }

  private void checkPathExistsInModel(String path) {
    Document md = docModels.get(type);
    boolean b = md.pathExists(path);
    if (b == false) {
      throw new UnifyException("jdoc_err_38", path, path);
    }
  }

  /**
   * @param path
   * @param vargs
   * @return
   * @throws UnifyException
   */
  @Override
  public Object getValue(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return getValue(path, null, tokenList);
  }

  /**
   * @param path
   * @param vargs
   * @return
   * @throws UnifyException
   */
  @Override
  public String getString(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return (String)getValue(path, String.class, tokenList);
  }

  /**
   * @param path
   * @param vargs
   * @return
   * @throws UnifyException
   */
  @Override
  public Integer getInteger(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return (Integer)getValue(path, Integer.class, tokenList);
  }

  /**
   * @param path
   * @param vargs
   * @return
   * @throws UnifyException
   */
  @Override
  public Boolean getBoolean(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return (Boolean)getValue(path, Boolean.class, tokenList);
  }

  @Override
  public Long getLong(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return (Long)getValue(path, Long.class, tokenList);
  }

  @Override
  public Double getDouble(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return (Double)getValue(path, Double.class, tokenList);
  }

  @Override
  public String getArrayValueString(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return (String)getValue(path, String.class, tokenList);
  }

  @Override
  public Integer getArrayValueInteger(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return (Integer)getValue(path, Integer.class, tokenList);
  }

  @Override
  public Boolean getArrayValueBoolean(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return (Boolean)getValue(path, Boolean.class, tokenList);
  }

  @Override
  public Long getArrayValueLong(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return (Long)getValue(path, Long.class, tokenList);
  }

  @Override
  public Double getArrayValueDouble(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return (Double)getValue(path, Double.class, tokenList);
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

    validate(fromDoc, fromPath, toPath);
    super.setContent(fromDoc, fromPath, toPath);
  }

  private void validateField(String path, Object value) {
    validateField(path, value, false);
  }

  private void validateField(String path, Object value, boolean isValueArray) {
    String modelPath = getModelPath(path);
    String format = getFieldFormat(path, modelPath, isValueArray);
    validateField(format, value, modelPath, type);
  }

  @Override
  public void setBoolean(String path, boolean value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    validateField(path, value);
    setValue(path, tokenList, value);
  }

  @Override
  public void setInteger(String path, int value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    validateField(path, value);
    setValue(path, tokenList, value);
  }

  @Override
  public void setLong(String path, long value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    validateField(path, value);
    setValue(path, tokenList, value);
  }

  @Override
  public void setDouble(String path, double value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    validateField(path, value);
    setValue(path, tokenList, value);
  }

  @Override
  public void setString(String path, String value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    validateField(path, value);
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueString(String path, String value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    validateField(path, value, true);
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueInteger(String path, int value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    validateField(path, value, true);
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueBoolean(String path, boolean value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    validateField(path, value, true);
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueLong(String path, long value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    validateField(path, value, true);
    setValue(path, tokenList, value);
  }

  @Override
  public void setArrayValueDouble(String path, double value, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.SET_ARRAY_VALUE, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    validateField(path, value, true);
    setValue(path, tokenList, value);
  }

  @Override
  public void deletePath(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.DELETE_PATH, PathAccessType.OBJECT, vargs);
    validateFilterNames(path, tokenList);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    deletePath(path, tokenList);
  }

  @Override
  public int getArraySize(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_SIZE, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return getArraySize(path, tokenList);
  }

  @Override
  public int getArrayIndex(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.GET_ARRAY_INDEX, PathAccessType.VALUE, vargs);
    validateFilterNames(path, tokenList);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return getArrayIndex(path, tokenList);
  }

  @Override
  public boolean pathExists(String path, String... vargs) {
    path = getStaticPath(path, vargs);
    List<Token> tokenList = validatePath(path, CONSTS_JDOCS.API.PATH_EXISTS, PathAccessType.OBJECT, vargs);
    validateFilterNames(path, tokenList);
    String modelPath = getModelPath(path);
    checkPathExistsInModel(modelPath);
    return pathExists(path, tokenList);
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

  @Override
  public void merge(Document d, List<String> pathsToDelete) {
    if ((d instanceof TypedDocument) == false) {
      throw new UnifyException("jdoc_err_31");
    }


    TypedDocument td = (TypedDocument)d;
    if (type.equals(td.type) == false) {
      throw new UnifyException("jdoc_err_55");
    }

    // first delete the paths
    if (pathsToDelete != null) {
      pathsToDelete.stream().forEach(s -> deletePath(s));
    }

    // now merge
    JsonNode modelNode = null;
    BaseDocument bd = (BaseDocument)TypedDocument.getDocumentModel(td.getType());
    modelNode = bd.rootNode;
    merge(rootNode, ((BaseDocument)d).rootNode, modelNode);
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
      String keyField = getKeyField(modelNode, field);
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

  private static String getKeyField(ArrayNode modelNode, String field) {
    String keyField = null;

    JsonNode node = modelNode.get(0).get(FORMAT_FIELDS.KEY);
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
            valueNode = new DoubleNode(fromFieldNode.doubleValue());
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

  private static JsonNode getDocModelPathNode(String type, String path, String format) {
    Map<String, JsonNode> map = docModelPaths.get(type);
    if (map == null) {
      map = new ConcurrentHashMap<>();
      docModelPaths.put(type, map);
    }

    // get the root node
    JsonNode node = map.get(path);
    if (node == null) {
      try {
        node = BaseDocument.objectMapper.readTree(format);
      }
      catch (IOException e) {
        throw new UnifyException("jdoc_err_1", path);
      }
      map.put(path, node);
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
    JsonNode node = getDocModelPathNode(type, path, format);

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
      String fieldType = node.get(CONSTS_JDOCS.FORMAT_FIELDS.TYPE).asText();
      switch (fieldType) {
        case CONSTS_JDOCS.TYPES.STRING:
          if ((value instanceof String) == false) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        case CONSTS_JDOCS.TYPES.BOOLEAN:
          if ((value instanceof Boolean) == false) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        case CONSTS_JDOCS.TYPES.DATE:
          if ((value instanceof String) == false) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        case CONSTS_JDOCS.TYPES.INTEGER:
          if (((value instanceof Integer) == false)) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        case CONSTS_JDOCS.TYPES.LONG:
          if (((value instanceof Long) == false)) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        case CONSTS_JDOCS.TYPES.DOUBLE:
          // Couchbase stores a decimal value of 10.00 as 10 in the json document
          // hence when we read the document and construct the typed document we
          // will need to check against int and long data types as well
          if (((value instanceof Double) == false) && ((value instanceof Integer) == false) && ((value instanceof Long) == false)) {
            throw new UnifyException("jdoc_err_37", path);
          }
          break;

        default:
          break;
      }

      // check against regex pattern and format
      switch (fieldType) {
        case CONSTS_JDOCS.TYPES.STRING:
        case CONSTS_JDOCS.TYPES.BOOLEAN:
        case CONSTS_JDOCS.TYPES.INTEGER:
        case CONSTS_JDOCS.TYPES.LONG:
        case CONSTS_JDOCS.TYPES.DOUBLE: {
          String s = value.toString();
          JsonNode node1 = node.get(FORMAT_FIELDS.REGEX);
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

        case CONSTS_JDOCS.TYPES.DATE:
          // Match input date with the format provided
          String fieldValue = node.get(FORMAT_FIELDS.FORMAT).asText();
          try {
            DateTimeFormatter dfs = DateTimeFormatter.ofPattern(fieldValue);
            if (value.toString().isEmpty() == false) {
              dfs.parse(value.toString());
            }
          }
          catch (Exception e) {
            throw new UnifyException("jdoc_err_51", path);
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

          JsonNode node = getDocModelPathNode(type, modelPath, format);
          String fieldType = node.get(CONSTS_JDOCS.FORMAT_FIELDS.TYPE).asText();

          // this value is not used anywhere except to make sure that no exception is thrown in this method
          Object value = null;

          try {
            switch (fieldType) {
              case CONSTS_JDOCS.TYPES.STRING:
                value = fieldValue;
                break;

              case CONSTS_JDOCS.TYPES.BOOLEAN:
                if (BaseUtils.compareWithMany(fieldValue, "true", "false") == true) {
                  value = new Boolean(fieldValue);
                }
                else {
                  throw new UnifyException("jdoc_err_53", fieldName, path);
                }
                break;

              case CONSTS_JDOCS.TYPES.DATE:
                value = fieldValue;
                break;

              case CONSTS_JDOCS.TYPES.INTEGER:
                value = new Integer(fieldValue);
                break;

              case CONSTS_JDOCS.TYPES.LONG:
                value = new Long(fieldValue);
                break;

              case CONSTS_JDOCS.TYPES.DOUBLE:
                value = new Double(fieldValue);
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
    if (doc instanceof TypedDocument) {
      TypedDocument fromTypedDoc = (TypedDocument)doc;
      Document modelDoc = docModels.get(fromTypedDoc.type);
      String modelPath = getModelPath(path);
      modelNode = ((BaseDocument)modelDoc).getJsonNode(modelPath);
      if (modelNode == null) {
        throw new UnifyException("jdoc_err_38", path, modelPath);
      }
    }
    return modelNode;
  }

  /**
   *
   */
  public final void validate() {
    // function to validate the contents of the whole document
    Document md = docModels.get(type);
    List<String> errorList = validate(((BaseDocument)md).rootNode, rootNode, "$.", type);
    processErrors(errorList);
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
    JsonNode fromDocNode = ((BaseDocument)fromDoc).getJsonNode(fromPath);
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
                  validateField(modelFieldNode.asText(), docFieldNode.asDouble(), basePath + docFieldName, type);
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
  protected void setFilterFieldNode(ObjectNode filterNode, String filterField, String filterValue, String path, String modelPath) {
    String format = getFieldFormat(path, modelPath, false);
    JsonNode node = getDocModelPathNode(type, modelPath, format);
    String fieldType = node.get(CONSTS_JDOCS.FORMAT_FIELDS.TYPE).asText();

    try {
      switch (fieldType) {
        case CONSTS_JDOCS.TYPES.STRING:
          filterNode.put(filterField, filterValue);
          break;

        case CONSTS_JDOCS.TYPES.BOOLEAN:
          if (BaseUtils.compareWithMany(filterValue, "true", "false") == true) {
            filterNode.put(filterField, new Boolean(filterValue));
          }
          else {
            throw new UnifyException("jdoc_err_53", filterField, path);
          }
          break;

        case CONSTS_JDOCS.TYPES.DATE:
          filterNode.put(filterField, filterValue);
          break;

        case CONSTS_JDOCS.TYPES.INTEGER:
          filterNode.put(filterField, new Integer(filterValue));
          break;

        case CONSTS_JDOCS.TYPES.LONG:
          filterNode.put(filterField, new Long(filterValue));
          break;

        case CONSTS_JDOCS.TYPES.DOUBLE:
          filterNode.put(filterField, new Double(filterValue));
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
            String contents = BaseUtils.getResourceAsString(TypedDocument.class, rfn).trim();
            sb.append(contents.substring(1, contents.length() - 1));

            if (line.charAt(line.length() - 1) == ',') {
              sb.append(',');
            }
          }
          else {
            sb.append(line);
          }
        }

        json = sb.toString();

        if (isReferred == false) {
          break;
        }
      }
    }

    JsonNode node = BaseDocument.objectMapper.readTree(json);
    json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    return json;
  }

}
