// Copyright 2013 Square, Inc.
package com.squareup.pollexor;

/** Exception denoting that a fatal error occurred while assembling a URL. */
public class UnableToBuildException extends RuntimeException {
  public UnableToBuildException(String reason) {
    super(reason);
  }

  public UnableToBuildException(Throwable e) {
    super(e);
  }
}
