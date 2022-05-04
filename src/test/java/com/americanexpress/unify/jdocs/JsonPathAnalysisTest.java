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
import java.util.List;

/*
 * @author Deepak Arora
 */
public class JsonPathAnalysisTest {

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.out.println("Please specify three arguments as below:");
      System.out.println("Argument 1 -> full path to the json file that whose unique paths need to be searched e.g. C:/Temp/test.json");
      System.out.println("Argument 2 -> path to the base directory under which all files matching the pattern are to be searched recursively e.g. C:/Source. Note no trailing slash");
      System.out.println("Argument 3 -> the file extension to look for under the directory e.g. .java");
      System.out.println("This program looks up the usage of all unique paths in the json file across all files matching the pattern under the base directory and lists out the paths not used");
      System.exit(1);
    }

    // new JsonPathUtils().getUnusedPaths(args[0], args[1], args[2]);
    // new JsonPathUtils().getUsedPaths(args[0], args[1], args[2]);
    getUniquePaths(args[0]);
  }

  private static void getUniquePaths(String filePath) throws IOException {
    List<String> paths = new JsonPathUtils().getUniquePaths(filePath);
    paths.stream().forEach(s -> System.out.println(s));
  }

}
