package com.americanexpress.unify.base;

import java.util.Map;

/**
 * @author Deepak Arora
 */
public class ERRORS_BASE extends ErrorMap {

  public static void load() {
    Map<String, String> map = errors;
    map.put("base_err_1", "Unexpected exception");
    map.put("base_err_4", "Unexpected Interrupted Exception");
    map.put("base_err_14", "Cannot create Instant");
    map.put("base_err_3", "IOException while reading from file");
  }

}
