// Copyright 2012 Square, Inc.
package com.squareup.thumbor;

import org.junit.Test;

import static com.squareup.thumbor.Thumbor.HorizontalAlign.CENTER;
import static com.squareup.thumbor.Thumbor.VerticalAlign.MIDDLE;
import static com.squareup.thumbor.Thumbor.brightness;
import static com.squareup.thumbor.Thumbor.image;
import static com.squareup.thumbor.Thumbor.contrast;
import static com.squareup.thumbor.Thumbor.fill;
import static com.squareup.thumbor.Thumbor.noise;
import static com.squareup.thumbor.Thumbor.quality;
import static com.squareup.thumbor.Thumbor.rgb;
import static com.squareup.thumbor.Thumbor.roundCorner;
import static com.squareup.thumbor.Thumbor.sharpen;
import static com.squareup.thumbor.Thumbor.watermark;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ThumborTest {
  @Test public void testComplexUnsafeBuild() {
    String expected = "/unsafe/10x10:90x90/40x40/filters:watermark(/unsafe/20x20/b.com/c.jpg,10,10,0):round_corner(5,255,255,255)/a.com/b.png";
    String actual = image("a.com/b.png")
        .crop(10, 10, 90, 90)
        .resize(40, 40)
        .filter(
            watermark(image("b.com/c.jpg").resize(20, 20), 10, 10),
            roundCorner(5))
        .buildUnsafe();
    assertEquals(expected, actual);
  }

  @Test public void testComplexSafeBuild() {
    String expected = "/xrUrWUD_ZhogPh-rvPF5VhgWENCgh-mzknoAEZ7dcX_xa7sjqP1ff9hQQq_ORAKmuCr5pyyU3srXG7BUdWUzBqp3AIucz8KiGsmHw1eFe4SBWhp1wSQNG49jSbbuHaFF_4jy5oV4Nh821F4yqNZfe6CIvjbrr1Vw2aMPL4bE7VCHBYE9ukKjVjLRiW3nLfih/a.com/b.png";
    String actual = image("a.com/b.png")
        .crop(10, 10, 90, 90)
        .resize(40, 40)
        .filter(
            watermark(image("b.com/c.jpg").resize(20, 20), 10, 10),
            roundCorner(5))
        .key("test")
        .buildSafe();
    assertEquals(expected, actual);
  }

  @Test public void testKeyChangesToStringToSafeBuild() {
    Thumbor url = image("a.com/b.png");
    assertNull(url.key);
    assertTrue(url.toString().startsWith("/unsafe/"));
    url.key("test");
    assertNotNull(url.key);
    assertFalse(url.toString().startsWith("/unsafe/"));
  }

  @Test public void testBuildMeta() {
    assertTrue(image("a.com/b.png").buildMeta().startsWith("/meta/"));
  }

  @Test public void testUnsafePrependHost() {
    String expected = "http://me.com/unsafe/a.com/b.png";
    String actual = image("a.com/b.png").host("http://me.com").buildUnsafe();
    assertEquals(expected, actual);
  }

  @Test public void testSafePrependHost() {
    String expected = "http://me.com/oNchWAmpD6SoDZXBkUpAYU3p6ZnHQY1_mYdnfTkm36g=/a.com/b.png";
    String actual = image("a.com/b.png").key("test").host("http://me.com").buildSafe();
    assertEquals(expected, actual);
  }

  @Test public void testMetaPrependHost() {
    String expected = "http://me.com/meta/a.com/b.png";
    String actual = image("a.com/b.png").host("http://me.com").buildMeta();
    assertEquals(expected, actual);
  }

  @Test public void testResize() {
    Thumbor url = new Thumbor("a.com/b.png");
    assertFalse(url.hasResize);

    url.resize(10, 5);
    assertTrue(url.hasResize);
    assertEquals(10, url.resizeWidth);
    assertEquals(5, url.resizeHeight);
    assertEquals("/unsafe/10x5/a.com/b.png", url.buildUnsafe());
  }

  @Test public void testResizeAndFitIn() {
    Thumbor url = new Thumbor("a.com/b.png");
    url.resize(10, 5);
    assertFalse(url.fitIn);
    url.fitIn();
    assertTrue(url.fitIn);
    assertEquals("/unsafe/10x5/fit-in/a.com/b.png", url.buildUnsafe());
  }

  @Test public void testResizeAndFlip() {
    Thumbor url1 = new Thumbor("a.com/b.png").resize(10, 5).flipHorizontally();
    assertTrue(url1.flipHorizontally);
    assertEquals("/unsafe/-10x5/a.com/b.png", url1.buildUnsafe());

    Thumbor url2 = new Thumbor("a.com/b.png").resize(10, 5).flipVertically();
    assertTrue(url2.flipVertically);
    assertEquals("/unsafe/10x-5/a.com/b.png", url2.buildUnsafe());

    Thumbor url3 = new Thumbor("a.com/b.png").resize(10, 5).flipHorizontally().flipVertically();
    assertTrue(url3.flipHorizontally);
    assertTrue(url3.flipVertically);
    assertEquals("/unsafe/-10x-5/a.com/b.png", url3.buildUnsafe());
  }

  @Test public void testCrop() {
    Thumbor url = new Thumbor("a.com/b.png");
    assertFalse(url.hasCrop);

    url.crop(1, 2, 3, 4);
    assertTrue(url.hasCrop);
    assertEquals(1, url.cropTop);
    assertEquals(2, url.cropLeft);
    assertEquals(3, url.cropBottom);
    assertEquals(4, url.cropRight);
    assertEquals("/unsafe/2x1:4x3/a.com/b.png", url.buildUnsafe());
  }

  @Test public void testCropAndSmart() {
    Thumbor url = new Thumbor("a.com/b.png");
    url.crop(1, 2, 3, 4);

    assertFalse(url.isSmart);
    url.smart();
    assertTrue(url.isSmart);
    assertEquals("/unsafe/2x1:4x3/smart/a.com/b.png", url.buildUnsafe());
  }

  @Test public void testCannotFlipHorizontalWithoutResize() {
    Thumbor url = new Thumbor("");
    assertFalse(url.hasResize);
    assertFalse(url.flipHorizontally);
    try {
      url.flipHorizontally();
      fail("Allowed horizontal flip without resize.");
    } catch (IllegalStateException e) {
      // Pass.
    }
    assertFalse(url.flipHorizontally);
  }

  @Test public void testCannotFlipVerticalWithoutResize() {
    Thumbor url = new Thumbor("");
    assertFalse(url.hasResize);
    assertFalse(url.flipVertically);
    try {
      url.flipVertically();
      fail("Allowed vertical flip without resize.");
    } catch (IllegalStateException e) {
      // Pass.
    }
    assertFalse(url.flipVertically);
  }

  @Test public void testCannotFitInWithoutCrop() {
    Thumbor url = new Thumbor("");
    assertFalse(url.hasCrop);
    assertFalse(url.fitIn);
    try {
      url.fitIn();
      fail("Allowed fit-in resize without resize.");
    } catch (IllegalStateException e) {
      // Pass.
    }
    assertFalse(url.fitIn);
  }

  @Test public void testCannotSmartWithoutCrop() {
    Thumbor url = new Thumbor("");
    assertFalse(url.hasCrop);
    assertFalse(url.isSmart);
    try {
      url.smart();
      fail("Allowed smart crop without crop.");
    } catch (IllegalStateException e) {
      // Pass.
    }
    assertFalse(url.isSmart);
  }

  @Test public void testDoubleAlignmentMethodSetsBoth() {
    Thumbor url = new Thumbor("");
    url.crop(0, 0, 1, 1);
    assertNull(url.cropHorizontalAlign);
    assertNull(url.cropVerticalAlign);
    url.align(MIDDLE, CENTER);
    assertEquals(CENTER, url.cropHorizontalAlign);
    assertEquals(MIDDLE, url.cropVerticalAlign);
  }

  @Test public void testCannotAlignWithoutCrop() {
    Thumbor url = new Thumbor("");
    assertFalse(url.hasCrop);
    assertNull(url.cropHorizontalAlign);

    try {
      url.align(CENTER);
      fail("Allowed horizontal crop align without crop.");
    } catch (IllegalStateException e) {
      // Pass.
    }

    try {
      url.align(MIDDLE);
      fail("Allowed vertical crop align without crop.");
    } catch (IllegalStateException e) {
      // Pass.
    }
  }

  @Test public void testCannotIssueBadCrop() {
    Thumbor url = new Thumbor("");

    try {
      url.crop(-1, 0, 1, 1);
      fail("Bad top value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      url.crop(0, -1, 1, 1);
      fail("Bad left value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      url.crop(0, 0, -1, 1);
      fail("Bad bottom value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      url.crop(0, 0, 1, -1);
      fail("Bad right value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      url.crop(0, 1, 1, 0);
      fail("Right value less than left value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      url.crop(1, 0, 0, 1);
      fail("Bottom value less than top value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testCannotIssueBadResize() {
    Thumbor url = new Thumbor("");

    try {
      url.resize(0, 5);
      fail("Bad width value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      url.resize(10, 0);
      fail("Bad height value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testCannotBuildWithInvalidTarget() {
    try {
      image(null);
      fail("Bad target image URL allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      image("");
      fail("Bad target image URL allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testCannotAddInvalidKey() {
    Thumbor url = new Thumbor("");

    try {
      url.key(null);
      fail("Bad key string allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      url.key("");
      fail("Bad key string allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testCannotAddInvalidHost() {
    Thumbor url = new Thumbor("");

    try {
      url.host(null);
      fail("Bad host string allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      url.host("");
      fail("Bad host string allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testCannotBuildSafeWithoutKey() {
    Thumbor url = new Thumbor("");
    try {
      url.buildSafe();
      fail(".buildSafe() succeeds without key.");
    } catch (IllegalStateException e) {
      // Pass.
    }
  }

  @Test public void testFilterBrightnessInvalidValues() {
    try {
      brightness(-101);
      fail("Brightness allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      brightness(101);
      fail("Brightness allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testFilterBrightnessFormat() {
    assertEquals("brightness(30)", brightness(30));
  }

  @Test public void testFilterContrastInvalidValues() {
    try {
      contrast(-101);
      fail("Contrast allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      contrast(101);
      fail("Contrast allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testFilterContrastFormat() {
    assertEquals("contrast(30)", contrast(30));
  }

  @Test public void testFilterNoiseInvalidValues() {
    try {
      noise(-1);
      fail("Noise allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      noise(101);
      fail("Noise allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testFilterNoiseFormat() {
    assertEquals("noise(30)", noise(30));
  }

  @Test public void testFilterQualityInvalidValues() {
    try {
      quality(-1);
      fail("Quality allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      quality(101);
      fail("Quality allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testFilterQualityFormat() {
    assertEquals("quality(30)", quality(30));
  }

  @Test public void testFilterRgbInvalidValues() {
    try {
      rgb(-101, 0, 0);
      fail("RGB allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      rgb(101, 0, 0);
      fail("RGB allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      rgb(0, -101, 0);
      fail("RGB allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      rgb(0, 101, 0);
      fail("RGB allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      rgb(0, 0, -101);
      fail("RGB allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      rgb(0, 0, 101);
      fail("RGB allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testFilterRgbFormat() {
    assertEquals("rgb(-30,40,-75)", rgb(-30, 40, -75));
  }

  @Test public void testFilterRoundCornerInvalidValues() {
    try {
      roundCorner(0);
      fail("Round corner allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      roundCorner(-50);
      fail("Round corner allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      roundCorner(1, -1, 0xFFFFFF);
      fail("Round corner allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testFilterRoundCornerFormat() {
    assertEquals("round_corner(10,255,255,255)", roundCorner(10));
    assertEquals("round_corner(10,255,16,16)", roundCorner(10, 0xFF1010));
    assertEquals("round_corner(10|15,255,16,16)", roundCorner(10, 15, 0xFF1010));
  }

  @Test public void testFilterWatermarkInvalidValues() {
    try {
      watermark((String) null);
      fail("Watermark allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      watermark((Thumbor) null);
      fail("Watermark allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      watermark("");
      fail("Watermark allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      watermark("a.png", 0, 0, -1);
      fail("Watermark allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
    try {
      watermark("a.png", 0, 0, 101);
      fail("Watermark allowed invalid value.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testFilterWatermarkFormat() {
    assertEquals("watermark(a.png,0,0,0)", watermark("a.png"));
    assertEquals("watermark(/unsafe/10x10/a.png,0,0,0)", watermark(image("a.png").resize(10, 10)));
    assertEquals("watermark(a.png,20,20,0)", watermark("a.png", 20, 20));
    assertEquals("watermark(/unsafe/10x10/a.png,20,20,0)", watermark(image("a.png").resize(10, 10), 20, 20));
    assertEquals("watermark(a.png,20,20,50)", watermark("a.png", 20, 20, 50));
    assertEquals("watermark(/unsafe/10x10/a.png,20,20,50)", watermark(image("a.png").resize(10, 10), 20, 20, 50));
  }

  @Test public void testFilterSharpenFormat() {
    assertEquals("sharpen(3.0,4.0,true)", sharpen(3, 4, true));
    assertEquals("sharpen(3.0,4.0,false)", sharpen(3, 4, false));
    assertEquals("sharpen(3.1,4.2,true)", sharpen(3.1f, 4.2f, true));
    assertEquals("sharpen(3.1,4.2,false)", sharpen(3.1f, 4.2f, false));
  }

  @Test public void testFilterFillingFormat() {
    assertEquals("fill(ff2020)", fill(0xFF2020));
    assertEquals("fill(ff2020)", fill(0xABFF2020));
  }
}
