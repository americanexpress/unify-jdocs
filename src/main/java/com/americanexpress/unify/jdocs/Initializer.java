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
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;

import static com.fasterxml.jackson.core.StreamReadConstraints.DEFAULT_MAX_STRING_LEN;

public class Initializer {
  private Boolean allowComments = null;
  private Boolean stripTrailingBigdecimalZeroes = null;
  private Integer maxStringLength = null;

  public Initializer() {
    // nothing to do
  }

  public Initializer allowComments(boolean allowComments) {
    this.allowComments = allowComments;
    return this;
  }

  public Initializer stripTrailingBigDecimalZeroes(boolean stripTrailingBigdecimalZeroes) {
    this.stripTrailingBigdecimalZeroes = stripTrailingBigdecimalZeroes;
    return this;
  }

  public Initializer maxStringLength(int maxStringLength) {
    this.maxStringLength = maxStringLength;
    return this;
  }

  void initialize() {
    if (JDocument.isInitialized == true) {
      throw new UnifyException("jdoc_err_82");
    }

    if (allowComments == null) {
      // default value
      JDocument.allowComments = true;
    }
    else {
      JDocument.allowComments = allowComments;
    }

    if (stripTrailingBigdecimalZeroes == null) {
      // default value
      JDocument.stripTrailingBigdecimalZeroes = false;
    }
    else {
      JDocument.stripTrailingBigdecimalZeroes = stripTrailingBigdecimalZeroes;
    }

    if (maxStringLength == null) {
      // default value
      JDocument.maxStringLength = DEFAULT_MAX_STRING_LEN;
    }
    else {
      JDocument.maxStringLength = maxStringLength;
    }

    if (JDocument.objectMapper == null) {
      JDocument.objectMapper = new ObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, JDocument.allowComments)
              .configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, JDocument.stripTrailingBigdecimalZeroes);
      JsonFactory jsonFactory = JDocument.objectMapper.getFactory();
      StreamReadConstraints src = StreamReadConstraints.builder().maxStringLength(JDocument.maxStringLength).build();
      jsonFactory.setStreamReadConstraints(src);
    }
    else {
      throw new UnifyException("jdoc_err_83");
    }

    // we need to initialize the runtime defaults
    JDocument.defaultValidationType = CONSTS_JDOCS.VALIDATION_TYPE.ONLY_MODEL_PATHS;
    JDocument.docTypePrefixPolicy = new DocTypePrefixPolicyEnforceForAll();
    JDocument.lineFeed = "\n";
    JDocument.objectWriter = JDocument.objectMapper.writer(new DefaultPrettyPrinter().withObjectIndenter(new DefaultIndenter().withLinefeed(JDocument.lineFeed)));
    JDocument.ignoreDocTypePrefixForBaseDocs = false;
    JDocument.deleteEmptyObject = false;
    JDocument.deleteEmptyArray = false;

    JDocument.isInitialized = true;
  }

}
