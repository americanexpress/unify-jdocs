package com.americanexpress.unify.jdocs;

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
    String s = "Error code -> " + errorCode + CONSTS_JDOCS.NEW_LINE;
    s = s + "Error message -> " + errorMessage + CONSTS_JDOCS.NEW_LINE;
    s = s + "Error details -> " + errorDetails + CONSTS_JDOCS.NEW_LINE;
    return s;
  }

}
