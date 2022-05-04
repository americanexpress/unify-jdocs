package com.americanexpress.unify.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Deepak Arora
 */
public class ErrorMap {

  private static final Logger logger = LoggerFactory.getLogger(ErrorMap.class);

  protected static Map<String, String> errors = new ConcurrentHashMap<>();

  public static final String getErrorMessage(String code) {
    String s = errors.get(code);
    s = (s == null) ? "" : s;
    return s;
  }

}
