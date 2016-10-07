// Copyright 2013 Square, Inc.
package com.squareup.pollexor;

/**
 * Representation of a remote <a href="https://github.com/globocom/thumbor">thumbor</a>
 * installation.
 */
public final class Thumbor {
  /**
   * Create a new instance for the specified host.
   *
   * @see #create(String, String)
   */
  public static Thumbor create(String host) {
    return new Thumbor(host, null);
  }

  /**
   * Create a new instance for the specified host and encryption key.
   *
   * @see #create(String)
   */
  public static Thumbor create(String host, String key) {
    if (key == null || key.length() == 0) {
      throw new IllegalArgumentException("Key must not be blank.");
    }
    return new Thumbor(host, key);
  }

  private final String host;
  private final String key;

  private Thumbor(String host, String key) {
    if (host == null || host.length() == 0) {
      throw new IllegalArgumentException("Host must not be blank.");
    }
    if (!host.endsWith("/")) {
      host += "/";
    }
    this.host = host;
    this.key = key;
  }

  public String getHost() {
    return host;
  }

  public String getKey() {
    return key;
  }

  /** Begin building a url for this host with the specified image. */
  public ThumborUrlBuilder buildImage(String image) {
    if (image == null || image.length() == 0) {
      throw new IllegalArgumentException("Image must not be blank.");
    }
    return new ThumborUrlBuilder(host, key, image);
  }
}
