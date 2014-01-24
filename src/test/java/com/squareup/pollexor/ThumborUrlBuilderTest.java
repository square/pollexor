// Copyright 2012 Square, Inc.
package com.squareup.pollexor;

import static com.squareup.pollexor.ThumborUrlBuilder.brightness;
import static com.squareup.pollexor.ThumborUrlBuilder.contrast;
import static com.squareup.pollexor.ThumborUrlBuilder.fill;
import static com.squareup.pollexor.ThumborUrlBuilder.frame;
import static com.squareup.pollexor.ThumborUrlBuilder.noise;
import static com.squareup.pollexor.ThumborUrlBuilder.quality;
import static com.squareup.pollexor.ThumborUrlBuilder.rgb;
import static com.squareup.pollexor.ThumborUrlBuilder.roundCorner;
import static com.squareup.pollexor.ThumborUrlBuilder.sharpen;
import static com.squareup.pollexor.ThumborUrlBuilder.watermark;
import static com.squareup.pollexor.ThumborUrlBuilder.HorizontalAlign.CENTER;
import static com.squareup.pollexor.ThumborUrlBuilder.VerticalAlign.MIDDLE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

import org.junit.Test;

public class ThumborUrlBuilderTest {

  private final Thumbor unsafe = Thumbor.create("/");
  private final Thumbor safe = Thumbor.create("/", "test");

  @Test public void testNoConfig() {
    assertThat(unsafe.buildImage("http://a.com/b.png").toUrl()) //
        .isEqualTo("/unsafe/http://a.com/b.png");
  }

  @Test public void testComplexUnsafeBuild() {
    String expected = "/unsafe/10x10:90x90/40x40/filters:watermark(/unsafe/20x20/b.com/c.jpg,10,10,0):round_corner(5,255,255,255)/a.com/b.png";
    String actual = unsafe.buildImage("a.com/b.png")
        .crop(10, 10, 90, 90)
        .resize(40, 40)
        .filter(
            watermark(unsafe.buildImage("b.com/c.jpg").resize(20, 20), 10, 10),
            roundCorner(5))
        .toUrl();
    assertThat(actual).isEqualTo(expected);
  }

  @Test public void testComplexSafeBuild() {
    String expected = "/X_5ze5WdyTObULp4Toj6mHX-R1U=/10x10:90x90/40x40/filters:watermark(/unsafe/20x20/b.com/c.jpg,10,10,0):round_corner(5,255,255,255)/a.com/b.png";
    String actual = safe.buildImage("a.com/b.png")
        .crop(10, 10, 90, 90)
        .resize(40, 40)
        .filter(
            watermark(unsafe.buildImage("b.com/c.jpg").resize(20, 20), 10, 10),
            roundCorner(5))
        .toUrl();
    assertThat(actual).isEqualTo(expected);
  }

  @Test public void testComplexLegacySafeBuild() {
    String expected = "/xrUrWUD_ZhogPh-rvPF5VhgWENCgh-mzknoAEZ7dcX_xa7sjqP1ff9hQQq_ORAKmuCr5pyyU3srXG7BUdWUzBqp3AIucz8KiGsmHw1eFe4SBWhp1wSQNG49jSbbuHaFF_4jy5oV4Nh821F4yqNZfe6CIvjbrr1Vw2aMPL4bE7VCHBYE9ukKjVjLRiW3nLfih/a.com/b.png";
    String actual = safe.buildImage("a.com/b.png")
        .crop(10, 10, 90, 90)
        .resize(40, 40)
        .filter(
            watermark(unsafe.buildImage("b.com/c.jpg").resize(20, 20), 10, 10),
            roundCorner(5))
        .legacy()
        .toUrl();
    assertThat(actual).isEqualTo(expected);
  }

  @Test public void testKeyChangesToStringToSafeBuild() {
    ThumborUrlBuilder url1 = unsafe.buildImage("a.com/b.png");
    assertThat(url1.key).isNull();
    assertThat(url1.toUrl().startsWith("/unsafe/")).isTrue();
    ThumborUrlBuilder url2 = safe.buildImage("a.com/b.png");
    assertThat(url2.key).isNotNull();
    assertThat(url2.toUrl().startsWith("/unsafe/")).isFalse();
  }

  @Test public void testBuildMeta() {
    assertThat(unsafe.buildImage("a.com/b.png").toMeta()).startsWith("/meta/");
  }

