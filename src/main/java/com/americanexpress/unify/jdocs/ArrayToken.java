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

class ArrayToken extends Token {

  public enum FilterType {

    NAME_VALUE, INDEX, EMPTY

  }

  public class Filter {

    private FilterType type = null;

    private String field = null;

    private String value = null;

    private int index = -1;

    public Filter(String field, String value) {
      this.field = field;
      this.value = value;
      type = FilterType.NAME_VALUE;
    }

    public Filter(int index) {
      this.index = index;
      type = FilterType.INDEX;
    }

    public Filter() {
      type = FilterType.EMPTY;
    }

    public FilterType getType() {
      return type;
    }

    public String getField() {
      return field;
    }

    public String getValue() {
      return value;
    }

    public int getIndex() {
      return index;
    }

  }

  private Filter filter = null;

  public ArrayToken(String name, String field, String value, boolean isLeaf) {
    super(name, isLeaf);
    filter = new Filter(field, value);
  }

  public ArrayToken(String name, int index, boolean isLeaf) {
    super(name, isLeaf);
    filter = new Filter(index);
  }

  public ArrayToken(String name, boolean isLeaf) {
    super(name, isLeaf);
    filter = new Filter();
  }

  @Override
  public boolean isArray() {
    return true;
  }

  public Filter getFilter() {
    return filter;
  }

}
