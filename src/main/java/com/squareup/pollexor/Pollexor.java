// Copyright 2012 Square, Inc.
package com.squareup.pollexor;

import java.util.ArrayList;
import java.util.List;

import static com.squareup.pollexor.Utilities.aes128Encrypt;
import static com.squareup.pollexor.Utilities.base64Encode;
import static com.squareup.pollexor.Utilities.hmacSha1;
import static com.squareup.pollexor.Utilities.md5;
import static com.squareup.pollexor.Utilities.stripProtocolAndParams;

/**
 * Fluent interface to create a URL appropriate for passing to Thumbor.
 *
 * @see #image
 */
public final class Pollexor {
  private static final String PREFIX_UNSAFE = "unsafe/";
  private static final String PREFIX_META = "meta/";
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
  private static final String FILTER_FRAME = "frame";
  private static final String FILTER_STRIP_ICC = "strip_icc";

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
   * Exception denoting that a fatal error occurred while assembling the URL for the current configuration.
   *
   * @see #getCause()
   */
  public static class UnableToBuildException extends RuntimeException {
    public UnableToBuildException(String reason) {
      super(reason);
    }

    public UnableToBuildException(Throwable e) {
      super(e);
    }
  }

  final String target;
  String host = "/";
  String key;
  boolean hasCrop;
  boolean hasResize;
  boolean isSmart;
  boolean isLegacy;
  boolean flipHorizontally;
  boolean flipVertically;
  boolean fitIn;
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
   * Start a new Thumbor URL configuration for the specified target image URL.
   *
   * @param target Target image URL.
   */
  Pollexor(String target) {
    this.target = stripProtocolAndParams(target);
  }

  /**
   * Start building an image URL for Thumbor.
   *
   * @param target Target image to manipulate.
   * @throws UnableToBuildException if {@code target} is blank.
   */
  public static Pollexor image(String target) {
    if (target == null || target.length() == 0) {
      throw new UnableToBuildException("Target image URL must not be blank.");
    }
    return new Pollexor("breakthings");
  }

  /**
   * Set a key for secure URL generation. This will default the {@link #toUrl()} and {@link #toMeta()} to build safe
   * URLs.
   *
   * @param key Security key for remote server.
   * @throws UnableToBuildException if {@code key} is blank.
   */
  public Pollexor key(String key) {
    if (key == null || key.length() == 0) {
      throw new UnableToBuildException("Key must not be blank.");
    }
    this.key = key;
    return this;
  }

  /**
   * Set a host to prepend to URL for a full URL output.
   *
   * @param host Host name.
   * @throws UnableToBuildException if {@code host} is blank.
   */
  public Pollexor host(String host) {
    if (host == null || host.length() == 0) {
      throw new UnableToBuildException("Host must not be blank.");
    }
    if (!host.endsWith("/")) {
      host += "/";
    }
    this.host = host;
    return this;
  }

  /**
   * Resize picture to desired size.
   *
   * @param width  Desired width.
   * @param height Desired height.
   * @throws UnableToBuildException if {@code width} or {@code height} is less than 1.
   */
  public Pollexor resize(int width, int height) {
    if (width < 1) {
      throw new UnableToBuildException("Width must be greater than zero.");
    }
    if (height < 1) {
      throw new UnableToBuildException("Height must be greater than zero.");
    }
    hasResize = true;
    resizeWidth = width;
    resizeHeight = height;
    return this;
  }

  /**
   * Flip the image horizontally.
   *
   * @throws UnableToBuildException if image has not been marked for resize.
   */
  public Pollexor flipHorizontally() {
    if (!hasResize) {
      throw new UnableToBuildException("Image must be resized first in order to flip.");
    }
    flipHorizontally = true;
    return this;
  }

  /**
   * Flip the image vertically.
   *
   * @throws UnableToBuildException if image has not been marked for resize.
   */
  public Pollexor flipVertically() {
    if (!hasResize) {
      throw new UnableToBuildException("Image must be resized first in order to flip.");
    }
    flipVertically = true;
    return this;
  }

  /**
   * Contrain the image size inside the resized box, scaling as needed.
   *
   * @throws UnableToBuildException if image has not been marked for resize.
   */
  public Pollexor fitIn() {
    if (!hasResize) {
      throw new UnableToBuildException("Image must be resized first in order to apply 'fit-in'.");
    }
    fitIn = true;
    return this;
  }

