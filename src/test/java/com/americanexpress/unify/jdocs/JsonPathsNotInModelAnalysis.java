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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * @author Deepak Arora
 */
public class JsonPathsNotInModelAnalysis {

  private static final String pattern = "\\$\\.[a-zA-Z \\-\\[=%\\]\\.0-9_]+";
  private static final Matcher matcher = Pattern.compile(pattern).matcher("");
  private static String fileExtension = null;

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.out.println("Please specify three arguments as below:");
      System.out.println("Argument 1 -> full path to the json file that whose unique paths need to be searched e.g. C:/Temp/test.json");
      System.out.println("Argument 2 -> path to the base directory under which all files matching the pattern are to be searched recursively e.g. C:/Source. Note no trailing slash");
      System.out.println("Argument 3 -> the file extension to look for under the directory e.g. .java");
      System.out.println("This program looks up the usage of all unique paths across all matched file under the base directory and matches them against the paths in the model file. Paths not matched are returned");
      System.exit(1);
    }

    // get the unique paths in the code base
    fileExtension = args[2];
    List<String> uniqueCodePathsList = getListOfUniquePaths(args[1], args[2]);
    // uniqueCodePaths.stream().forEach(s -> System.out.println(s));

    // get unique paths in the json document
    List<String> uniqueModelPathsList = new ArrayList<>();
    {
      List<String> paths = new JsonPathUtils().getUniquePaths(args[0]);
      for (String s : paths) {
        if (s.endsWith(".jdocs_arr_pk") == false) {
          uniqueModelPathsList.add(s);
        }
        // uniqueModelPaths.stream().forEach(s -> System.out.println(s));
      }
    }
    Set<String> uniqueModelPathsSet = new HashSet<>();
    uniqueModelPathsSet.addAll(uniqueModelPathsList);

    outer:
    for (String codePath : uniqueCodePathsList) {
      if (uniqueModelPathsSet.contains(codePath)) {
        continue outer;
      }
      for (String s : uniqueModelPathsList) {
        if (s.startsWith(codePath)) {
          continue outer;
        }
      }
      System.out.println(codePath);
    }
  }

  private static List<String> getListOfUniquePaths(String baseDirPath, String filePattern) {
    HashSet<String> uniquePaths = new HashSet<>();
    try (Stream<Path> walk = Files.walk(Paths.get(baseDirPath))) {
      Set<String> usedPaths = new HashSet<>();
      List<String> result = walk.map(x -> x.toString()).filter(f -> f.endsWith(filePattern)).collect(Collectors.toList());
      result.forEach(s -> {
        try {
          if (!s.endsWith("Test" + fileExtension) && !s.endsWith("Mock" + fileExtension) && !s.endsWith("Ctf" + fileExtension)
                  && !s.endsWith("V3ToV4.java") && !s.endsWith("SetContentUtilsV3.java") && !s.endsWith("GdeRequestXmlGenerator.java")
                  && !s.endsWith("GdeResponse.java.java")) {
            System.out.println("Processing file -> " + s);
            Stream<String> lines = Files.lines(Paths.get(s));
            lines.forEach(line -> checkLine(line, uniquePaths));
          }
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    List<String> list = uniquePaths.stream().collect(Collectors.toList());
    return list;
  }

  private static void checkLine(String line, Set<String> uniquePaths) {
    line = line.trim();
    if (line.startsWith("//")) {
      return;
    }
    matcher.reset(line);
    while (matcher.find()) {
      String path = line.substring(matcher.start(), matcher.end());
      path = path.replaceAll("\\[[a-zA-Z0-9 \\-=_%]+\\]", "[]");
      if (path.startsWith("$.application.")) {
        uniquePaths.add(path);
      }
    }
  }

}