  @Test public void testSafeUrlCanStillBuildUnsafe() {
    String expected = "/unsafe/a.com/b.png";
    String actual = safe.buildImage("a.com/b.png").toUrlUnsafe();
    assertThat(actual).isEqualTo(expected);
  }

  @Test public void testSafeMetaUrlCanStillBuildUnsafe() {
    String expected = "/meta/a.com/b.png";
    String actual = safe.buildImage("a.com/b.png").toMetaUnsafe();
    assertThat(actual).isEqualTo(expected);
  }

  @Test public void testResize() {
    ThumborUrlBuilder url = unsafe.buildImage("a.com/b.png");
    assertThat(url.hasResize).isFalse();

    url.resize(10, 5);
    assertThat(url.hasResize).isTrue();
    assertThat(Integer.valueOf(url.resizeWidthHeight.getWidth())).isEqualTo(10);
    assertThat(Integer.valueOf(url.resizeWidthHeight.getHeight())).isEqualTo(5);
    assertThat(url.toUrl()).isEqualTo("/unsafe/10x5/a.com/b.png");

    ThumborUrlBuilder url2 = unsafe.buildImage("a.com/b.png");
    assertThat(url2.hasResize).isFalse();

    ThumborImageSize imageResize = new ThumborImageSize();
    assertThat(imageResize.getHeight()).isEqualTo("orig");
    assertThat(imageResize.getWidth()).isEqualTo("orig");

    url2.resize(imageResize);
    assertThat(url.hasResize).isTrue();
    assertThat(url2.resizeWidthHeight.getWidth()).isEqualTo("orig");
    assertThat(url2.resizeWidthHeight.getHeight()).isEqualTo("orig");
    assertThat(url2.toUrl()).isEqualTo("/unsafe/origxorig/a.com/b.png");

  }

  @Test public void testResizeAndFitIn() {
    ThumborUrlBuilder url = unsafe.buildImage("a.com/b.png");
    url.resize(10, 5);
    assertThat(url.fitIn).isFalse();
    url.fitIn();
    assertThat(url.fitIn).isTrue();
    assertThat(url.toUrl()).isEqualTo("/unsafe/fit-in/10x5/a.com/b.png");
  }

  @Test public void testResizeAndFlip() {
    ThumborUrlBuilder image1 = unsafe.buildImage("a.com/b.png").resize(10, 5).flipHorizontally();
    assertThat(image1.flipHorizontally).isTrue();
    assertThat(image1.toUrl()).isEqualTo("/unsafe/-10x5/a.com/b.png");

    ThumborUrlBuilder image2 = unsafe.buildImage("a.com/b.png").resize(10, 5).flipVertically();
    assertThat(image2.flipVertically).isTrue();
    assertThat(image2.toUrl()).isEqualTo("/unsafe/10x-5/a.com/b.png");

    ThumborUrlBuilder image3 = unsafe.buildImage("a.com/b.png").resize(10, 5).flipHorizontally().flipVertically();
    assertThat(image3.flipHorizontally).isTrue();
    assertThat(image3.flipVertically).isTrue();
    assertThat(image3.toUrl()).isEqualTo("/unsafe/-10x-5/a.com/b.png");
  }

  @Test public void testCrop() {
    ThumborUrlBuilder image = unsafe.buildImage("a.com/b.png");
    assertThat(image.hasCrop).isFalse();

    image.crop(1, 2, 3, 4);
    assertThat(image.hasCrop).isTrue();
    assertThat(image.cropTop).isEqualTo(1);
    assertThat(image.cropLeft).isEqualTo(2);
    assertThat(image.cropBottom).isEqualTo(3);
    assertThat(image.cropRight).isEqualTo(4);
    assertThat(image.toUrl()).isEqualTo("/unsafe/2x1:4x3/a.com/b.png");
  }

  @Test public void testCropAndSmart() {
    ThumborUrlBuilder image = unsafe.buildImage("http://a.com/b.png");
    image.crop(1, 2, 3, 4);

    assertThat(image.isSmart).isFalse();
    image.smart();
    assertThat(image.isSmart).isTrue();
    assertThat(image.toUrl()).isEqualTo("/unsafe/2x1:4x3/smart/http://a.com/b.png");
  }

  @Test public void testCannotFlipHorizontalWithoutResize() {
    ThumborUrlBuilder image = unsafe.buildImage("http://a.com/b.png");
    assertThat(image.hasResize).isFalse();
    assertThat(image.flipHorizontally).isFalse();
    try {
      image.flipHorizontally();
      fail("Allowed horizontal flip without resize.");
    } catch (UnableToBuildException expected) {
    }
    assertThat(image.flipHorizontally).isFalse();
  }

