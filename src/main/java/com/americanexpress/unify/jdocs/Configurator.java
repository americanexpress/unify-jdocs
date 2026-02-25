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

import com.americanexpress.unify.base.UnifyException;
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
    if (defaultValidationType == null) {
      if (JDocument.defaultValidationType == null) {
        // default value
        JDocument.defaultValidationType = CONSTS_JDOCS.VALIDATION_TYPE.ONLY_MODEL_PATHS;
      }
      else {
        // leave the value as it is and so nothing to do
      }
    }
    else {
      JDocument.defaultValidationType = defaultValidationType;
    }

    if (docTypePrefixPolicy == null) {
      if (JDocument.docTypePrefixPolicy == null) {
        // default value
        JDocument.docTypePrefixPolicy = new DocTypePrefixPolicyEnforceForAll();
      }
      else {
        // leave the value as it is and so nothing to do
      }
    }
    else {
      JDocument.docTypePrefixPolicy = docTypePrefixPolicy;
    }

    {
      if (lineFeed == null) {
        if (JDocument.lineFeed == null) {
          // default value
          JDocument.lineFeed = "\n";
        }
        else {
          // leave the value as it is and so nothing to do
        }
      }
      else {
        if (JDocument.lineFeed == null) {
          JDocument.lineFeed = lineFeed;
        }
        else {
          if (lineFeed.equals(JDocument.lineFeed) == false) {
            throw new UnifyException("jdoc_err_82");
          }
          else {
            // do nothing
          }
        }
      }
      JDocument.objectWriter = JDocument.objectMapper.writer(new DefaultPrettyPrinter().withObjectIndenter(new DefaultIndenter().withLinefeed(JDocument.lineFeed)));
    }

    if (ignoreDocTypePrefixForBaseDocs == null) {
      if (JDocument.ignoreDocTypePrefixForBaseDocs == null) {
        // default value
        JDocument.ignoreDocTypePrefixForBaseDocs = false;
      }
      else {
        // leave the value as it is and so nothing to do
      }
    }
    else {
      JDocument.ignoreDocTypePrefixForBaseDocs = ignoreDocTypePrefixForBaseDocs;
    }

    if (deleteEmptyObject == null) {
      if (JDocument.deleteEmptyObject == null) {
        // default value
        JDocument.deleteEmptyObject = false;
      }
      else {
        // leave the value as it is and so nothing to do
      }
    }
    else {
      JDocument.deleteEmptyObject = deleteEmptyObject;
    }

    if (deleteEmptyArray == null) {
      if (JDocument.deleteEmptyArray == null) {
        // default value
        JDocument.deleteEmptyArray = false;
      }
      else {
        // leave the value as it is and so nothing to do
      }
    }
    else {
      JDocument.deleteEmptyArray = deleteEmptyArray;
    }
  }

}
