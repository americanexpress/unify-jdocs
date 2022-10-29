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
import com.americanexpress.unify.base.UnifyException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonPathUtils {

  private static final String pattern = "\\$\\.[a-zA-Z \\-\\[=%\\]\\.0-9_]+";
  private static final Matcher matcher = Pattern.compile(pattern).matcher("");

  public void getUnusedPaths(String filePath, String dirPath, String filePattern) throws IOException {
    Set<String> unusedPaths = new HashSet<>();
    getUniquePaths(filePath).forEach(unusedPaths::add);
    getUnusedPaths(unusedPaths, dirPath, filePattern);
    unusedPaths.forEach(System.out::println);
  }

  public void getUsedPaths(String filePath, String dirPath, String filePattern) throws IOException {
    Set<String> usedPaths = new HashSet<>();
    getUniquePaths(filePath).forEach(usedPaths::add);
    getUsedPaths(usedPaths, dirPath, filePattern);
    usedPaths.forEach(System.out::println);
  }

  private void getUnusedPaths(Set<String> paths, String baseDirPath, String filePattern) throws IOException {
    try (Stream<Path> walk = Files.walk(Paths.get(baseDirPath))) {
      List<String> result = walk.map(x -> x.toString()).filter(f -> f.endsWith(filePattern)).collect(Collectors.toList());
      result.forEach(s -> {
        try {
          System.out.println("Processing file -> " + s);
          removeUnused(paths, s);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void getUsedPaths(Set<String> paths, String baseDirPath, String filePattern) throws IOException {
    try (Stream<Path> walk = Files.walk(Paths.get(baseDirPath))) {
      Set<String> usedPaths = new HashSet<>();
      List<String> result = walk.map(x -> x.toString()).filter(f -> f.endsWith(filePattern)).collect(Collectors.toList());
      result.forEach(s -> {
        try {
          System.out.println("Processing file -> " + s);
          getUsedPaths(paths, s, usedPaths);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      });
      paths.clear();
      paths.addAll(usedPaths);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void getUsedPaths(Set<String> paths, String fileName, Set<String> usedPaths) throws IOException {
    Stream<String> lines = Files.lines(Paths.get(fileName));
    lines.forEach(s -> checkLine(paths, s, usedPaths));
  }

  private void removeUnused(Set<String> paths, String fileName) throws IOException {
    Stream<String> lines = Files.lines(Paths.get(fileName));
    lines.forEach(s -> checkLine(paths, s));
  }

  private void checkLine(Set<String> paths, String line) {
    matcher.reset(line);
    while (matcher.find()) {
      String path = line.substring(matcher.start(), matcher.end());
      path = path.replaceAll("\\[[a-zA-Z0-9 \\-=_%]+\\]", "[]");
      if (paths.contains(path) == true) {
        paths.remove(path);
      }
    }
  }

  private void checkLine(Set<String> paths, String line, Set<String> usedPaths) {
    matcher.reset(line);
    while (matcher.find()) {
      String path = line.substring(matcher.start(), matcher.end());
      path = path.replaceAll("\\[[a-zA-Z0-9 \\-=_%]+\\]", "[]");
      if (paths.contains(path) == true) {
        usedPaths.add(path);
      }
    }
  }

  public List<String> flattenPaths(String filePath) throws IOException {
    List<String> list = new LinkedList<>();
    InputStream is = new BufferedInputStream(new FileInputStream(filePath));
    String json = BaseUtils.getStringFromStream(is);
    Document d = new JDocument(json);
    return d.flatten();
  }

  public List<PathValue> flattenPathsWithValues(String filePath) throws IOException {
    List<String> list = new LinkedList<>();
    InputStream is = new BufferedInputStream(new FileInputStream(filePath));
    String json = BaseUtils.getStringFromStream(is);
    Document d = new JDocument(json);
    List<PathValue> pathValues = d.flattenWithValues();
    return pathValues;
  }

  public List<String> getUniquePaths(String filePath) throws IOException {
    List<String> list = flattenPaths(filePath);
    return getUniquePaths(list);
  }

  public List<String> getUniquePaths(Document d) throws IOException {
    List<String> list = d.flatten();
    return getUniquePaths(list);
  }

  private List<String> getUniquePaths(List<String> flattenedPaths) {
    Map<String, String> map = new HashMap<>();

    for (String s : flattenedPaths) {
      s = s.replaceAll("\\[\\d*+\\]", "[]");
      map.put(s, s);
    }

    List<String> list = new LinkedList<>();
    Set<String> keys = map.keySet();
    for (String key : keys) {
      list.add(key);
    }

    return list;
  }

  public static List<String> getZeroPaddedIndexes(List<String> paths) {
    // this method takes a list of paths where the indexes are numeric or empty and pads
    // each index with zeros upto a total width of 6. This means that the delete
    // feature during merge will work as long as there are not more than 999,999
    // elements in an array
    List<String> newPaths = new ArrayList<>(paths.size());

    for (String path : paths) {
      List<Token> tokens = Parser.getTokens(path);

      String s = "$";
      for (Token t : tokens) {
        if (t.isArray()) {
          ArrayToken at = (ArrayToken)t;
          s = s + "." + at.getField() + "[";

          ArrayToken.FilterType ft = at.getFilter().getType();
          if (ft == ArrayToken.FilterType.EMPTY) {
            s = s + "]";
          }
          else if (ft == ArrayToken.FilterType.INDEX) {
            // here we pad the value
            int index = at.getFilter().getIndex();
            String paddedIndex = String.format("%06d", index);
            s = s + paddedIndex + "]";
          }
          else if (ft == ArrayToken.FilterType.NAME_VALUE) {
            throw new UnifyException("jdoc_err_67", path);
          }
        }
        else {
          s = s + "." + t.getField();
        }
      }

      newPaths.add(s);
    }

    return newPaths;
  }

  public static List<String> getNoPaddedIndexes(List<String> paths) {
    // this method takes a list of paths where the indexes are padded with
    // zeroes. It removes the zeros and returns the list of paths
    // it preserves the order in which the paths are stored
    List<String> newPaths = new ArrayList<>(paths.size());

    for (String path : paths) {
      List<Token> tokens = Parser.getTokens(path);

      String s = "$";
      for (Token t : tokens) {
        if (t.isArray()) {
          ArrayToken at = (ArrayToken)t;
          s = s + "." + at.getField() + "[";

          ArrayToken.FilterType ft = at.getFilter().getType();
          if (ft == ArrayToken.FilterType.EMPTY) {
            s = s + "]";
          }
          else if (ft == ArrayToken.FilterType.INDEX) {
            // here we remove the leading zeros
            int index = at.getFilter().getIndex();
            s = s + index + "]";
          }
          else if (ft == ArrayToken.FilterType.NAME_VALUE) {
            throw new UnifyException("jdoc_err_67", path);
          }
        }
        else {
          s = s + "." + t.getField();
        }
      }

      newPaths.add(s);
    }

    return newPaths;
  }

}
