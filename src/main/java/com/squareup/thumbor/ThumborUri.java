// Copyright 2012 Square, Inc.
package com.squareup.thumbor;

import java.util.ArrayList;
import java.util.List;

import static com.squareup.thumbor.Utilities.aes128Encrypt;
import static com.squareup.thumbor.Utilities.md5;
import static com.squareup.thumbor.Utilities.normalizeString;
import static com.squareup.thumbor.Utilities.stripProtocolAndParams;

/**
 * Fluent interface to create a URI appropriate for passing to Thumbor.
 *
 * @see #build(String)
 */
public final class ThumborUri {
  private static final String PREFIX_UNSAFE = "unsafe";
  private static final String PREFIX_META = "meta";
  private static final String PART_SMART = "smart";
  private static final String PART_FIT_IN = "fit-in";
  private static final String PART_FILTERS = "filters";
  private static final String FILTER_BRIGHTNESS = "brightness";
  private static final String FILTER_CONTRAST = "contrast";
  private static final String FILTER_NOISE = "noise";
  private static final String FILTER_QUALITY = "quality";
  private static final String FILTER_RGB = "rgb";
  private static final String FILTER_ROUND_CORNER = "round_corner";
  private static final String FILTER_WATERMARK = "watermark";
  private static final String FILTER_SHARPEN = "sharpen";
  private static final String FILTER_FILL = "fill";

  /**
   * Horizontal alignment for crop positioning.
   */
  public enum HorizontalAlign {
    LEFT("left"), CENTER("center"), RIGHT("right");

    final String value;

    private HorizontalAlign(String value) {
      this.value = value;
    }
  }

  /**
   * Vertical alignment for crop positioning.
   */
  public enum VerticalAlign {
    TOP("top"), MIDDLE("middle"), BOTTOM("bottom");

    final String value;

    private VerticalAlign(String value) {
      this.value = value;
    }
  }

  /**
   * Exception denoting that a fatal error occurred while assembling the URI for the current configuration.
   *
   * @see #getCause()
   */
  public static class UnableToBuildException extends RuntimeException {
    public UnableToBuildException(Throwable e) {
      super(e);
    }
  }

  final String target;
  String key;
  boolean hasCrop = false;
  boolean hasResize = false;
  boolean isSmart = false;
  boolean flipHorizontally = false;
  boolean flipVertically = false;
  boolean fitIn = false;
  int resizeWidth;
  int resizeHeight;
  int cropTop;
  int cropLeft;
  int cropBottom;
  int cropRight;
  HorizontalAlign cropHorizontalAlign;
  VerticalAlign cropVerticalAlign;
  List<String> filters;

  /**
   * Start a new Thumbor URI configuration for the specified target image URL.
   *
   * @param target Target image URL.
   */
  ThumborUri(String target) {
    this.target = stripProtocolAndParams(target);
  }

  /**
   * Start building an image URI for Thumbor.
   *
   * @param target Target image to manipulate.
   * @return New instance for configuration.
   */
  public static ThumborUri build(String target) {
    if (target == null || target.length() == 0) {
      throw new IllegalArgumentException("Target image URI must not be blank.");
    }
    return new ThumborUri(target);
  }

  /**
   * Set a key for secure URI generation. This will default the {@link #toString()} to call {@link #buildSafe()}.
   *
   * @param key Security key for remote server.
   * @return Current instance.
   */
  public ThumborUri key(String key) {
    if (key == null || key.length() == 0) {
      throw new IllegalArgumentException("Key must not be blank.");
    }
    this.key = key;
    return this;
  }

  /**
   * Resize picture to desired size.
   *
   * @param width  Desired width.
   * @param height Desired height.
   * @return Current instance.
   */
  public ThumborUri resize(int width, int height) {
    if (width < 1) {
      throw new IllegalArgumentException("Width must be greater than zero.");
    }
    if (height < 1) {
      throw new IllegalArgumentException("Height must be greater than zero.");
    }
    hasResize = true;
    resizeWidth = width;
    resizeHeight = height;
    return this;
  }

