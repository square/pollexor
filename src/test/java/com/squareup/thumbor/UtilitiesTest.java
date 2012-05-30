// Copyright 2012 Square, Inc.
package com.squareup.thumbor;

import org.junit.Test;

import static com.squareup.thumbor.Utilities.base64Encode;
import static com.squareup.thumbor.Utilities.md5;
import static com.squareup.thumbor.Utilities.normalizeString;
import static com.squareup.thumbor.Utilities.rightPadString;
import static com.squareup.thumbor.Utilities.stripProtocolAndParams;
import static org.junit.Assert.assertEquals;

public class UtilitiesTest {
  @Test public void testProtocolAndParamStrip() {
    assertEquals("hi.com", stripProtocolAndParams("http://hi.com"));
    assertEquals("hi.com", stripProtocolAndParams("https://hi.com"));
    assertEquals("hi.com", stripProtocolAndParams("hi.com?whatup"));
    assertEquals("hi.com/hi.html", stripProtocolAndParams("http://hi.com/hi.html"));
    assertEquals("hi.com/hi.html", stripProtocolAndParams("https://hi.com/hi.html"));
    assertEquals("hi.com/hi.html", stripProtocolAndParams("hi.com/hi.html?whatup"));
    assertEquals("hi.com/hi.html", stripProtocolAndParams("http://hi.com/hi.html?whatup"));
    assertEquals("hi.com/hi.html", stripProtocolAndParams("https://hi.com/hi.html?whatup"));
    assertEquals("hi.com/hi.html", stripProtocolAndParams("http://hi.com/hi.html?http://whatever.com"));
    assertEquals("hi.com/http://whatever.com", stripProtocolAndParams("http://hi.com/http://whatever.com"));
    assertEquals("hi.com/http://whatever.com", stripProtocolAndParams("http://hi.com/http://whatever.com?whatup"));
  }

  @Test public void testKeyNormalization() {
    assertEquals("oneoneoneo", normalizeString("one", 10));
    assertEquals("equaltoten", normalizeString("equaltoten", 10));
    assertEquals("reallylong", normalizeString("reallylongstring", 10));
  }

  @Test public void testMd5() {
    assertEquals("5a105e8b9d40e1329780d62ea2265d8a", md5("test1"));
    assertEquals("ad0234829205b9033196ba818f7a872b", md5("test2"));
    assertEquals("8ad8757baa8564dc136c1e07507f4a98", md5("test3"));
  }

  @Test public void testBase64() {
    assertEquals("dGVzdA==", base64Encode("test".getBytes()));
    assertEquals("dGhpcyBpcyBhIHJlYWxseSBsb25nIHN0cmluZw==", base64Encode("this is a really long string".getBytes()));
  }

  @Test public void testPadString() {
    StringBuilder b1 = new StringBuilder("abc");
    rightPadString(b1, 'X', 3);
    assertEquals("abc", b1.toString());

    StringBuilder b2 = new StringBuilder("abcde");
    rightPadString(b2, 'X', 6);
    assertEquals("abcdeX", b2.toString());

    StringBuilder b3 = new StringBuilder("a");
    rightPadString(b3, 'X', 6);
    assertEquals("aXXXXX", b3.toString());

    StringBuilder b4 = new StringBuilder("abcdef");
    rightPadString(b4, 'X', 16);
    assertEquals("abcdefXXXXXXXXXX", b4.toString());
  }
}
