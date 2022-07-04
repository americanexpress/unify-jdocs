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

package com.americanexpress.unify.base;

import java.util.Map;

/**
 * @author Deepak Arora
 */
public class ERRORS_BASE extends ErrorMap {

  public static void load() {
    Map<String, String> map = errors;
    map.put("base_err_1", "Unexpected exception");
    map.put("base_err_4", "Unexpected Interrupted Exception");
    map.put("base_err_14", "Cannot create Instant");
    map.put("base_err_3", "IOException while reading from file");
  }

}
