package com.americanexpress.unify.jdocs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

/**
 * @author daror20
 */
public class UnifyException extends RuntimeException {

  private static Logger logger = LogManager.getLogger(UnifyException.class);

  private ErrorTuple et = new ErrorTuple();
  private Throwable cause = null;

  private String getMessage(String code, String... vargs) {
    String msg = ErrorMap.getErrorMessage(code);
    if (msg == null) {
      logger.error("Error code {} not found in ErrorMap", code);
      msg = "";
    }
    else {
      if (vargs.length != 0) {
        msg = MessageFormat.format(msg, vargs);
      }
    }

    return msg;
  }

  // constructor for creating from an error code
  public UnifyException(String code, String... vargs) {
    this.et.setErrorCode(code);
    this.et.setErrorMessage(getMessage(code, vargs));
    this.et.setErrorDetails("");
  }

  // constructor for creating Unify Exception from an Error Tuple
  public UnifyException(ErrorTuple et) {
    this.et = et;

    String s = et.getErrorMessage();
    if (s.isEmpty()) {
      // we try and get value from error map
      s = ErrorMap.getErrorMessage(et.getErrorCode());
      if (s == null) {
        s = "";
      }
    }
    this.et.setErrorMessage(s);
  }

  // constructor for wrapping up an exception in Unify Exception
  public UnifyException(String code, Throwable cause, String... vargs) {
    super(cause);
    this.et.setErrorCode(code);
    this.et.setErrorMessage(getMessage(code, vargs) + ". Cause -> " + cause.getMessage());
    this.et.setErrorDetails(BaseUtils.getStackTrace(cause, 12));
    this.cause = cause;
  }

  @Override
  public String getMessage() {
    return et.getErrorMessage();
  }

  public String getErrorCode() {
    return et.getErrorCode();
  }

  public String getDetails() {
    return et.getErrorDetails();
  }

  public boolean isRetryable() {
    return et.isRetryable();
  }

  public ErrorTuple getErrorTuple() {
    return et;
  }

  public Throwable getCause() {
    return cause;
  }

}