  /**
   * Flip the image horizontally.
   *
   * @return Current instance.
   */
  public ThumborUri flipHorizontally() {
    if (!hasResize) {
      throw new IllegalStateException("Image must be resized first in order to flip.");
    }
    flipHorizontally = true;
    return this;
  }

  /**
   * Flip the image vertically.
   *
   * @return Current instance.
   */
  public ThumborUri flipVertically() {
    if (!hasResize) {
      throw new IllegalStateException("Image must be resized first in order to flip.");
    }
    flipVertically = true;
    return this;
  }

  /**
   * Contrain the image size inside the resized box, scaling as needed.
   *
   * @return Current instance.
   */
  public ThumborUri fitIn() {
    if (!hasResize) {
      throw new IllegalStateException("Image must be resized first in order to apply 'fit-in'.");
    }
    fitIn = true;
    return this;
  }

  /**
   * Crop the image between two points.
   *
   * @param top Top bound.
   * @param left Left bound.
   * @param bottom Bottom bound.
   * @param right Right bound.
   * @return Current instance.
   */
  public ThumborUri crop(int top, int left, int bottom, int right) {
    if (top < 0) {
      throw new IllegalArgumentException("Top must be greater or equal to zero.");
    }
    if (left < 0) {
      throw new IllegalArgumentException("Left must be greater or equal to zero.");
    }
    if (bottom < 1 || bottom <= top) {
      throw new IllegalArgumentException("Bottom must be greater than zero and top.");
    }
    if (right < 1 || right <= left) {
      throw new IllegalArgumentException("Right must be greater than zero and left.");
    }
    hasCrop = true;
    cropTop = top;
    cropLeft = left;
    cropBottom = bottom;
    cropRight = right;
    return this;
  }

  /**
   * Set the horizontal alignment for the image when cropping.
   *
   * @param align Horizontal alignment.
   * @return Current instance.
   */
  public ThumborUri align(HorizontalAlign align) {
    if (!hasCrop) {
      throw new IllegalStateException("Image must be cropped first in order to align.");
    }
    cropHorizontalAlign = align;
    return this;
  }

  /**
   * Set the vertical alignment for the image when cropping.
   *
   * @param align Vertical alignment.
   * @return Current instance.
   */
  public ThumborUri align(VerticalAlign align) {
    if (!hasCrop) {
      throw new IllegalStateException("Image must be cropped first in order to align.");
    }
    cropVerticalAlign = align;
    return this;
  }

  /**
   * Set the horizontal and vertical alignment for the image when cropping.
   *
   * @param valign Vertical alignment.
   * @param halign Horizontal alignment.
   * @return Current instance.
   */
  public ThumborUri align(VerticalAlign valign, HorizontalAlign halign) {
    return align(valign).align(halign);
  }

  /**
   * Use smart cropping for determining the imortant portion of an image.
   *
   * @return Current instance.
   */
  public ThumborUri smart() {
    if (!hasCrop) {
      throw new IllegalStateException("Image must be cropped first in order to smart align.");
    }
    isSmart = true;
    return this;
  }

  /**
   * Add one or more filters to the image.
   *
   * @param filters Filter strings.
   * @return Current instance.
   * @see #brightness(int)
   * @see #contrast(int)
   * @see #fill
   * @see #noise(int)
   * @see #quality(int)
   * @see #rgb(int, int, int)
   * @see #roundCorner(int)
   * @see #roundCorner(int, int)
   * @see #roundCorner(int, int, int)
   * @see #sharpen(float, float, boolean)
   * @see #watermark(String, int, int)
   * @see #watermark(ThumborUri, int, int)
   * @see #watermark(String, int, int, int)
   * @see #watermark(ThumborUri, int, int, int)
   */
  public ThumborUri filter(String... filters) {
    if (filters.length == 0) {
      throw new IllegalArgumentException("You must provide at least one filter.");
    }
    if (this.filters == null) {
      this.filters = new ArrayList<String>(1);
    }
    for (String filter : filters) {
      if (filter == null || filter.length() == 0) {
        throw new IllegalArgumentException("Filter must not be blank.");
      }
      this.filters.add(filter);
    }
    return this;
  }

