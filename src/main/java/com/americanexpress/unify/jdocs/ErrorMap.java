package com.americanexpress.unify.jdocs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Deepak Arora
 */
public class ErrorMap {

  private static final Logger logger = LogManager.getLogger(ErrorMap.class);

  protected static Map<String, String> errors = new ConcurrentHashMap<>();

  public static String getErrorMessage(String code) {
    String s = errors.get(code);
    s = (s == null) ? "" : s;
    return s;
  }

}
