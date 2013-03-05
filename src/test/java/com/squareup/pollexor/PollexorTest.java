// Copyright 2012 Square, Inc.
package com.squareup.pollexor;

import org.junit.Test;

import static com.squareup.pollexor.Pollexor.HorizontalAlign.CENTER;
import static com.squareup.pollexor.Pollexor.UnableToBuildException;
import static com.squareup.pollexor.Pollexor.VerticalAlign.MIDDLE;
import static com.squareup.pollexor.Pollexor.brightness;
import static com.squareup.pollexor.Pollexor.frame;
import static com.squareup.pollexor.Pollexor.image;
import static com.squareup.pollexor.Pollexor.contrast;
import static com.squareup.pollexor.Pollexor.fill;
import static com.squareup.pollexor.Pollexor.noise;
import static com.squareup.pollexor.Pollexor.quality;
import static com.squareup.pollexor.Pollexor.rgb;
import static com.squareup.pollexor.Pollexor.roundCorner;
import static com.squareup.pollexor.Pollexor.sharpen;
import static com.squareup.pollexor.Pollexor.watermark;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PollexorTest {
  @Test public void testNoConfig() {
    assertEquals("/unsafe/a.com/b.png", image("http://a.com/b.png").toUrl());
  }

  @Test public void testComplexUnsafeBuild() {
    String expected = "/unsafe/10x10:90x90/40x40/filters:watermark(/unsafe/20x20/b.com/c.jpg,10,10,0):round_corner(5,255,255,255)/a.com/b.png";
    String actual = image("a.com/b.png")
        .crop(10, 10, 90, 90)
        .resize(40, 40)
        .filter(
            watermark(image("b.com/c.jpg").resize(20, 20), 10, 10),
            roundCorner(5))
        .toUrl();
    assertEquals(expected, actual);
  }

  @Test public void testComplexSafeBuild() {
    String expected = "/X_5ze5WdyTObULp4Toj6mHX-R1U=/10x10:90x90/40x40/filters:watermark(/unsafe/20x20/b.com/c.jpg,10,10,0):round_corner(5,255,255,255)/a.com/b.png";
    String actual = image("a.com/b.png")
        .crop(10, 10, 90, 90)
        .resize(40, 40)
        .filter(
            watermark(image("b.com/c.jpg").resize(20, 20), 10, 10),
            roundCorner(5))
        .key("test")
        .toUrl();
    assertEquals(expected, actual);
  }

  @Test public void testComplexLegacySafeBuild() {
    String expected = "/xrUrWUD_ZhogPh-rvPF5VhgWENCgh-mzknoAEZ7dcX_xa7sjqP1ff9hQQq_ORAKmuCr5pyyU3srXG7BUdWUzBqp3AIucz8KiGsmHw1eFe4SBWhp1wSQNG49jSbbuHaFF_4jy5oV4Nh821F4yqNZfe6CIvjbrr1Vw2aMPL4bE7VCHBYE9ukKjVjLRiW3nLfih/a.com/b.png";
    String actual = image("a.com/b.png")
        .crop(10, 10, 90, 90)
        .resize(40, 40)
        .filter(
            watermark(image("b.com/c.jpg").resize(20, 20), 10, 10),
            roundCorner(5))
        .key("test")
        .legacy()
        .toUrl();
    assertEquals(expected, actual);
  }

  @Test public void testKeyChangesToStringToSafeBuild() {
    Pollexor url = image("a.com/b.png");
    assertNull(url.key);
    assertTrue(url.toUrl().startsWith("/unsafe/"));
    url.key("test");
    assertNotNull(url.key);
    assertFalse(url.toUrl().startsWith("/unsafe/"));
  }

  @Test public void testBuildMeta() {
    assertTrue(image("a.com/b.png").toMeta().startsWith("/meta/"));
  }

  @Test public void testUnsafePrependHost() {
    String expected = "http://me.com/unsafe/a.com/b.png";
    String actual = image("a.com/b.png").host("http://me.com").toUrl();
    assertEquals(expected, actual);
  }

  @Test public void testSafePrependHost() {
    String expected = "http://me.com/aH-fFf9RJ3Z30FchMNIwY8nsT-w=/a.com/b.png";
    String actual = image("a.com/b.png").key("test").host("http://me.com").toUrl();
    assertEquals(expected, actual);
  }

  @Test public void testLegacySafePrependHost() {
    String expected = "http://me.com/oNchWAmpD6SoDZXBkUpAYU3p6ZnHQY1_mYdnfTkm36g=/a.com/b.png";
    String actual = image("a.com/b.png").key("test").host("http://me.com").legacy().toUrl();
    assertEquals(expected, actual);
  }

  @Test public void testMetaPrependHost() {
    String expected = "http://me.com/meta/a.com/b.png";
    String actual = image("a.com/b.png").host("http://me.com").toMeta();
    assertEquals(expected, actual);
  }

  @Test public void testHostAlwaysEndsWithSlash() {
    Pollexor url1 = new Pollexor("");
    assertEquals("/", url1.host);

    Pollexor url2 = new Pollexor("");
    url2.host("http://me.com");
    assertEquals("http://me.com/", url2.host);

    Pollexor url3 = new Pollexor("");
    url3.host("http://me.com/");
    assertEquals("http://me.com/", url3.host);
  }

  @Test public void testSafeUrlCanStillBuildUnsafe() {
    String expected = "/unsafe/a.com/b.png";
    String actual = image("a.com/b.png").key("test").toUrlUnsafe();
    assertEquals(expected, actual);
  }

  @Test public void testSafeMetaUrlCanStillBuildUnsafe() {
    String expected = "/meta/a.com/b.png";
    String actual = image("a.com/b.png").key("test").toMetaUnsafe();
    assertEquals(expected, actual);
  }

  @Test public void testResize() {
    Pollexor url = new Pollexor("a.com/b.png");
    assertFalse(url.hasResize);

    url.resize(10, 5);
    assertTrue(url.hasResize);
    assertEquals(10, url.resizeWidth);
    assertEquals(5, url.resizeHeight);
    assertEquals("/unsafe/10x5/a.com/b.png", url.toUrl());
  }

  @Test public void testResizeAndFitIn() {
    Pollexor url = new Pollexor("a.com/b.png");
    url.resize(10, 5);
    assertFalse(url.fitIn);
    url.fitIn();
    assertTrue(url.fitIn);
    assertEquals("/unsafe/fit-in/10x5/a.com/b.png", url.toUrl());
  }

  @Test public void testResizeAndFlip() {
    Pollexor url1 = new Pollexor("a.com/b.png").resize(10, 5).flipHorizontally();
    assertTrue(url1.flipHorizontally);
    assertEquals("/unsafe/-10x5/a.com/b.png", url1.toUrl());

    Pollexor url2 = new Pollexor("a.com/b.png").resize(10, 5).flipVertically();
    assertTrue(url2.flipVertically);
    assertEquals("/unsafe/10x-5/a.com/b.png", url2.toUrl());

    Pollexor url3 = new Pollexor("a.com/b.png").resize(10, 5).flipHorizontally().flipVertically();
    assertTrue(url3.flipHorizontally);
    assertTrue(url3.flipVertically);
    assertEquals("/unsafe/-10x-5/a.com/b.png", url3.toUrl());
  }

  @Test public void testCrop() {
    Pollexor url = new Pollexor("a.com/b.png");
    assertFalse(url.hasCrop);

    url.crop(1, 2, 3, 4);
    assertTrue(url.hasCrop);
    assertEquals(1, url.cropTop);
    assertEquals(2, url.cropLeft);
    assertEquals(3, url.cropBottom);
    assertEquals(4, url.cropRight);
    assertEquals("/unsafe/2x1:4x3/a.com/b.png", url.toUrl());
  }

  @Test public void testCropAndSmart() {
    Pollexor url = new Pollexor("a.com/b.png");
    url.crop(1, 2, 3, 4);

    assertFalse(url.isSmart);
    url.smart();
    assertTrue(url.isSmart);
    assertEquals("/unsafe/2x1:4x3/smart/a.com/b.png", url.toUrl());
  }

  @Test public void testCannotFlipHorizontalWithoutResize() {
    Pollexor url = new Pollexor("");
    assertFalse(url.hasResize);
    assertFalse(url.flipHorizontally);
    try {
      url.flipHorizontally();
      fail("Allowed horizontal flip without resize.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    assertFalse(url.flipHorizontally);
  }

  @Test public void testCannotFlipVerticalWithoutResize() {
    Pollexor url = new Pollexor("");
    assertFalse(url.hasResize);
    assertFalse(url.flipVertically);
    try {
      url.flipVertically();
      fail("Allowed vertical flip without resize.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    assertFalse(url.flipVertically);
  }

  @Test public void testCannotFitInWithoutCrop() {
    Pollexor url = new Pollexor("");
    assertFalse(url.hasCrop);
    assertFalse(url.fitIn);
    try {
      url.fitIn();
      fail("Allowed fit-in resize without resize.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    assertFalse(url.fitIn);
  }

  @Test public void testCannotSmartWithoutCrop() {
    Pollexor url = new Pollexor("");
    assertFalse(url.hasCrop);
    assertFalse(url.isSmart);
    try {
      url.smart();
      fail("Allowed smart crop without crop.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    assertFalse(url.isSmart);
  }

  @Test public void testDoubleAlignmentMethodSetsBoth() {
    Pollexor url = new Pollexor("");
    url.crop(0, 0, 1, 1);
    assertNull(url.cropHorizontalAlign);
    assertNull(url.cropVerticalAlign);
    url.align(MIDDLE, CENTER);
    assertEquals(CENTER, url.cropHorizontalAlign);
    assertEquals(MIDDLE, url.cropVerticalAlign);
  }

  @Test public void testCannotAlignWithoutCrop() {
    Pollexor url = new Pollexor("");
    assertFalse(url.hasCrop);
    assertNull(url.cropHorizontalAlign);

    try {
      url.align(CENTER);
      fail("Allowed horizontal crop align without crop.");
    } catch (UnableToBuildException e) {
      // Pass.
    }

    try {
      url.align(MIDDLE);
      fail("Allowed vertical crop align without crop.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
  }

  @Test public void testCannotIssueBadCrop() {
    Pollexor url = new Pollexor("");

    try {
      url.crop(-1, 0, 1, 1);
      fail("Bad top value allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }

    try {
      url.crop(0, -1, 1, 1);
      fail("Bad left value allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }

    try {
      url.crop(0, 0, -1, 1);
      fail("Bad bottom value allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }

    try {
      url.crop(0, 0, 1, -1);
      fail("Bad right value allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }

    try {
      url.crop(0, 1, 1, 0);
      fail("Right value less than left value allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }

    try {
      url.crop(1, 0, 0, 1);
      fail("Bottom value less than top value allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
  }

  @Test public void testCannotIssueBadResize() {
    Pollexor url = new Pollexor("");

    try {
      url.resize(0, 5);
      fail("Bad width value allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }

    try {
      url.resize(10, 0);
      fail("Bad height value allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
  }

  @Test public void testCannotBuildWithInvalidTarget() {
    try {
      image(null);
      fail("Bad target image URL allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }

    try {
      image("");
      fail("Bad target image URL allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
  }

  @Test public void testCannotAddInvalidKey() {
    Pollexor url = new Pollexor("");

    try {
      url.key(null);
      fail("Bad key string allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }

    try {
      url.key("");
      fail("Bad key string allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
  }

  @Test public void testCannotAddInvalidHost() {
    Pollexor url = new Pollexor("");

    try {
      url.host(null);
      fail("Bad host string allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }

    try {
      url.host("");
      fail("Bad host string allowed.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
  }

  @Test public void testCannotBuildSafeWithoutKey() {
    Pollexor url = new Pollexor("");
    try {
      url.toUrlSafe();
      fail(".toUrlSafe() succeeds without key.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
  }

  @Test public void testFilterBrightnessInvalidValues() {
    try {
      brightness(-101);
      fail("Brightness allowed invalid value.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      brightness(101);
      fail("Brightness allowed invalid value.");
    } catch (UnableToBuildException e) {
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
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      contrast(101);
      fail("Contrast allowed invalid value.");
    } catch (UnableToBuildException e) {
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
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      noise(101);
      fail("Noise allowed invalid value.");
    } catch (UnableToBuildException e) {
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
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      quality(101);
      fail("Quality allowed invalid value.");
    } catch (UnableToBuildException e) {
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
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      rgb(101, 0, 0);
      fail("RGB allowed invalid value.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      rgb(0, -101, 0);
      fail("RGB allowed invalid value.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      rgb(0, 101, 0);
      fail("RGB allowed invalid value.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      rgb(0, 0, -101);
      fail("RGB allowed invalid value.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      rgb(0, 0, 101);
      fail("RGB allowed invalid value.");
    } catch (UnableToBuildException e) {
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
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      roundCorner(-50);
      fail("Round corner allowed invalid value.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      roundCorner(1, -1, 0xFFFFFF);
      fail("Round corner allowed invalid value.");
    } catch (UnableToBuildException e) {
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
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      watermark((Pollexor) null);
      fail("Watermark allowed invalid value.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      watermark("");
      fail("Watermark allowed invalid value.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      watermark("a.png", 0, 0, -1);
      fail("Watermark allowed invalid value.");
    } catch (UnableToBuildException e) {
      // Pass.
    }
    try {
      watermark("a.png", 0, 0, 101);
      fail("Watermark allowed invalid value.");
    } catch (UnableToBuildException e) {
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

  @Test public void testFilterFrameFormat() {
    assertEquals("frame(a.png)", frame("a.png"));
  }
}
