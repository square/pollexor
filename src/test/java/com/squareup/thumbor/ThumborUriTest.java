// Copyright 2012 Square, Inc.
package com.squareup.thumbor;

import org.junit.Test;

import static com.squareup.thumbor.ThumborUri.HorizontalAlign.CENTER;
import static com.squareup.thumbor.ThumborUri.VerticalAlign.MIDDLE;
import static com.squareup.thumbor.ThumborUri.brightness;
import static com.squareup.thumbor.ThumborUri.build;
import static com.squareup.thumbor.ThumborUri.contrast;
import static com.squareup.thumbor.ThumborUri.fill;
import static com.squareup.thumbor.ThumborUri.noise;
import static com.squareup.thumbor.ThumborUri.quality;
import static com.squareup.thumbor.ThumborUri.rgb;
import static com.squareup.thumbor.ThumborUri.roundCorner;
import static com.squareup.thumbor.ThumborUri.sharpen;
import static com.squareup.thumbor.ThumborUri.watermark;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ThumborUriTest {
  @Test public void testComplexUnsafeBuild() {
    String expected = "/unsafe/10x10:90x90/40x40/filters:watermark(/unsafe/20x20/b.com/c.jpg,10,10,0):round_corner(5,255,255,255)/a.com/b.png";
    String actual = build("a.com/b.png")
        .crop(10, 10, 90, 90)
        .resize(40, 40)
        .filter(
            watermark(build("b.com/c.jpg").resize(20, 20), 10, 10),
            roundCorner(5))
        .buildUnsafe();
    assertEquals(expected, actual);
  }

  @Test public void testComplexSafeBuild() {
    String expected = "/xrUrWUD_ZhogPh-rvPF5VhgWENCgh-mzknoAEZ7dcX_xa7sjqP1ff9hQQq_ORAKmuCr5pyyU3srXG7BUdWUzBqp3AIucz8KiGsmHw1eFe4SBWhp1wSQNG49jSbbuHaFF_4jy5oV4Nh821F4yqNZfe6CIvjbrr1Vw2aMPL4bE7VCHBYE9ukKjVjLRiW3nLfih/a.com/b.png";
    String actual = build("a.com/b.png")
        .crop(10, 10, 90, 90)
        .resize(40, 40)
        .filter(
            watermark(build("b.com/c.jpg").resize(20, 20), 10, 10),
            roundCorner(5))
        .key("test")
        .buildSafe();
    assertEquals(expected, actual);
  }

  @Test public void testKeyChangesToStringToSafeBuild() {
    ThumborUri uri = build("a.com/b.png");
    assertNull(uri.key);
    assertTrue(uri.toString().startsWith("/unsafe/"));
    uri.key("test");
    assertNotNull(uri.key);
    assertFalse(uri.toString().startsWith("/unsafe/"));
  }

  @Test public void testBuildMeta() {
    assertTrue(build("a.com/b.png").buildMeta().startsWith("/meta/"));
  }

  @Test public void testResize() {
    ThumborUri uri = new ThumborUri("a.com/b.png");
    assertFalse(uri.hasResize);

    uri.resize(10, 5);
    assertTrue(uri.hasResize);
    assertEquals(10, uri.resizeWidth);
    assertEquals(5, uri.resizeHeight);
    assertEquals("/unsafe/10x5/a.com/b.png", uri.buildUnsafe());
  }

  @Test public void testResizeAndFitIn() {
    ThumborUri uri = new ThumborUri("a.com/b.png");
    uri.resize(10, 5);
    assertFalse(uri.fitIn);
    uri.fitIn();
    assertTrue(uri.fitIn);
    assertEquals("/unsafe/10x5/fit-in/a.com/b.png", uri.buildUnsafe());
  }

  @Test public void testResizeAndFlip() {
    ThumborUri uri1 = new ThumborUri("a.com/b.png").resize(10, 5).flipHorizontally();
    assertTrue(uri1.flipHorizontally);
    assertEquals("/unsafe/-10x5/a.com/b.png", uri1.buildUnsafe());

    ThumborUri uri2 = new ThumborUri("a.com/b.png").resize(10, 5).flipVertically();
    assertTrue(uri2.flipVertically);
    assertEquals("/unsafe/10x-5/a.com/b.png", uri2.buildUnsafe());

    ThumborUri uri3 = new ThumborUri("a.com/b.png").resize(10, 5).flipHorizontally().flipVertically();
    assertTrue(uri3.flipHorizontally);
    assertTrue(uri3.flipVertically);
    assertEquals("/unsafe/-10x-5/a.com/b.png", uri3.buildUnsafe());
  }

  @Test public void testCrop() {
    ThumborUri uri = new ThumborUri("a.com/b.png");
    assertFalse(uri.hasCrop);

    uri.crop(1, 2, 3, 4);
    assertTrue(uri.hasCrop);
    assertEquals(1, uri.cropTop);
    assertEquals(2, uri.cropLeft);
    assertEquals(3, uri.cropBottom);
    assertEquals(4, uri.cropRight);
    assertEquals("/unsafe/2x1:4x3/a.com/b.png", uri.buildUnsafe());
  }

  @Test public void testCropAndSmart() {
    ThumborUri uri = new ThumborUri("a.com/b.png");
    uri.crop(1, 2, 3, 4);

    assertFalse(uri.isSmart);
    uri.smart();
    assertTrue(uri.isSmart);
    assertEquals("/unsafe/2x1:4x3/smart/a.com/b.png", uri.buildUnsafe());
  }

  @Test public void testCannotFlipHorizontalWithoutResize() {
    ThumborUri uri = new ThumborUri("");
    assertFalse(uri.hasResize);
    assertFalse(uri.flipHorizontally);
    try {
      uri.flipHorizontally();
      fail("Allowed horizontal flip without resize.");
    } catch (IllegalStateException e) {
      // Pass.
    }
    assertFalse(uri.flipHorizontally);
  }

  @Test public void testCannotFlipVerticalWithoutResize() {
    ThumborUri uri = new ThumborUri("");
    assertFalse(uri.hasResize);
    assertFalse(uri.flipVertically);
    try {
      uri.flipVertically();
      fail("Allowed vertical flip without resize.");
    } catch (IllegalStateException e) {
      // Pass.
    }
    assertFalse(uri.flipVertically);
  }

  @Test public void testCannotFitInWithoutCrop() {
    ThumborUri uri = new ThumborUri("");
    assertFalse(uri.hasCrop);
    assertFalse(uri.fitIn);
    try {
      uri.fitIn();
      fail("Allowed fit-in resize without resize.");
    } catch (IllegalStateException e) {
      // Pass.
    }
    assertFalse(uri.fitIn);
  }

  @Test public void testCannotSmartWithoutCrop() {
    ThumborUri uri = new ThumborUri("");
    assertFalse(uri.hasCrop);
    assertFalse(uri.isSmart);
    try {
      uri.smart();
      fail("Allowed smart crop without crop.");
    } catch (IllegalStateException e) {
      // Pass.
    }
    assertFalse(uri.isSmart);
  }

  @Test public void testDoubleAlignmentMethodSetsBoth() {
    ThumborUri uri = new ThumborUri("");
    uri.crop(0, 0, 1, 1);
    assertNull(uri.cropHorizontalAlign);
    assertNull(uri.cropVerticalAlign);
    uri.align(MIDDLE, CENTER);
    assertEquals(CENTER, uri.cropHorizontalAlign);
    assertEquals(MIDDLE, uri.cropVerticalAlign);
  }

  @Test public void testCannotAlignWithoutCrop() {
    ThumborUri uri = new ThumborUri("");
    assertFalse(uri.hasCrop);
    assertNull(uri.cropHorizontalAlign);

    try {
      uri.align(CENTER);
      fail("Allowed horizontal crop align without crop.");
    } catch (IllegalStateException e) {
      // Pass.
    }

    try {
      uri.align(MIDDLE);
      fail("Allowed vertical crop align without crop.");
    } catch (IllegalStateException e) {
      // Pass.
    }
  }

  @Test public void testCannotIssueBadCrop() {
    ThumborUri uri = new ThumborUri("");

    try {
      uri.crop(-1, 0, 1, 1);
      fail("Bad top value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      uri.crop(0, -1, 1, 1);
      fail("Bad left value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      uri.crop(0, 0, -1, 1);
      fail("Bad bottom value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      uri.crop(0, 0, 1, -1);
      fail("Bad right value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      uri.crop(0, 1, 1, 0);
      fail("Right value less than left value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      uri.crop(1, 0, 0, 1);
      fail("Bottom value less than top value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testCannotIssueBadResize() {
    ThumborUri uri = new ThumborUri("");

    try {
      uri.resize(0, 5);
      fail("Bad width value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      uri.resize(10, 0);
      fail("Bad height value allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testCannotBuildWithInvalidTarget() {
    try {
      build(null);
      fail("Bad target image URL allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      build("");
      fail("Bad target image URL allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  @Test public void testCannotAddInvalidKey() {
    ThumborUri uri = new ThumborUri("");

    try {
      uri.key(null);
      fail("Bad key string allowed.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }

    try {
      uri.key("");
      fail("Bad key string allowed.");
    } catch (IllegalArgumentException e) {
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
      watermark((ThumborUri) null);
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
    assertEquals("watermark(/unsafe/10x10/a.png,0,0,0)", watermark(build("a.png").resize(10, 10)));
    assertEquals("watermark(a.png,20,20,0)", watermark("a.png", 20, 20));
    assertEquals("watermark(/unsafe/10x10/a.png,20,20,0)", watermark(build("a.png").resize(10, 10), 20, 20));
    assertEquals("watermark(a.png,20,20,50)", watermark("a.png", 20, 20, 50));
    assertEquals("watermark(/unsafe/10x10/a.png,20,20,50)", watermark(build("a.png").resize(10, 10), 20, 20, 50));
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
