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

package com.aexp.acq.unify.jdocs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * @author Deepak Arora
 */
public class LocAnalysis {

  private static String fileExtension = null;
  private static long blankLines = 0;
  private static long codeLines = 0;
  private static long commentLines = 0;
  private static boolean isCommentBlock = false;

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Please specify two arguments as below:");
      System.out.println("Argument 1 -> path to the base directory under which all files matching the pattern are to be searched recursively e.g. C:/Source. Note no trailing slash");
      System.out.println("Argument 2 -> the file extension to look for under the directory e.g. .java");
      System.out.println("This program outputs the number of blank lines and number of code lines in Java files");
      System.exit(1);
    }

    getLoc(args[0], args[1]);

    System.out.println("Blank lines -> " + blankLines);
    System.out.println("Comment lines -> " + commentLines);
    System.out.println("Code lines -> " + codeLines);
  }

  private static void getLoc(String baseDirPath, String filePattern) {
    try (Stream<Path> walk = Files.walk(Paths.get(baseDirPath))) {
      List<String> result = walk.map(x -> x.toString()).filter(f -> f.endsWith(filePattern)).collect(Collectors.toList());
      result.forEach(s -> {
        try {
          System.out.println("Processing file -> " + s);
          Stream<String> lines = Files.lines(Paths.get(s));
          lines.forEach(line -> checkLine(line));
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

  private static void checkLine(String line) {
    line = line.trim();

    if (line.startsWith("/*")) {
      isCommentBlock = true;
      commentLines++;
      return;
    }

    if (isCommentBlock == true) {
      if (line.endsWith("*/")) {
        isCommentBlock = false;
      }
      commentLines++;
    }

    if (line.isEmpty() == true) {
      blankLines++;
      return;
    }

    codeLines++;
  }

}
