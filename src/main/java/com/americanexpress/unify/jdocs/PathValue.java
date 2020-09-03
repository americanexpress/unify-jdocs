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
public class PathValue {

  private String path;
  private Object value;
  private DataType dataType;

  public PathValue(String path, Object value, DataType dataType) {
    this.path = path;
    this.value = value;
    this.dataType = dataType;
  }

  public String getPath() {
    return path;
  }

  public Object getValue() {
    return value;
  }

  public DataType getDataType() {
    return dataType;
  }

}