  @Test public void testCannotFlipVerticalWithoutResize() {
    ThumborUrlBuilder image = unsafe.buildImage("http://a.com/b.png");
    assertThat(image.hasResize).isFalse();
    assertThat(image.flipVertically).isFalse();
    try {
      image.flipVertically();
      fail("Allowed vertical flip without resize.");
    } catch (UnableToBuildException expected) {
    }
    assertThat(image.flipVertically).isFalse();
  }

  @Test public void testCannotFitInWithoutCrop() {
    ThumborUrlBuilder image = unsafe.buildImage("http://a.com/b.png");
    assertThat(image.hasCrop).isFalse();
    assertThat(image.fitIn).isFalse();
    try {
      image.fitIn();
      fail("Allowed fit-in resize without resize.");
    } catch (UnableToBuildException expected) {
    }
    assertThat(image.fitIn).isFalse();
  }

  @Test public void testCannotSmartWithoutCrop() {
    ThumborUrlBuilder image = unsafe.buildImage("http://a.com/b.png");
    assertThat(image.hasCrop).isFalse();
    assertThat(image.isSmart).isFalse();
    try {
      image.smart();
      fail("Allowed smart crop without crop.");
    } catch (UnableToBuildException expected) {
    }
    assertThat(image.isSmart).isFalse();
  }

  @Test public void testDoubleAlignmentMethodSetsBoth() {
    ThumborUrlBuilder image = unsafe.buildImage("http://a.com/b.png");
    image.crop(0, 0, 1, 1);
    assertThat(image.cropHorizontalAlign).isNull();
    assertThat(image.cropVerticalAlign).isNull();
    image.align(MIDDLE, CENTER);
    assertThat(image.cropHorizontalAlign).isEqualTo(CENTER);
    assertThat(image.cropVerticalAlign).isEqualTo(MIDDLE);
  }

  @Test public void testCannotAlignWithoutCrop() {
    ThumborUrlBuilder image = unsafe.buildImage("http://a.com/b.png");
    assertThat(image.hasCrop).isFalse();
    assertThat(image.cropHorizontalAlign).isNull();

    try {
      image.align(CENTER);
      fail("Allowed horizontal crop align without crop.");
    } catch (UnableToBuildException expected) {
    }

    try {
      image.align(MIDDLE);
      fail("Allowed vertical crop align without crop.");
    } catch (UnableToBuildException expected) {
    }
  }

  @Test public void testCannotIssueBadCrop() {
    ThumborUrlBuilder image = unsafe.buildImage("http://a.com/b.png");

    try {
      image.crop(-1, 0, 1, 1);
      fail("Bad top value allowed.");
    } catch (UnableToBuildException expected) {
    }

    try {
      image.crop(0, -1, 1, 1);
      fail("Bad left value allowed.");
    } catch (UnableToBuildException expected) {
    }

    try {
      image.crop(0, 0, -1, 1);
      fail("Bad bottom value allowed.");
    } catch (UnableToBuildException expected) {
    }

    try {
      image.crop(0, 0, 1, -1);
      fail("Bad right value allowed.");
    } catch (UnableToBuildException expected) {
    }

    try {
      image.crop(0, 1, 1, 0);
      fail("Right value less than left value allowed.");
    } catch (UnableToBuildException expected) {
    }

    try {
      image.crop(1, 0, 0, 1);
      fail("Bottom value less than top value allowed.");
    } catch (UnableToBuildException expected) {
    }
  }

  @Test public void testCannotIssueBadResize() {
    ThumborUrlBuilder image = unsafe.buildImage("http://a.com/b.png");

    try {
      image.resize(-1, 5);
      fail("Bad width value allowed.");
    } catch (UnableToBuildException expected) {
    }

    try {
      image.resize(10, -400);
      fail("Bad height value allowed.");
    } catch (UnableToBuildException expected) {
    }

    try {
      image.resize(0, 0);
      fail("Zero resize value allowed.");
    } catch (UnableToBuildException expected) {
    }
  }

  @Test public void testCannotBuildSafeWithoutKey() {
    try {
      unsafe.buildImage("foo").toUrlSafe();
      fail(".toUrlSafe() succeeds without key.");
    } catch (UnableToBuildException expected) {
    }
  }

