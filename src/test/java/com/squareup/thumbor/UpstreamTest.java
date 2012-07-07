package com.squareup.thumbor;

import org.junit.Test;

import static com.squareup.thumbor.Thumbor.brightness;
import static com.squareup.thumbor.Thumbor.contrast;
import static com.squareup.thumbor.Thumbor.image;
import static org.junit.Assert.assertEquals;

/**
 * Tests defined on the upstream client project as valid.
 */
public class UpstreamTest {

  private Thumbor url() {
    return image("my.server.com/some/path/to/image.jpg").key("my-security-key");
  }

  @Test public void matchingSignature() {
    String expected = "/8ammJH8D-7tXy6kU3lTvoXlhu4o=/300x200/my.server.com/some/path/to/image.jpg";
    String actual = url().resize(300, 200).toUrl();
    assertEquals(expected, actual);
  }

  @Test public void matchingSignatureWithMeta() {
    String expected = "/Ps3ORJDqxlSQ8y00T29GdNAh2CY=/meta/my.server.com/some/path/to/image.jpg";
    String actual = url().toMeta();
    assertEquals(expected, actual);
  }

  @Test public void matchingSignatureWithFilters() {
    String expected = "/ZZtPCw-BLYN1g42Kh8xTcRs0Qls=/filters:brightness(10):contrast(20)/my.server.com/some/path/to/image.jpg";
    String actual = url().filter(brightness(10), contrast(20)).toUrl();
    assertEquals(expected, actual);
  }

}
