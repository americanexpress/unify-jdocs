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

/*
 * @author Deepak Arora
 */
public class CONSTS_JDOCS {

  /**
   *
   */
  public static final String NEW_LINE = System.getProperty("line.separator");

  // these are the fields that appear in the format string i.e. the whole string for a leaf node
  public class FORMAT_FIELDS {

    public static final String KEY = "key";
    public static final String TYPE = "type";
    public static final String REGEX = "regex";
    public static final String FORMAT = "format"; // used only for date
    public static final String NULL_ALLOWED = "null_allowed";

  }

  // these are the fields that appear in the type tag of the format string
  public class TYPES {

    public static final String STRING = "string";
    public static final String DATE = "date";
    public static final String BOOLEAN = "boolean";
    public static final String INTEGER = "integer";
    public static final String LONG = "long";
    public static final String DOUBLE = "double";

  }

  public enum API {
    DELETE_PATH,
    GET_ARRAY_SIZE,
    GET_ARRAY_INDEX,
    GET,
    SET,
    GET_ARRAY_VALUE,
    SET_ARRAY_VALUE,
    PATH_EXISTS,
    SET_CONTENT
  }

}
