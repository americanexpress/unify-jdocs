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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Deepak Arora
 */
public class ErrorTuple {

  private static Logger logger = LoggerFactory.getLogger(ErrorTuple.class);

  private String errorCode = "";
  private String errorMessage = "";
  private String errorDetails = "";
  private boolean isRetryable = false;

  public ErrorTuple() {
  }

  public ErrorTuple(String code, String message) {
    this.errorCode = code;
    this.errorMessage = message;
  }

  public ErrorTuple(String errorCode, Exception e) {
    set(errorCode, e);
  }

  public ErrorTuple(Exception e) {
    set("", e);
  }

  private void set(String errorCode, Exception e) {
    if (e instanceof UnifyException) {
      UnifyException ue = (UnifyException)e;
      ErrorTuple et = ue.getErrorTuple();
      this.errorCode = BaseUtils.getEmptyWhenNull(et.getErrorCode());
      errorMessage = BaseUtils.getEmptyWhenNull(et.getErrorMessage());
      errorDetails = BaseUtils.getEmptyWhenNull(et.getErrorDetails());
      isRetryable = et.isRetryable();
    }
    else {
      this.errorCode = BaseUtils.getEmptyWhenNull(errorCode);
      errorMessage = BaseUtils.getEmptyWhenNull(e.getMessage());
    }
  }

  public void setRetryable(boolean retryable) {
    this.isRetryable = retryable;
  }

  public boolean isRetryable() {
    return isRetryable;
  }

  public void setErrorDetails(String errorDetails) {
    if (errorDetails == null) {
      errorDetails = "";
    }
    this.errorDetails = errorDetails;
  }

  public String getErrorDetails() {
    return errorDetails;
  }

  /**
   * @return the errorCode
   */
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * @param errorCode the errorCode to set
   */
  public void setErrorCode(String errorCode) {
    if (errorCode == null) {
      errorCode = "";
    }
    this.errorCode = errorCode;
  }

  /**
   * @return the errorMessage
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * errorCode
   *
   * @param errorMessage the errorMessage to set
   */
  public void setErrorMessage(String errorMessage) {
    if (errorMessage == null) {
      errorMessage = "";
    }
    this.errorMessage = errorMessage;
  }

  public String getErrorString() {
    String s = "Error code -> " + errorCode + CONSTS_BASE.NEW_LINE;
    s = s + "Error message -> " + errorMessage + CONSTS_BASE.NEW_LINE;
    s = s + "Error details -> " + errorDetails + CONSTS_BASE.NEW_LINE;
    return s;
  }

}
