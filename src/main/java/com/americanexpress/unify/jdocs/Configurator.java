/*
 * Copyright 2025 American Express Travel Related Services Company, Inc.
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

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class Configurator {

  private CONSTS_JDOCS.VALIDATION_TYPE defaultValidationType = null;
  private String lineFeed = null;
  private DocTypePrefixPolicy docTypePrefixPolicy = null;
  private Boolean ignoreDocTypePrefixForBaseDocs = null;
  private Boolean deleteEmptyObject = null;
  private Boolean deleteEmptyArray = null;

  public Configurator() {
    // nothing to do
  }

  public Configurator defaultValidationPolicy(CONSTS_JDOCS.VALIDATION_TYPE defaultValidationType) {
    this.defaultValidationType = defaultValidationType;
    return this;
  }

  public Configurator lineFeed(String lineFeed) {
    this.lineFeed = lineFeed;
    return this;
  }

  public Configurator docTypePrefixPolicy(DocTypePrefixPolicy policy) {
    this.docTypePrefixPolicy = policy;
    return this;
  }

  public Configurator ignoreDocTypePrefixForBaseDocs(boolean ignoreDocTypePrefixForBaseDocs) {
    this.ignoreDocTypePrefixForBaseDocs = ignoreDocTypePrefixForBaseDocs;
    return this;
  }

  public Configurator deleteEmptyObject(boolean deleteEmptyObject) {
    this.deleteEmptyObject = deleteEmptyObject;
    return this;
  }

  public Configurator deleteEmptyArray(boolean deleteEmptyArray) {
    this.deleteEmptyArray = deleteEmptyArray;
    return this;
  }

  void configure() {
    // set the static values and check as we go along
    if (defaultValidationType != null) {
      JDocument.defaultValidationType = defaultValidationType;
    }

    if (docTypePrefixPolicy != null) {
      JDocument.docTypePrefixPolicy = docTypePrefixPolicy;
    }

    if (lineFeed != null) {
      JDocument.lineFeed = lineFeed;
      JDocument.objectWriter = JDocument.objectMapper.writer(new DefaultPrettyPrinter().withObjectIndenter(new DefaultIndenter().withLinefeed(JDocument.lineFeed)));
    }

    if (ignoreDocTypePrefixForBaseDocs != null) {
      JDocument.ignoreDocTypePrefixForBaseDocs = ignoreDocTypePrefixForBaseDocs;
    }

    if (deleteEmptyObject != null) {
      JDocument.deleteEmptyObject = deleteEmptyObject;
    }

    if (deleteEmptyArray != null) {
      JDocument.deleteEmptyArray = deleteEmptyArray;
    }
  }

}
