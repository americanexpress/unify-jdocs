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

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;

import static com.americanexpress.unify.jdocs.JDocument.objectMapper;

/*
 * @author Deepak Arora
 */

public class TestParsing {

  private static Logger logger = LoggerFactory.getLogger(TestParsing.class);

  /**
   * @param args
   * @throws IOException
   * @throws UnifyException
   */
  public static void main(String[] args) throws IOException, UnifyException {
    logger.info(parseDocModel("model_response"));
  }

  private static String parseDocModel(String type) throws IOException {
    String json = BaseUtils.getResourceAsString(TestParsing.class, "/jdocs/" + type + ".json");
    return insertReferredModels(json);
  }

  private static String insertReferredModels(String json) throws IOException {
    while (true) {
      StringBuilder sb = new StringBuilder(1024);
      Scanner scanner = new Scanner(json);
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

          String contents = BaseUtils.getResourceAsString(TestParsing.class, rfn);
          contents = contents.trim();
          sb.append(contents.substring(1, contents.length() - 1));

          if (line.charAt(line.length() - 1) == ',') {
            sb.append(',');
          }
        }
        else {
          sb.append(line);
        }
      }

      scanner.close();

      json = sb.toString();

      if (isReferred == false) {
        break;
      }
    }

    JsonNode node = objectMapper.readTree(json);
    json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    return json;
  }

}