  /**
   * Build an unsafe version of the URI.
   *
   * @return Unsafe URI for the current configuration.
   */
  public String buildUnsafe() {
    return new StringBuilder("/") //
        .append(PREFIX_UNSAFE) //
        .append("/") //
        .append(assembleConfig()) //
        .append("/") //
        .append(target) //
        .toString();
  }

  /**
   * Build a safe version of the URI. Requires a prior call to {@link #key(String)}.
   *
   * @return Safe URI for the current configuration.
   */
  public String buildSafe() {
    if (key == null) {
      throw new IllegalStateException("Cannot build safe URI without a key.");
    }

    // Assemble config and an MD5 of the target image.
    StringBuilder config = assembleConfig().append("/").append(md5(target));
    final byte[] encrypted = aes128Encrypt(config, normalizeString(key, 16));

    // URI-safe Base64 encode.
    final String encoded = Utilities.base64Encode(encrypted);

    return new StringBuilder("/").append(encoded).append("/").append(target).toString();
  }

  /**
   * Build a URI for fetching Thumbor metadata.
   *
   * @return Meta URI for the current configuration.
   */
  public String buildMeta() {
    return new StringBuilder("/") //
        .append(PREFIX_META) //
        .append("/") //
        .append(assembleConfig()) //
        .append("/") //
        .append(target) //
        .toString();
  }

  @Override public String toString() {
    return (key == null) ? buildUnsafe() : buildSafe();
  }

  /**
   * Assembly the configuration section of the URI.
   *
   * @return Configuration assembled in a {@link StringBuilder}.
   */
  StringBuilder assembleConfig() {
    StringBuilder builder = new StringBuilder();

    if (hasCrop) {
      builder.append("/").append(cropLeft).append("x").append(cropTop) //
          .append(":").append(cropRight).append("x").append(cropBottom);

      if (isSmart) {
        builder.append("/").append(PART_SMART);
      } else {
        if (cropHorizontalAlign != null) {
          builder.append("/").append(cropHorizontalAlign.value);
        }
        if (cropVerticalAlign != null) {
          builder.append("/").append(cropVerticalAlign.value);
        }
      }
    }

    if (hasResize) {
      builder.append("/");
      if (flipHorizontally) {
        builder.append("-");
      }
      builder.append(resizeWidth).append("x");
      if (flipVertically) {
        builder.append("-");
      }
      builder.append(resizeHeight);

      if (fitIn) {
        builder.append("/").append(PART_FIT_IN);
      }
    }

    if (filters != null) {
      builder.append("/").append(PART_FILTERS);
      for (String filter : filters) {
        builder.append(":").append(filter);
      }
    }

    if (builder.length() > 0) {
      builder.deleteCharAt(0);
    }
    return builder;
  }

  /**
   * This filter increases or decreases the image brightness.
   *
   * @param amount -100 to 100 - The amount (in %) to change the image brightness. Positive numbers
   *               make the image brighter and negative numbers make the image darker.
   * @return String representation of this filter.
   */
  public static String brightness(int amount) {
    if (amount < -100 || amount > 100) {
      throw new IllegalArgumentException("Amount must be between -100 and 100, inclusive.");
    }
    return new StringBuilder(FILTER_BRIGHTNESS).append("(").append(amount).append(")").toString();
  }

  /**
   * The filter increases or decreases the image contrast.
   *
   * @param amount -100 to 100 - The amount (in %) to change the image contrast. Positive numbers
   *               increase contrast and negative numbers decrease contrast.
   * @return String representation of this filter.
   */
  public static String contrast(int amount) {
    if (amount < -100 || amount > 100) {
      throw new IllegalArgumentException("Amount must be between -100 and 100, inclusive.");
    }
    return new StringBuilder(FILTER_CONTRAST).append("(").append(amount).append(")").toString();
  }

  /**
   * This filter adds noise to the image.
   *
   * @param amount 0 to 100 - The amount (in %) of noise to add to the image.
   * @return String representation of this filter.
   */
  public static String noise(int amount) {
    if (amount < 0 || amount > 100) {
      throw new IllegalArgumentException("Amount must be between 0 and 100, inclusive");
    }
    return new StringBuilder(FILTER_NOISE).append("(").append(amount).append(")").toString();
  }

