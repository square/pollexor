package com.squareup.pollexor;

import org.junit.Test;

import static com.squareup.pollexor.ThumborUrlBuilder.brightness;
import static com.squareup.pollexor.ThumborUrlBuilder.contrast;
import static org.fest.assertions.api.Assertions.assertThat;

/** Tests defined on the upstream client project as valid. */
public class UpstreamTest {
  private static final String IMG = "my.server.com/some/path/to/image.jpg";
  private static final String KEY = "my-security-key";

  private static ThumborUrlBuilder url() {
    return Thumbor.create("/", KEY).buildImage(IMG);
  }

  @Test public void matchingSignature() {
    String expected = "/8ammJH8D-7tXy6kU3lTvoXlhu4o=/300x200/my.server.com/some/path/to/image.jpg";
    String actual = url().resize(300, 200).toUrl();
    assertThat(actual).isEqualTo(expected);
  }

  @Test public void matchingSignatureWithMeta() {
    String expected = "/Ps3ORJDqxlSQ8y00T29GdNAh2CY=/meta/my.server.com/some/path/to/image.jpg";
    String actual = url().toMeta();
    assertThat(actual).isEqualTo(expected);
  }

  @Test public void matchingSignatureWithFilters() {
    String expected =
        "/ZZtPCw-BLYN1g42Kh8xTcRs0Qls=/filters:brightness(10):contrast(20)/my.server.com/some/path/to/image.jpg";
    String actual = url().filter(brightness(10), contrast(20)).toUrl();
    assertThat(actual).isEqualTo(expected);
  }
}
