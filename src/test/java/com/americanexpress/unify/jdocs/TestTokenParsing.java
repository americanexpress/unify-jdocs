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

import com.americanexpress.unify.base.UnifyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/*
 * @author Deepak Arora
 */
public class TestTokenParsing {

  private static Logger logger = LoggerFactory.getLogger(JDocument.class);

  public static void main(String[] args) throws IOException, UnifyException {
    String path = "$.application.applicants[].phones[type=home\\]].number";
    List<Token> tokens = Parser.getTokens(path);
    for (Token t : tokens) {
      if (t.isArray()) {
        String s = "";
        ArrayToken at = (ArrayToken)t;
        s += at.getField() + "[";

        ArrayToken.FilterType ft = at.getFilter().getType();
        if (ft == ArrayToken.FilterType.EMPTY) {
          s += "]";
        }
        else if (ft == ArrayToken.FilterType.INDEX) {
          s += at.getFilter().getIndex() + "]";
        }
        else if (ft == ArrayToken.FilterType.NAME_VALUE) {
          s += at.getFilter().getField() + "=" + at.getFilter().getValue() + "]";
        }
        System.out.println(s);
      }
      else {
        System.out.println(t.getField());
      }
      System.out.println();
    }
  }

}