  /**
   * This filter changes the overall quality of the JPEG image (does nothing for PNGs or GIFs).
   *
   * @param amount 0 to 100 - The quality level (in %) that the end image will feature.
   * @return String representation of this filter.
   */
  public static String quality(int amount) {
    if (amount < 0 || amount > 100) {
      throw new IllegalArgumentException("Amount must be between 0 and 100, inclusive.");
    }
    return new StringBuilder(FILTER_QUALITY).append("(").append(amount).append(")").toString();
  }

  /**
   * This filter changes the amount of color in each of the three channels.
   *
   * @param r The amount of redness in the picture. Can range from -100 to 100 in percentage.
   * @param g The amount of greenness in the picture. Can range from -100 to 100 in percentage.
   * @param b The amount of blueness in the picture. Can range from -100 to 100 in percentage.
   * @return String representation of this filter.
   */
  public static String rgb(int r, int g, int b) {
    if (r < -100 || r > 100) {
      throw new IllegalArgumentException("Redness value must be between -100 and 100, inclusive.");
    }
    if (g < -100 || g > 100) {
      throw new IllegalArgumentException("Greenness value must be between -100 and 100, inclusive.");
    }
    if (b < -100 || b > 100) {
      throw new IllegalArgumentException("Blueness value must be between -100 and 100, inclusive.");
    }
    return new StringBuilder(FILTER_RGB).append("(") //
        .append(r).append(",") //
        .append(g).append(",") //
        .append(b).append(")") //
        .toString();
  }

  /**
   * This filter adds rounded corners to the image using the specified color as background.
   *
   * @param radius amount of pixels to use as radius.
   * @return String representation of this filter.
   */
  public static String roundCorner(int radius) {
    return roundCorner(radius, 0xFFFFFF);
  }

  /**
   * This filter adds rounded corners to the image using the specified color as background.
   *
   * @param radius amount of pixels to use as radius.
   * @param color  fill color for clipped region.
   * @return String representation of this filter.
   */
  public static String roundCorner(int radius, int color) {
    return roundCorner(radius, 0, color);
  }

  /**
   * This filter adds rounded corners to the image using the specified color as background.
   *
   * @param radiusInner amount of pixels to use as radius.
   * @param radiusOuter specifies the second value for the ellipse used for the radius. Use 0 for
   *                    no value.
   * @param color       fill color for clipped region.
   * @return String representation of this filter.
   */
  public static String roundCorner(int radiusInner, int radiusOuter, int color) {
    if (radiusInner < 1) {
      throw new IllegalArgumentException("Radius must be greater than zero.");
    }
    if (radiusOuter < 0) {
      throw new IllegalArgumentException("Outer radius must be greater than or equal to zero.");
    }
    StringBuilder builder = new StringBuilder(FILTER_ROUND_CORNER).append("(").append(radiusInner);
    if (radiusOuter > 0) {
      builder.append("|").append(radiusOuter);
    }
    return builder.append(",") //
        .append((color & 0xFF0000) >>> 16).append(",") //
        .append((color & 0xFF00) >>> 8).append(",") //
        .append(color & 0xFF).append(")") //
        .toString();
  }

  /**
   * This filter adds a watermark to the image.
   *
   * @param imageUrl Watermark image URL. It is very important to understand that the same image
   *                 loader that Thumbor uses will be used here.
   * @return String representation of this filter.
   */
  public static String watermark(String imageUrl) {
    return watermark(imageUrl, 0, 0);
  }

  /**
   * This filter adds a watermark to the image.
   *
   * @param image Watermark image URL. It is very important to understand that the same image
   *              loader that Thumbor uses will be used here.
   * @return String representation of this filter.
   */
  public static String watermark(ThumborUri image) {
    return watermark(image, 0, 0);
  }

  /**
   * This filter adds a watermark to the image.
   *
   * @param imageUrl Watermark image URL. It is very important to understand that the same image
   *                 loader that Thumbor uses will be used here.
   * @param x        Horizontal position that the watermark will be in. Positive numbers indicate position
   *                 from the left and negative numbers indicate position from the right.
   * @param y        Vertical position that the watermark will be in. Positive numbers indicate position
   *                 from the top and negative numbers indicate position from the bottom.
   * @return String representation of this filter.
   */
  public static String watermark(String imageUrl, int x, int y) {
    return watermark(imageUrl, x, y, 0);
  }

