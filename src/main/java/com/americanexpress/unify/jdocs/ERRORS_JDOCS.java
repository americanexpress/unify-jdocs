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

import com.americanexpress.unify.base.ErrorMap;

import java.util.Map;

/*
 * @author Deepak Arora
 */
public class ERRORS_JDOCS extends ErrorMap {

  public static void load() {
    Map<String, String> map = errors;
    map.put("jdoc_err_1", "Unexpected exception");
    map.put("jdoc_err_2", "Merge functionality can only be run on typed documents");
    map.put("jdoc_err_3", "This function is not applicable for non array type object");
    map.put("jdoc_err_4", "This function is not applicable for array index type object");
    map.put("jdoc_err_5", "The leaf node is not of the type array in the path -> {0}");
    map.put("jdoc_err_6", "Node at specified path is not an array node. Path -> {0}");
    map.put("jdoc_err_7", "Path mismatch. Path specifies array but the field in document is not. Field -> {0}");
    map.put("jdoc_err_8", "Array index out of bounds. Field -> {0}");
    map.put("jdoc_err_9", "Invalid filter type -> {0}");
    map.put("jdoc_err_10", "Invalid path expression. Non definite array node not allowed in path. Node -> {0}");
    map.put("jdoc_err_11", "Invalid path expression. Non leaf array node cannot be indefinite. Node -> {0}");
    map.put("jdoc_err_12", "Internal error. Non existent path access type. Type -> {0}");
    map.put("jdoc_err_13", "Mismatch in field type between calling function and data in doucment at path -> {0}");
    map.put("jdoc_err_14", "Invalid path expression. Leaf of path is not a value node. Path -> {0}");
    map.put("jdoc_err_15", "Invalid class specification. Clazz -> {0}");
    map.put("jdoc_err_16", "Array index out of bounds for leaf node");
    map.put("jdoc_err_17", "Array index out of bounds -> {0}");
    map.put("jdoc_err_18", "Invalid path expression. Filter field is not a value node -> {0}");
    map.put("jdoc_err_19", "Cannot write into a read only document");
    map.put("jdoc_err_20", "Cannot set null value at path -> {0}");
    map.put("jdoc_err_21", "No object found at specified path -> {0}");
    map.put("jdoc_err_22", "Source path specifed does not evaluate to an object / array node -> {0}");
    map.put("jdoc_err_23", "Target path specifed does not evaluate to an object / array node -> {0}");
    map.put("jdoc_err_24", "Source and target paths are not the same types");
    map.put("jdoc_err_25", "Leaf node in path is not an array. Path -> {0}");
    map.put("jdoc_err_26", "Internal error. Should never reach here");
    map.put("jdoc_err_27", "Invalid path expression. Closing ] not found in token {0}");
    map.put("jdoc_err_28", "{0}");
    map.put("jdoc_err_29", "Document model not found for {0}");
    map.put("jdoc_err_30", "Invalid path -> {0}");
    map.put("jdoc_err_31", "Merge functionality can only be run on typed documents");
    map.put("jdoc_err_32", "Key field not defined in the model for array -> {0}");
    map.put("jdoc_err_33", "No key field defined in the source document for array -> {0}");
    map.put("jdoc_err_34", "No key field defined in the target document for array -> {0}");
    map.put("jdoc_err_35", "Cannot merge. Mismatch in source and target node types. Node name -> {0}");
    map.put("jdoc_err_36", "Null value is not allowed at this path -> {0}");
    map.put("jdoc_err_37", "Field type mismatch between model and data for path -> {0}");
    map.put("jdoc_err_38", "Invalid path. Path does not exist in document model. Type -> {0}, path -> {1}");
    map.put("jdoc_err_39", "Invalid source path. Last character of the path cannot be . Path -> {0}");
    map.put("jdoc_err_40", "Invalid destination path. Last character of the path cannot be . Path -> {0}");
    map.put("jdoc_err_41", "Invalid source path. Path does not exist in source document. Path -> {0}");
    map.put("jdoc_err_42", "Invalid path value in document model -> {0} at path -> {1}");
    map.put("jdoc_err_43", "Invalid number value in from field -> {0}");
    map.put("jdoc_err_44", "Invalid number value in from field, path -> {0}, value -> {1}");
    map.put("jdoc_err_45", "Mismatch in document field value and filter value. Field -> {0}, field value -> {1}, filter value -> {2}");
    map.put("jdoc_err_46", "Unhandled node type encountered. Field -> {0}, node type -> {1}");
    map.put("jdoc_err_47", "Invalid path syntax. Leaf node is not an empty array in path -> {0}");
    map.put("jdoc_err_48", "Invalid path syntax. Leaf node is not a name value array in path -> {0}");
    map.put("jdoc_err_49", "Invalid path syntax. Leaf node cannot be an array in path -> {0}");
    map.put("jdoc_err_50", "Invalid path syntax. Leaf node is not a definite index array in path -> {0}");
    map.put("jdoc_err_51", "Invalid date syntax for path -> {0}");
    map.put("jdoc_err_52", "Date Format should be specified -> {0}");
    map.put("jdoc_err_53", "Incompatible data type for value of array filter field -> {0} in path -> {1}");
    map.put("jdoc_err_54", "Regex pattern mismatch in path -> {0}");
    map.put("jdoc_err_55", "Types of two documents do not match");
    map.put("jdoc_err_56", "Document type is either null or empty");
    map.put("jdoc_err_60", "This method is only applicable for typed documents");
    map.put("jdoc_err_61", "Type value not found for model path {0}");
    map.put("jdoc_err_62", "Invalid value encountered in path -> {0}");
    map.put("jdoc_err_63", "Error encountered while loading format string. Type -> {0}, path -> {1}, format string -> {2}");
    map.put("jdoc_err_64", "Key field node is not a value node in the array -> {0}");
    map.put("jdoc_err_65", "Path not found in document -> {0}");
  }

}
