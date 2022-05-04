package com.americanexpress.unify.base;

/*
 * @author Deepak Arora
 */
public class UnifyBase {

  public static void init() {
    ERRORS_BASE.load();
  }

  public static void initTest() {
    ERRORS_BASE.load();
  }

  public static void close() {
    // nothing to do
  }

  public static void closeTest() {
    // nothing to do
  }

}
