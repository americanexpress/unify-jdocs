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


import com.americanexpress.unify.base.UnifyException;

import java.math.BigDecimal;
import java.util.List;

/*
 * @author Deepak Arora
 */
public class ReadOnlyDocument implements Document {

  private Document d = null;

  public ReadOnlyDocument(Document d) {
    this.d = d;
  }

  @Override
  public void deletePath(String path, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void deletePaths(List<String> pathsToDelete) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public boolean isTyped() {
    return d.isTyped();
  }

  @Override
  public String getType() {
    return d.getType();
  }

  @Override
  public DataType getLeafNodeDataType(String path, String... vargs) {
    return d.getLeafNodeDataType(path, vargs);
  }

  @Override
  public DataType getArrayValueLeafNodeDataType(String path, String... vargs) {
    return d.getArrayValueLeafNodeDataType(path, vargs);
  }

  @Override
  public void setType(String type) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  @Deprecated
  public void setType(String type, boolean validateOnReadWriteOnly) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void setType(String type, CONSTS_JDOCS.VALIDATION_TYPE validationType) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void empty() {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public int getArraySize(String path, String... vargs) {
    return d.getArraySize(path, vargs);
  }

  @Override
  public int getArrayIndex(String path, String... vargs) {
    return d.getArrayIndex(path, vargs);
  }

  @Override
  public Boolean getBoolean(String path, String... vargs) {
    return d.getBoolean(path, vargs);
  }

  @Override
  public Integer getInteger(String path, String... vargs) {
    return d.getInteger(path, vargs);
  }

  @Override
  public String getJson() {
    return d.getJson();
  }

  @Override
  public Long getLong(String path, String... vargs) {
    return d.getLong(path, vargs);
  }

  @Override
  public BigDecimal getBigDecimal(String path, String... vargs) {
    return d.getBigDecimal(path, vargs);
  }

  @Override
  public Boolean getArrayValueBoolean(String path, String... vargs) {
    return d.getArrayValueBoolean(path, vargs);
  }

  @Override
  public Integer getArrayValueInteger(String path, String... vargs) {
    return d.getArrayValueInteger(path, vargs);
  }

  @Override
  public String getArrayValueString(String path, String... vargs) {
    return d.getArrayValueString(path, vargs);
  }

  @Override
  public Long getArrayValueLong(String path, String... vargs) {
    return d.getArrayValueLong(path, vargs);
  }

  @Override
  public BigDecimal getArrayValueBigDecimal(String path, String... vargs) {
    return d.getArrayValueBigDecimal(path, vargs);
  }

  @Override
  public String getPrettyPrintJson() {
    return d.getPrettyPrintJson();
  }

  @Override
  public Object getValue(String path, String... vargs) {
    return d.getValue(path, vargs);
  }

  @Override
  public Object getArrayValue(String path, String... vargs) {
    return d.getArrayValue(path, vargs);
  }

  @Override
  public String getString(String path, String... vargs) {
    return d.getString(path, vargs);
  }

  @Override
  public boolean pathExists(String path, String... vargs) {
    return d.pathExists(path, vargs);
  }

  @Override
  public boolean isArray(String path, String... vargs) {
    return d.isArray(path, vargs);
  }

  @Override
  public Document getDocument(String path, String... vargs) {
    return d.getDocument(path, vargs);
  }

  @Override
  public Document getContent(String path, boolean returnTypedDocument, boolean includeFullPath, String... vargs) {
    return d.getContent(path, returnTypedDocument, includeFullPath, vargs);
  }

  @Override
  public void setBoolean(String path, boolean value, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void setContent(Document fromDoc, String fromPath, String toPath, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void setInteger(String path, int value, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void setLong(String path, long value, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void setBigDecimal(String path, BigDecimal value, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void setString(String path, String value, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void setArrayValueBoolean(String path, boolean value, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void setArrayValueInteger(String path, int value, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void setArrayValueLong(String path, long value, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void setArrayValueBigDecimal(String path, BigDecimal value, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public void setArrayValueString(String path, String value, String... vargs) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public Document deepCopy() {
    return d.deepCopy();
  }

  @Override
  public void merge(Document d, List<String> pathsToDelete) {
    throw new UnifyException("jdoc_err_19");
  }

  @Override
  public List<String> flatten() {
    return d.flatten();
  }

  @Override
  public List<PathValue> flattenWithValues() {
    return d.flattenWithValues();
  }

  @Override
  public List<DiffInfo> getDifferences(Document right, boolean onlyDifferences) {
    return d.getDifferences(right, onlyDifferences);
  }

  @Override
  public List<DiffInfo> getDifferences(String leftPath, Document right, String rightPath, boolean onlyDifferences) {
    return d.getDifferences(leftPath, right, rightPath, onlyDifferences);
  }

  @Override
  @Deprecated
  public void validate(String type) {
    d.validate(type);
  }

  @Override
  public boolean isLeafNode(String path, String... vargs) {
    return d.isLeafNode(path, vargs);
  }

  @Override
  public CONSTS_JDOCS.VALIDATION_TYPE getValidationType() {
    return null;
  }

  @Override
  public void validateAllPaths(String type) {
    d.validateAllPaths(type);
  }

  @Override
  public void validateModelPaths(String type) {
    d.validateModelPaths(type);
  }

}