  /**
   * This filter adds a watermark to the image.
   *
   * @param image Watermark image URL. It is very important to understand that the same image
   *              loader that Thumbor uses will be used here.
   * @param x     Horizontal position that the watermark will be in. Positive numbers indicate position
   *              from the left and negative numbers indicate position from the right.
   * @param y     Vertical position that the watermark will be in. Positive numbers indicate position
   *              from the top and negative numbers indicate position from the bottom.
   * @return String representation of this filter.
   */
  public static String watermark(ThumborUri image, int x, int y) {
    if (image == null) {
      throw new IllegalArgumentException("ThumborUri must not be null.");
    }
    return watermark(image.toString(), x, y, 0);
  }

  /**
   * This filter adds a watermark to the image.
   *
   * @param imageUrl     Watermark image URL. It is very important to understand that the same image
   *                     loader that Thumbor uses will be used here.
   * @param x            Horizontal position that the watermark will be in. Positive numbers indicate position
   *                     from the left and negative numbers indicate position from the right.
   * @param y            Vertical position that the watermark will be in. Positive numbers indicate position
   *                     from the top and negative numbers indicate position from the bottom.
   * @param transparency Watermark image transparency. Should be a number between 0 (fully opaque)
   *                     and 100 (fully transparent).
   * @return String representation of this filter.
   */
  public static String watermark(String imageUrl, int x, int y, int transparency) {
    if (imageUrl == null || imageUrl.length() == 0) {
      throw new IllegalArgumentException("Image URL must not be blank.");
    }
    if (transparency < 0 || transparency > 100) {
      throw new IllegalArgumentException("Transparency must be between 0 and 100, inclusive.");
    }
    return new StringBuilder(FILTER_WATERMARK).append("(") //
        .append(stripProtocolAndParams(imageUrl)).append(",") //
        .append(x).append(",") //
        .append(y).append(",") //
        .append(transparency).append(")") //
        .toString();
  }

  /**
   * This filter adds a watermark to the image.
   *
   * @param image        Watermark image URL. It is very important to understand that the same image
   *                     loader that Thumbor uses will be used here.
   * @param x            Horizontal position that the watermark will be in. Positive numbers indicate position
   *                     from the left and negative numbers indicate position from the right.
   * @param y            Vertical position that the watermark will be in. Positive numbers indicate position
   *                     from the top and negative numbers indicate position from the bottom.
   * @param transparency Watermark image transparency. Should be a number between 0 (fully opaque)
   *                     and 100 (fully transparent).
   * @return String representation of this filter.
   */
  public static String watermark(ThumborUri image, int x, int y, int transparency) {
    return watermark(image.toString(), x, y, transparency);
  }

  /**
   * This filter enhances apparent sharpness of the image. It's heavily based on Marco Rossini's
   * excellent Wavelet sharpen GIMP plugin. Check http://registry.gimp.org/node/9836 for details
   * about how it work.
   *
   * @param amount        Sharpen amount. Typical values are between 0.0 and 10.0.
   * @param radius        Sharpen radius. Typical values are between 0.0 and 2.0.
   * @param luminanceOnly Sharpen only luminance channel.
   * @return String representation of this filter.
   */
  public static String sharpen(float amount, float radius, boolean luminanceOnly) {
    return new StringBuilder(FILTER_SHARPEN).append("(") //
        .append(amount).append(",") //
        .append(radius).append(",") //
        .append(luminanceOnly).append(")") //
        .toString();
  }

  /**
   * This filter permit to return an image sized exactly as requested wherever is its ratio by
   * filling with chosen color the missing parts. Usually used with "fit-in" or "adaptive-fit-in"
   *
   * @param color integer representation of color.
   * @return String representation of this filter.
   */
  public static String fill(int color) {
    final String colorCode = Integer.toHexString(color & 0xFFFFFF); // Strip alpha
    return new StringBuilder(FILTER_FILL).append("(").append(colorCode).append(")").toString();
  }
}
