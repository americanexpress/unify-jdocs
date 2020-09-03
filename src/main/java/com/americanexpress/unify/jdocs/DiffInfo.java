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
public class DiffInfo {

  private PathDiffResult diffResult;
  private PathValue left;
  private PathValue right;

  public DiffInfo(PathDiffResult diffResult, PathValue left, PathValue right) {
    this.diffResult = diffResult;
    this.left = left;
    this.right = right;
  }

  public PathDiffResult getDiffResult() {
    return diffResult;
  }

  public PathValue getLeft() {
    return left;
  }

  public PathValue getRight() {
    return right;
  }

}
