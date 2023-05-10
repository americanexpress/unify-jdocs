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

import java.util.ArrayList;
import java.util.List;

import static com.americanexpress.unify.base.BaseUtils.removeEscapeChars;

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
