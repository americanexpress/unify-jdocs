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

    private FORMAT_FIELDS() {
      // nothing to do
    }

    public static final String KEY = "jdocs_arr_pk";
    public static final String TYPE = "type";
    public static final String REGEX = "regex";
    public static final String FORMAT = "format"; // used only for date
    public static final String NULL_ALLOWED = "null_allowed";
    public static final String IGNORE_REGEX_IF_EMPTY_STRING = "ignore_regex_if_empty_string";
    public static final String EMPTY_DATE_ALLOWED = "empty_date_allowed";
    public static final String MIN_LENGTH = "min_length";
    public static final String MAX_LENGTH = "max_length";
    public static final String MIN_VALUE = "min_value";
    public static final String MAX_VALUE = "max_value";
    public static final String MIN_DATE = "min_date";
    public static final String MAX_DATE = "max_date";
    public static final String CREATED_PATH="created_path";
    public static final String PATH_DOES_NOT_EXIST="path_does_not_exist";
    public static final String MODIFIED_PATH="modified_path";


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
    CONTENT
  }

  public enum VALIDATION_TYPE {
    ALL_DATA_PATHS,
    ONLY_MODEL_PATHS,
    ONLY_AT_READ_WRITE
  }

}
