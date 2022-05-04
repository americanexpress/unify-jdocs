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

import com.americanexpress.unify.base.BaseUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/*
 * @author Deepak Arora
 */
public class SortDocument {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Please specify one arguments as below:");
      System.out.println("Argument 1 -> full path to the json file that whose unique paths need to be searched e.g. C:/Temp/test.json");
      System.out.println("This program sorts a JSON document");
      System.exit(1);
    }

    InputStream is = new BufferedInputStream(new FileInputStream(args[0]));
    String json = BaseUtils.getStringFromStream(is);
    ObjectMapper objectMapper = new ObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    JsonNode node = objectMapper.readTree(json);
    List<Tuple> list = new ArrayList<>();
    traverse(node, list);
    StringBuffer sb = new StringBuffer();
    sb.append("{" + System.lineSeparator());
    writeJson(list, sb, 0);
    sb.append("}");
    System.out.println(sb.toString());
  }

  private static void traverse(JsonNode node, List<Tuple> parentList) {
    Iterator<Map.Entry<String, JsonNode>> iter = node.fields();
    while (iter.hasNext()) {
      Map.Entry<String, JsonNode> entry = iter.next();
      String fieldName = entry.getKey();
      JsonNode fieldNode = entry.getValue();

      JsonNodeType nodeType = fieldNode.getNodeType();
      if (nodeType == JsonNodeType.ARRAY) {
        List<Tuple> list = new ArrayList<>();
        Tuple tuple = new Tuple(fieldName, fieldNode, list);
        parentList.add(tuple);

        int size = fieldNode.size();
        for (int i = 0; i < size; i++) {
          JsonNode node1 = fieldNode.get(i);
          JsonNodeType nodeType1 = node1.getNodeType();
          if (nodeType1 == JsonNodeType.OBJECT) {
            List<Tuple> list1 = new ArrayList<>();
            Tuple tuple1 = new Tuple("", node1, list1);
            list.add(tuple1);
            traverse(node1, list1);
          }
          else {
            Tuple tuple1 = new Tuple("", node1, null);
            list.add(tuple1);
          }
        }
      }
      else if (nodeType == JsonNodeType.OBJECT) {
        // go inside
        List<Tuple> list = new ArrayList<>();
        Tuple tuple = new Tuple(fieldName, fieldNode, list);
        parentList.add(tuple);
        traverse(fieldNode, list);
      }
      else {
        // add to the list
        parentList.add(new Tuple(fieldName, fieldNode, null));
      }
    }

    sort(parentList);
  }

  private static void sort(List<Tuple> list) {
    // we need to sort this list in place
    Map<String, Tuple> map1 = new TreeMap<>();
    Map<String, Tuple> map2 = new TreeMap<>();

    // put in map first
    for (Tuple t : list) {
      if (t.list == null) {
        map1.put(t.fieldName, t);
      }
      else {
        map2.put(t.fieldName, t);
      }
    }

    // iterate over map1
    int index = 0;
    Set<String> keys = map1.keySet();
    for (String key : keys) {
      Tuple t = map1.get(key);
      list.set(index++, t);
    }

    // iterate over map2
    keys = map2.keySet();
    for (String key : keys) {
      Tuple t = map2.get(key);
      list.set(index++, t);
    }
  }

  private static void writeJson(List<Tuple> list, StringBuffer sb, int indentSize) {
    indentSize += 2;
    int size = list.size();
    for (int i = 0; i < size; i++) {
      Tuple t = list.get(i);
      if (t.list == null) {
        for (int j = 0; j < indentSize; j++) {
          sb.append(" ");
        }

        if (t.fieldName.isEmpty()) {
          sb.append(t.node.toPrettyString());
        }
        else {
          sb.append("\"" + t.fieldName + "\": " + t.node.toPrettyString());
        }

        if (i < (size - 1)) {
          sb.append("," + System.lineSeparator());
        }
        else {
          sb.append(System.lineSeparator());
        }
      }
      else {
        for (int j = 0; j < indentSize; j++) {
          sb.append(" ");
        }
        JsonNodeType nodeType = t.node.getNodeType();
        if (nodeType == JsonNodeType.ARRAY) {
          sb.append("\"" + t.fieldName + "\": [" + System.lineSeparator());
        }
        else {
          if (t.fieldName.isEmpty()) {
            sb.append("{" + System.lineSeparator());
          }
          else {
            sb.append("\"" + t.fieldName + "\": {" + System.lineSeparator());
          }
        }
        writeJson(t.list, sb, indentSize);
        for (int j = 0; j < indentSize; j++) {
          sb.append(" ");
        }
        if (nodeType == JsonNodeType.ARRAY) {
          sb.append("]");
        }
        else {
          sb.append("}");
        }
        if (i < (size - 1)) {
          sb.append("," + System.lineSeparator());
        }
        else {
          sb.append(System.lineSeparator());
        }
      }
    }
  }

}

class Tuple {

  public String fieldName;
  public JsonNode node;
  public List<Tuple> list;

  public Tuple(String fieldName, JsonNode node, List<Tuple> list) {
    this.fieldName = fieldName;
    this.node = node;
    this.list = list;
  }

}
