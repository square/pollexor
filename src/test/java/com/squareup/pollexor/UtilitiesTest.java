// Copyright 2012 Square, Inc.
package com.squareup.pollexor;

import org.junit.Test;

import static com.squareup.pollexor.Utilities.base64Encode;
import static com.squareup.pollexor.Utilities.md5;
import static com.squareup.pollexor.Utilities.normalizeString;
import static com.squareup.pollexor.Utilities.rightPadString;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

public class UtilitiesTest {
  @Test public void testKeyNormalization() {
    assertThat(normalizeString("one", 10)).isEqualTo("oneoneoneo");
    assertThat(normalizeString("equaltoten", 10)).isEqualTo("equaltoten");
    assertThat(normalizeString("reallylongstring", 10)).isEqualTo("reallylong");
  }

  @Test public void testKeyNormalizationInvalidInputs() {
    try {
      normalizeString(null, 2);
      fail("String normalization allowed invalid input.");
    } catch (IllegalArgumentException expected) {
    }
    try {
      normalizeString("", 2);
      fail("String normalization allowed invalid input.");
    } catch (IllegalArgumentException expected) {
    }
    try {
      normalizeString("Hi", 0);
      fail("String normalization allowed invalid input.");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test public void testMd5() {
    assertThat(md5("test1")).isEqualTo("5a105e8b9d40e1329780d62ea2265d8a");
    assertThat(md5("test2")).isEqualTo("ad0234829205b9033196ba818f7a872b");
    assertThat(md5("test3")).isEqualTo("8ad8757baa8564dc136c1e07507f4a98");
  }

  @Test public void testMd5InvalidInputs() {
    try {
      md5(null);
      fail("MD5 allowed invalid input.");
    } catch (IllegalArgumentException expected) {
    }
    try {
      md5("");
      fail("MD5 allowed invalid input.");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test public void testBase64() {
    assertThat(base64Encode(new byte[0])).isEqualTo("");
    assertThat(base64Encode("test".getBytes())).isEqualTo("dGVzdA==");
    assertThat(base64Encode("this is a really long string".getBytes()))
        .isEqualTo("dGhpcyBpcyBhIHJlYWxseSBsb25nIHN0cmluZw==");
  }

  @Test public void testBase64InvalidInputs() {
    try {
      base64Encode(null);
      fail("Base 64 encoding allowed invalid input.");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test public void testPadString() {
    StringBuilder b1 = new StringBuilder("abc");
    rightPadString(b1, 'X', 3);
    assertThat(b1.toString()).isEqualTo("abc");

    StringBuilder b2 = new StringBuilder("abcde");
    rightPadString(b2, 'X', 6);
    assertThat(b2.toString()).isEqualTo("abcdeX");

    StringBuilder b3 = new StringBuilder("a");
    rightPadString(b3, 'X', 6);
    assertThat(b3.toString()).isEqualTo("aXXXXX");

    StringBuilder b4 = new StringBuilder("abcdef");
    rightPadString(b4, 'X', 16);
    assertThat(b4.toString()).isEqualTo("abcdefXXXXXXXXXX");

    StringBuilder b5 = new StringBuilder("");
    rightPadString(b5, 'X', 2);
    assertThat(b5.toString()).isEqualTo("");
  }

  @Test public void testPadStringInvalidInputs() {
    try {
      rightPadString(null, ' ', 30);
      fail("String pad allowed invalid input.");
    } catch (IllegalArgumentException expected) {
    }
    try {
      rightPadString(new StringBuilder("hi"), ' ', 0);
      fail("String pad allowed invalid input.");
    } catch (IllegalArgumentException expected) {
    }
    try {
      rightPadString(new StringBuilder("hi"), ' ', 1);
      fail("String pad allowed invalid input.");
    } catch (IllegalArgumentException expected) {
    }
  }
}
