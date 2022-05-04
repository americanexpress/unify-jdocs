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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * @author Deepak Arora
 */
public class JsonPathTest {

  public static void main(String[] args) throws IOException {
    // checkArrayKeys(args);
    flattenPaths(args);
  }

  public static void foo() {
    Document d = new JDocument("45");
    d.setString("$.name", "Deepak");
    System.out.println(d.getPrettyPrintJson());
  }

  public static void foo1(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Please specify the full path to the json file e.g. C:/Temp/test.json");
      System.exit(1);
    }

    //    List<String> list = new JsonPathUtils().flattenPaths(args[0]);
    //    list.forEach(s -> System.out.println(s));
    //    System.out.println();

    List<PathValue> list = new JsonPathUtils().flattenPathsWithValues(args[0]);
    List<String> sList = new ArrayList<>(3000);
    list.forEach(pv -> sList.add(pv.getPath() + " = " + pv.getValue()));
    Collections.sort(sList);
    sList.forEach(s -> System.out.println(s));
    System.out.println();

    //    list = new JsonPathUtils().getUniquePaths(args[0]);
    //    list.forEach(s -> System.out.println(s));
    //    System.out.println();
  }

  public static void flattenPaths(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Please specify the full path to the json file e.g. C:/Temp/test.json");
      System.exit(1);
    }

    List<String> list = new JsonPathUtils().flattenPaths(args[0]);
    list.forEach(s -> System.out.println(s));
    System.out.println();
  }

  public static void checkArrayKeys(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Please specify the base path to the directory containing model files i.e. C:/Temp, note no trailing slash");
      System.exit(1);
    }

    try (Stream<Path> walk = Files.walk(Paths.get(args[0]))) {
      List<String> result = walk.map(x -> x.toString()).filter(f -> f.endsWith(".json")).collect(Collectors.toList());
      result.forEach(s -> {
        try {
          checkArrayKeys(s);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      });
    }

  }

  private static void checkArrayKeys(String filePath) throws IOException {
    System.out.println("Processing file -> " + filePath);

    byte[] bytes = Files.readAllBytes(Paths.get(filePath));
    String json = new String(bytes);
    Document d = new JDocument(json);

    List<String> list = new JsonPathUtils().flattenPaths(filePath);
    for (String s : list) {
      if (s.endsWith(".jdocs_arr_pk") == true) {
        int index = s.lastIndexOf(".jdocs_arr_pk");
        String base = s.substring(0, index);
        String entry = d.getString(s);
        Document d1 = new JDocument(entry);
        String fieldName = d1.getString("$.field");
        String fieldPath = base + "." + fieldName;
        try {
          d.getString(fieldPath);
        }
        catch (Exception e) {
          System.out.println("Error in file -> " + filePath + ", array key -> " + s);
        }
      }
    }
  }

}