  @Test public void testFilterBrightnessInvalidValues() {
    try {
      brightness(-101);
      fail("Brightness allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      brightness(101);
      fail("Brightness allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
  }

  @Test public void testFilterBrightnessFormat() {
    assertThat(brightness(30)).isEqualTo("brightness(30)");
  }

  @Test public void testFilterContrastInvalidValues() {
    try {
      contrast(-101);
      fail("Contrast allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      contrast(101);
      fail("Contrast allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
  }

  @Test public void testFilterContrastFormat() {
    assertThat(contrast(30)).isEqualTo("contrast(30)");
  }

  @Test public void testFilterNoiseInvalidValues() {
    try {
      noise(-1);
      fail("Noise allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      noise(101);
      fail("Noise allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
  }

  @Test public void testFilterNoiseFormat() {
    assertThat(noise(30)).isEqualTo("noise(30)");
  }

  @Test public void testFilterQualityInvalidValues() {
    try {
      quality(-1);
      fail("Quality allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      quality(101);
      fail("Quality allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
  }

  @Test public void testFilterQualityFormat() {
    assertThat(quality(30)).isEqualTo("quality(30)");
  }

  @Test public void testFilterRgbInvalidValues() {
    try {
      rgb(-101, 0, 0);
      fail("RGB allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      rgb(101, 0, 0);
      fail("RGB allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      rgb(0, -101, 0);
      fail("RGB allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      rgb(0, 101, 0);
      fail("RGB allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      rgb(0, 0, -101);
      fail("RGB allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      rgb(0, 0, 101);
      fail("RGB allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
  }

  @Test public void testFilterRgbFormat() {
    assertThat(rgb(-30, 40, -75)).isEqualTo("rgb(-30,40,-75)");
  }

  @Test public void testFilterRoundCornerInvalidValues() {
    try {
      roundCorner(0);
      fail("Round corner allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      roundCorner(-50);
      fail("Round corner allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      roundCorner(1, -1, 0xFFFFFF);
      fail("Round corner allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
  }

  @Test public void testFilterRoundCornerFormat() {
    assertThat(roundCorner(10)).isEqualTo("round_corner(10,255,255,255)");
    assertThat(roundCorner(10, 0xFF1010)).isEqualTo("round_corner(10,255,16,16)");
    assertThat(roundCorner(10, 15, 0xFF1010)).isEqualTo("round_corner(10|15,255,16,16)");
  }

  @Test public void testFilterWatermarkInvalidValues() {
    try {
      watermark((String) null);
      fail("Watermark allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      watermark((ThumborUrlBuilder) null);
      fail("Watermark allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      watermark("");
      fail("Watermark allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      watermark("a.png", 0, 0, -1);
      fail("Watermark allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
    try {
      watermark("a.png", 0, 0, 101);
      fail("Watermark allowed invalid value.");
    } catch (UnableToBuildException expected) {
    }
  }

  @Test public void testFilterWatermarkFormat() {
    assertThat(watermark("a.png")).isEqualTo("watermark(a.png,0,0,0)");
    assertThat(watermark(unsafe.buildImage("a.png").resize(10, 10))) //
        .isEqualTo("watermark(/unsafe/10x10/a.png,0,0,0)");
    assertThat(watermark("a.png", 20, 20)).isEqualTo("watermark(a.png,20,20,0)");
    assertThat(watermark(unsafe.buildImage("a.png").resize(10, 10), 20, 20)) //
        .isEqualTo("watermark(/unsafe/10x10/a.png,20,20,0)");
    assertThat(watermark("a.png", 20, 20, 50)).isEqualTo("watermark(a.png,20,20,50)");
    assertThat(watermark(unsafe.buildImage("a.png").resize(10, 10), 20, 20, 50)) //
        .isEqualTo("watermark(/unsafe/10x10/a.png,20,20,50)");
  }

  @Test public void testFilterSharpenFormat() {
    assertThat(sharpen(3, 4, true)).isEqualTo("sharpen(3.0,4.0,true)");
    assertThat(sharpen(3, 4, false)).isEqualTo("sharpen(3.0,4.0,false)");
    assertThat(sharpen(3.1f, 4.2f, true)).isEqualTo("sharpen(3.1,4.2,true)");
    assertThat(sharpen(3.1f, 4.2f, false)).isEqualTo("sharpen(3.1,4.2,false)");
  }

  @Test public void testFilterFillingFormat() {
    assertThat(fill(0xFF2020)).isEqualTo("fill(ff2020)");
    assertThat(fill(0xABFF2020)).isEqualTo("fill(ff2020)");
  }

  @Test public void testFilterFrameFormat() {
    assertThat(frame("a.png")).isEqualTo("frame(a.png)");
  }
}
