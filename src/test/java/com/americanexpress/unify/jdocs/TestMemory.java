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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/*
 * @author Deepak Arora
 */

/**
 * @author daror20
 */
public class TestMemory {

  private static Logger logger = LoggerFactory.getLogger(JDocument.class);

  /**
   * @param args
   * @throws IOException
   * @throws UnifyException
   */
  public static void main(String[] args) throws IOException, UnifyException {
    setDocModel("application");
    testMemory();
  }

  private static void setDocModel(String type) {
    String json = BaseUtils.getResourceAsString(TestMemory.class, "/jdocs/" + type + ".json");
    Document d = new JDocument(json);
    JDocument.setDocumentModel("application", d);
  }

  private static void testMemory() throws IOException {
    String json = BaseUtils.getResourceAsString(TestMemory.class, "/jdocs/app_fragment.json");
    Document[] docs = new Document[100000];

    System.out.println("Start...");

    long mem = Runtime.getRuntime().freeMemory();

    for (int i = 0; i < 10000; i++) {
      docs[i] = new JDocument("application", json);
    }

    System.out.println(mem - Runtime.getRuntime().freeMemory());

  }

  private static void testMemory1() throws IOException {
    String json = BaseUtils.getResourceAsString(TestMemory.class, "/jdocs/app_fragment.json");

    String[] docs = new String[100000];

    System.out.println("Start...");

    long mem = Runtime.getRuntime().freeMemory();

    for (int i = 0; i < 10000; i++) {
      docs[i] = new String(json);
    }

    System.out.println(mem - Runtime.getRuntime().freeMemory());

  }

}
