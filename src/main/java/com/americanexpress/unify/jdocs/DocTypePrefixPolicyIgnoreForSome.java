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

import java.util.HashSet;
import java.util.Set;

public class DocTypePrefixPolicyIgnoreForSome  extends DocTypePrefixPolicy {

  // package protected
  Set<String> types = null;

  public DocTypePrefixPolicyIgnoreForSome(Set<String> types) {
    super();
    if (types == null) {
      throw new UnifyException("jdoc_err_85");
    }
    this.types = new HashSet<>(types);
  }

  public PolicyType getPolicyType() {
    return PolicyType.IGNORE_FOR_SOME;
  }

  public Set<String> getTypes() {
    return types;
  }

}
