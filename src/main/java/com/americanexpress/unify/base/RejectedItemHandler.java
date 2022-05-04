package com.americanexpress.unify.base;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/*
 * @author Deepak Arora
 */
public class RejectedItemHandler implements RejectedExecutionHandler {

  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    r.run();
  }

}