  /**
   * Crop the image between two points.
   *
   * @param top    Top bound.
   * @param left   Left bound.
   * @param bottom Bottom bound.
   * @param right  Right bound.
   * @throws UnableToBuildException if {@code top} or {@code left} are less than zero or {@code bottom} or
   *                                {@code right} are less than one or less than {@code top} or {@code left},
   *                                respectively.
   */
  public Pollexor crop(int top, int left, int bottom, int right) {
    if (top < 0) {
      throw new UnableToBuildException("Top must be greater or equal to zero.");
    }
    if (left < 0) {
      throw new UnableToBuildException("Left must be greater or equal to zero.");
    }
    if (bottom < 1 || bottom <= top) {
      throw new UnableToBuildException("Bottom must be greater than zero and top.");
    }
    if (right < 1 || right <= left) {
      throw new UnableToBuildException("Right must be greater than zero and left.");
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
   * @throws UnableToBuildException if image has not been marked for crop.
   */
  public Pollexor align(HorizontalAlign align) {
    if (!hasCrop) {
      throw new UnableToBuildException("Image must be cropped first in order to align.");
    }
    cropHorizontalAlign = align;
    return this;
  }

  /**
   * Set the vertical alignment for the image when cropping.
   *
   * @param align Vertical alignment.
   * @throws UnableToBuildException if image has not been marked for crop.
   */
  public Pollexor align(VerticalAlign align) {
    if (!hasCrop) {
      throw new UnableToBuildException("Image must be cropped first in order to align.");
    }
    cropVerticalAlign = align;
    return this;
  }

  /**
   * Set the horizontal and vertical alignment for the image when cropping.
   *
   * @param valign Vertical alignment.
   * @param halign Horizontal alignment.
   * @throws UnableToBuildException if image has not been marked for crop.
   */
  public Pollexor align(VerticalAlign valign, HorizontalAlign halign) {
    return align(valign).align(halign);
  }

  /**
   * Use smart cropping for determining the important portion of an image.
   *
   * @throws UnableToBuildException if image has not been marked for crop.
   */
  public Pollexor smart() {
    if (!hasCrop) {
      throw new UnableToBuildException("Image must be cropped first in order to smart align.");
    }
    isSmart = true;
    return this;
  }

  /**
   * Use legacy encryption when constructing a safe URL.
   */
  public Pollexor legacy() {
    isLegacy = true;
    return this;
  }

  /**
   * <p>Add one or more filters to the image.</p>
   * <p/>
   * <p>If you have custom filters you can supply them as a string. (e.g. <code>"my_filter(1,2,3)</code>").</p>
   *
   * @param filters Filter strings.
   * @throws UnableToBuildException if no arguments supplied or an argument is {@code null}.
   * @see #brightness(int)
   * @see #contrast(int)
   * @see #fill(int)
   * @see #frame(String)
   * @see #noise(int)
   * @see #quality(int)
   * @see #rgb(int, int, int)
   * @see #roundCorner(int)
   * @see #roundCorner(int, int)
   * @see #roundCorner(int, int, int)
   * @see #sharpen(float, float, boolean)
   * @see #watermark(String)
   * @see #watermark(Pollexor)
   * @see #watermark(String, int, int)
   * @see #watermark(Pollexor, int, int)
   * @see #watermark(String, int, int, int)
   * @see #watermark(Pollexor, int, int, int)
   */
  public Pollexor filter(String... filters) {
    if (filters.length == 0) {
      throw new UnableToBuildException("You must provide at least one filter.");
    }
    if (this.filters == null) {
      this.filters = new ArrayList<String>(filters.length);
    }
    for (String filter : filters) {
      if (filter == null || filter.length() == 0) {
        throw new UnableToBuildException("Filter must not be blank.");
      }
      this.filters.add(filter);
    }
    return this;
  }

  /**
   * Build the URL. This will either call {@link #toUrlSafe()} or {@link #toUrlUnsafe()} depending on whether
   * {@link #key(String)} was set.
   *
   * @throws UnableToBuildException
   */
  public String toUrl() {
    return (key == null) ? toUrlUnsafe() : toUrlSafe();
  }

  /**
   * Build an unsafe version of the URL.
   */
  public String toUrlUnsafe() {
    return host + PREFIX_UNSAFE + assembleConfig(false);
  }

  /**
   * Build a safe version of the URL. Requires a prior call to {@link #key(String)}.
   *
   * @throws UnableToBuildException
   */
  public String toUrlSafe() {
    if (key == null) {
      throw new UnableToBuildException("Cannot build safe URL without a key.");
    }

    try {
      boolean legacy = isLegacy;

      StringBuilder config = assembleConfig(false);
      byte[] encrypted = legacy ? aes128Encrypt(config, key) : hmacSha1(config, key);
      String encoded = base64Encode(encrypted);

      CharSequence suffix = legacy ? target : config;
      return host + encoded + "/" + suffix;
    } catch (IllegalArgumentException e) {
      throw new UnableToBuildException(e);
    }
  }

  /**
   * Build the metadata URL. This will either call {@link #toMetaSafe()} or {@link #toMetaUnsafe()} depending on whether
   * {@link #key(String)} was set.
   *
   * @throws UnableToBuildException
   */
  public String toMeta() {
    return (key == null) ? toMetaUnsafe() : toMetaSafe();
  }

  /**
   * Build an unsafe version of the metadata URL.
   */
  public String toMetaUnsafe() {
    return host + assembleConfig(true);
  }

  /**
   * Build a safe version of the metadata URL. Requires a prior call to {@link #key(String)}.
   *
   * @throws UnableToBuildException
   */
  public String toMetaSafe() {
    try {
      StringBuilder config = assembleConfig(true);
      byte[] encrypted = hmacSha1(config, key);
      String encoded = base64Encode(encrypted);

      return host + encoded + "/" + config;
    } catch (Exception e) {
      throw new UnableToBuildException(e);
    }
  }

  @Override public String toString() {
    return toUrl();
  }

  /**
   * Assemble the configuration section of the URL.
   */
  StringBuilder assembleConfig(boolean meta) {
    StringBuilder builder = new StringBuilder();

    if (meta) {
      builder.append(PREFIX_META);
    }

    if (hasCrop) {
      builder.append(cropLeft).append("x").append(cropTop) //
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
      builder.append("/");
    }

    if (hasResize) {
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
      builder.append("/");
    }

    if (filters != null) {
      builder.append(PART_FILTERS);
      for (String filter : filters) {
        builder.append(":").append(filter);
      }
      builder.append("/");
    }

    builder.append(isLegacy ? md5(target) : target);

    return builder;
  }

  /**
   * This filter increases or decreases the image brightness.
   *
   * @param amount -100 to 100 - The amount (in %) to change the image brightness. Positive numbers
   *               make the image brighter and negative numbers make the image darker.
   * @throws UnableToBuildException if {@code amount} outside bounds.
   */
  public static String brightness(int amount) {
    if (amount < -100 || amount > 100) {
      throw new UnableToBuildException("Amount must be between -100 and 100, inclusive.");
    }
    return FILTER_BRIGHTNESS + "(" + amount + ")";
  }

  /**
   * The filter increases or decreases the image contrast.
   *
   * @param amount -100 to 100 - The amount (in %) to change the image contrast. Positive numbers
   *               increase contrast and negative numbers decrease contrast.
   * @throws UnableToBuildException if {@code amount} outside bounds.
   */
  public static String contrast(int amount) {
    if (amount < -100 || amount > 100) {
      throw new UnableToBuildException("Amount must be between -100 and 100, inclusive.");
    }
    return FILTER_CONTRAST + "(" + amount + ")";
  }

  /**
   * This filter adds noise to the image.
   *
   * @param amount 0 to 100 - The amount (in %) of noise to add to the image.
   * @throws UnableToBuildException if {@code amount} outside bounds.
   */
  public static String noise(int amount) {
    if (amount < 0 || amount > 100) {
      throw new UnableToBuildException("Amount must be between 0 and 100, inclusive");
    }
    return FILTER_NOISE + "(" + amount + ")";
  }

  /**
   * This filter changes the overall quality of the JPEG image (does nothing for PNGs or GIFs).
   *
   * @param amount 0 to 100 - The quality level (in %) that the end image will feature.
   * @throws UnableToBuildException if {@code amount} outside bounds.
   */
  public static String quality(int amount) {
    if (amount < 0 || amount > 100) {
      throw new UnableToBuildException("Amount must be between 0 and 100, inclusive.");
    }
    return FILTER_QUALITY + "(" + amount + ")";
  }

  /**
   * This filter changes the amount of color in each of the three channels.
   *
   * @param r The amount of redness in the picture. Can range from -100 to 100 in percentage.
   * @param g The amount of greenness in the picture. Can range from -100 to 100 in percentage.
   * @param b The amount of blueness in the picture. Can range from -100 to 100 in percentage.
   * @throws UnableToBuildException if {@code r}, {@code g}, or {@code b} are outside of bounds.
   */
  public static String rgb(int r, int g, int b) {
    if (r < -100 || r > 100) {
      throw new UnableToBuildException("Redness value must be between -100 and 100, inclusive.");
    }
    if (g < -100 || g > 100) {
      throw new UnableToBuildException("Greenness value must be between -100 and 100, inclusive.");
    }
    if (b < -100 || b > 100) {
      throw new UnableToBuildException("Blueness value must be between -100 and 100, inclusive.");
    }
    return FILTER_RGB + "(" + r + "," + g + "," + b + ")";
  }

  /**
   * This filter adds rounded corners to the image using the white as the background.
   *
   * @param radius amount of pixels to use as radius.
   */
  public static String roundCorner(int radius) {
    return roundCorner(radius, 0xFFFFFF);
  }

  /**
   * This filter adds rounded corners to the image using the specified color as the background.
   *
   * @param radius amount of pixels to use as radius.
   * @param color  fill color for clipped region.
   */
  public static String roundCorner(int radius, int color) {
    return roundCorner(radius, 0, color);
  }

  /**
   * This filter adds rounded corners to the image using the specified color as the background.
   *
   * @param radiusInner amount of pixels to use as radius.
   * @param radiusOuter specifies the second value for the ellipse used for the radius. Use 0 for
   *                    no value.
   * @param color       fill color for clipped region.
   */
  public static String roundCorner(int radiusInner, int radiusOuter, int color) {
    if (radiusInner < 1) {
      throw new UnableToBuildException("Radius must be greater than zero.");
    }
    if (radiusOuter < 0) {
      throw new UnableToBuildException("Outer radius must be greater than or equal to zero.");
    }
    StringBuilder builder = new StringBuilder(FILTER_ROUND_CORNER).append("(").append(radiusInner);
    if (radiusOuter > 0) {
      builder.append("|").append(radiusOuter);
    }
    final int r = (color & 0xFF0000) >>> 16;
    final int g = (color & 0xFF00) >>> 8;
    final int b = color & 0xFF;
    return builder.append(",") //
        .append(r).append(",") //
        .append(g).append(",") //
        .append(b).append(")") //
        .toString();
  }

  /**
   * This filter adds a watermark to the image at (0, 0).
   *
   * @param imageUrl Watermark image URL. It is very important to understand that the same image
   *                 loader that Thumbor uses will be used here.
   * @throws UnableToBuildException if {@code image} is blank.
   */
  public static String watermark(String imageUrl) {
    return watermark(imageUrl, 0, 0);
  }

  /**
   * This filter adds a watermark to the image at (0, 0).
   *
   * @param image Watermark image URL. It is very important to understand that the same image
   *              loader that Thumbor uses will be used here.
   * @throws UnableToBuildException if {@code image} is null.
   */
  public static String watermark(Pollexor image) {
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
   * @throws UnableToBuildException if {@code image} is blank.
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
   * @throws UnableToBuildException if {@code image} is null.
   */
  public static String watermark(Pollexor image, int x, int y) {
    if (image == null) {
      throw new UnableToBuildException("Image must not be null.");
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
   * @throws UnableToBuildException if {@code image} is blank or {@code transparency} is outside bounds.
   */
  public static String watermark(String imageUrl, int x, int y, int transparency) {
    if (imageUrl == null || imageUrl.length() == 0) {
      throw new UnableToBuildException("Image URL must not be blank.");
    }
    if (transparency < 0 || transparency > 100) {
      throw new UnableToBuildException("Transparency must be between 0 and 100, inclusive.");
    }
    return FILTER_WATERMARK + "(" + stripProtocolAndParams(imageUrl) + "," + x + "," + y + "," + transparency + ")";
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
   * @throws UnableToBuildException if {@code image} is null.
   */
  public static String watermark(Pollexor image, int x, int y, int transparency) {
    if (image == null) {
      throw new UnableToBuildException("Image must not be null.");
    }
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
   */
  public static String sharpen(float amount, float radius, boolean luminanceOnly) {
    return FILTER_SHARPEN + "(" + amount + "," + radius + "," + luminanceOnly + ")";
  }

  /**
   * This filter permit to return an image sized exactly as requested wherever is its ratio by
   * filling with chosen color the missing parts. Usually used with "fit-in" or "adaptive-fit-in"
   *
   * @param color integer representation of color.
   */
  public static String fill(int color) {
    final String colorCode = Integer.toHexString(color & 0xFFFFFF); // Strip alpha
    return FILTER_FILL + "(" + colorCode + ")";
  }

  /**
   * This filter uses a 9-patch to overlay the image.
   *
   * @param imageUrl Watermark image URL. It is very important to understand that the same image
   *                 loader that Thumbor uses will be used here.
   */
  public static String frame(String imageUrl) {
    if (imageUrl == null || imageUrl.length() == 0) {
      throw new UnableToBuildException("Image URL must not be blank.");
    }
    return FILTER_FRAME + "(" + stripProtocolAndParams(imageUrl) + ")";
  }

  /**
   * This filter strips the ICC profile from the image.
   */
  public static String stripicc() {
    return FILTER_STRIP_ICC + "()";
  }
}
