// Copyright 2012 Square, Inc.
package com.squareup.pollexor;

import java.util.ArrayList;
import java.util.List;

import static com.squareup.pollexor.Utilities.aes128Encrypt;
import static com.squareup.pollexor.Utilities.base64Encode;
import static com.squareup.pollexor.Utilities.hmacSha1;
import static com.squareup.pollexor.Utilities.md5;

/**
 * Fluent interface to build a Thumbor URL.
 *
 * @see Thumbor#buildImage(String)
 */
public final class ThumborUrlBuilder {
  private static final String PREFIX_UNSAFE = "unsafe/";
  private static final String PREFIX_META = "meta/";
  private static final String PART_SMART = "smart";
  private static final String PART_TRIM = "trim";
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
  private static final String FILTER_FORMAT = "format";
  private static final String FILTER_FRAME = "frame";
  private static final String FILTER_STRIP_ICC = "strip_icc";
  private static final String FILTER_GRAYSCALE = "grayscale";
  private static final String FILTER_EQUALIZE = "equalize";
  private static final String FILTER_BLUR = "blur";
  private static final String FILTER_NO_UPSCALE = "no_upscale";
  private static final String FILTER_ROTATE = "rotate";

  /** Original size for image width or height. **/
  public static final int ORIGINAL_SIZE = Integer.MIN_VALUE;

  /** Horizontal alignment for crop positioning. */
  public enum HorizontalAlign {
    LEFT("left"), CENTER("center"), RIGHT("right");

    final String value;

    HorizontalAlign(String value) {
      this.value = value;
    }
  }

  /** Vertical alignment for crop positioning. */
  public enum VerticalAlign {
    TOP("top"), MIDDLE("middle"), BOTTOM("bottom");

    final String value;

    VerticalAlign(String value) {
      this.value = value;
    }
  }

  /** Orientation from where to get the pixel color for trim. */
  public enum TrimPixelColor {
    TOP_LEFT("top-left"), BOTTOM_RIGHT("bottom-right");

    final String value;

    TrimPixelColor(String value) {
      this.value = value;
    }
  }

  /** Image formats supported by Thumbor. */
  public enum ImageFormat {
    GIF("gif"), JPEG("jpeg"), PNG("png"), WEBP("webp");

    final String value;

    ImageFormat(String value) {
      this.value = value;
    }
  }

  /** Style of resizing for 'fit-in'. */
  public enum FitInStyle {
    NORMAL("fit-in"), FULL("full-fit-in"), ADAPTIVE("adaptive-fit-in");

    final String value;

    FitInStyle(String value) {
      this.value = value;
    }
  }

  final String image;
  final String host;
  final String key;
  boolean hasCrop;
  boolean hasResize;
  boolean isSmart;
  boolean isTrim;
  boolean isLegacy;
  boolean flipHorizontally;
  boolean flipVertically;
  FitInStyle fitInStyle;
  int resizeWidth;
  int resizeHeight;
  int cropTop;
  int cropLeft;
  int cropBottom;
  int cropRight;
  int trimColorTolerance;
  HorizontalAlign cropHorizontalAlign;
  VerticalAlign cropVerticalAlign;
  TrimPixelColor trimPixelColor;
  List<String> filters;

  ThumborUrlBuilder(String host, String key, String image) {
    this.host = host;
    this.key = key;
    this.image = image;
  }

  /**
   * Resize picture to desired size.
   *
   * @param width Desired width.
   * @param height Desired height.
   * @throws IllegalArgumentException if {@code width} or {@code height} is less than 0 or both are
   * 0.
   */
  public ThumborUrlBuilder resize(int width, int height) {
    if (width < 0 && width != ORIGINAL_SIZE) {
      throw new IllegalArgumentException("Width must be a positive number.");
    }
    if (height < 0 && height != ORIGINAL_SIZE) {
      throw new IllegalArgumentException("Height must be a positive number.");
    }
    if (width == 0 && height == 0) {
      throw new IllegalArgumentException("Both width and height must not be zero.");
    }
    hasResize = true;
    resizeWidth = width;
    resizeHeight = height;
    return this;
  }

  /**
   * Flip the image horizontally.
   *
   * @throws IllegalStateException if image has not been marked for resize.
   */
  public ThumborUrlBuilder flipHorizontally() {
    if (!hasResize) {
      throw new IllegalStateException("Image must be resized first in order to flip.");
    }
    flipHorizontally = true;
    return this;
  }

  /**
   * Flip the image vertically.
   *
   * @throws IllegalStateException if image has not been marked for resize.
   */
  public ThumborUrlBuilder flipVertically() {
    if (!hasResize) {
      throw new IllegalStateException("Image must be resized first in order to flip.");
    }
    flipVertically = true;
    return this;
  }

  /**
   * Constrain the image size inside the resized box, scaling as needed.
   *
   * @throws IllegalStateException if image has not been marked for resize.
   */
  public ThumborUrlBuilder fitIn() {
    return fitIn(FitInStyle.NORMAL);
  }

  /**
   * Constrain the image size inside the resized box, scaling as needed.
   *
   * @throws IllegalStateException if image has not been marked for resize.
   */
  public ThumborUrlBuilder fitIn(FitInStyle fitInStyle) {
    if (!hasResize) {
      throw new IllegalStateException("Image must be resized first in order to apply 'fit-in'.");
    }
    this.fitInStyle = fitInStyle;
    return this;
  }

  /**
   * Crop the image between two points.
   *
   * @param top Top bound.
   * @param left Left bound.
   * @param bottom Bottom bound.
   * @param right Right bound.
   * @throws IllegalArgumentException if {@code top} or {@code left} are less than zero or {@code
   * bottom} or {@code right} are less than one or less than {@code top} or {@code left},
   * respectively.
   */
  public ThumborUrlBuilder crop(int top, int left, int bottom, int right) {
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
   * Set the horizontal alignment for the image when image gets cropped by resizing.
   *
   * @param align Horizontal alignment.
   * @throws IllegalStateException if image has not been marked for resize.
   */
  public ThumborUrlBuilder align(HorizontalAlign align) {
    if (!hasResize) {
      throw new IllegalStateException("Image must be resized first in order to align.");
    }
    cropHorizontalAlign = align;
    return this;
  }

  /**
   * Set the vertical alignment for the image when image gets cropped by resizing.
   *
   * @param align Vertical alignment.
   * @throws IllegalStateException if image has not been marked for resize.
   */
  public ThumborUrlBuilder align(VerticalAlign align) {
    if (!hasResize) {
      throw new IllegalStateException("Image must be resized first in order to align.");
    }
    cropVerticalAlign = align;
    return this;
  }

  /**
   * Set the horizontal and vertical alignment for the image when image gets cropped by resizing.
   *
   * @param valign Vertical alignment.
   * @param halign Horizontal alignment.
   * @throws IllegalStateException if image has not been marked for resize.
   */
  public ThumborUrlBuilder align(VerticalAlign valign, HorizontalAlign halign) {
    return align(valign).align(halign);
  }

  /**
   * Use smart cropping for determining the important portion of an image.
   *
   * @throws IllegalStateException if image has not been marked for resize.
   */
  public ThumborUrlBuilder smart() {
    if (!hasResize) {
      throw new IllegalStateException("Image must be resized first in order to smart align.");
    }
    isSmart = true;
    return this;
  }

  /**
   * Removing surrounding space in image.
   */
  public ThumborUrlBuilder trim() {
    return trim(null);
  }

  /**
   * Removing surrounding space in image. Get trim color from specified pixel.
   * @param value orientation from where to get the pixel color.
   */
  public ThumborUrlBuilder trim(TrimPixelColor value) {
    return trim(value, 0);
  }

  /**
   * Removing surrounding space in image. Get trim color from specified pixel.
   * @param value orientation from where to get the pixel color.
   * @param colorTolerance 0 - 442. This is the euclidian distance
   * between the colors of the reference pixel and the surrounding pixels is used.
   * If the distance is within the tolerance they'll get trimmed.
   */
  public ThumborUrlBuilder trim(TrimPixelColor value, int colorTolerance) {
    if (colorTolerance < 0 || colorTolerance > 442) {
      throw new IllegalArgumentException("Color tolerance must be between 0 and 442.");
    }
    if (colorTolerance > 0 && value == null) {
      throw new IllegalArgumentException("Trim pixel color value must not be null.");
    }
    isTrim = true;
    trimPixelColor = value;
    trimColorTolerance = colorTolerance;
    return this;
  }

  /** Use legacy encryption when constructing a safe URL. */
  public ThumborUrlBuilder legacy() {
    isLegacy = true;
    return this;
  }

  /**
   * Add one or more filters to the image.
   * <p>
   * If you have custom filters you can supply them as a string. (e.g.
   * <code>"my_filter(1,2,3)</code>").
   *
   * @param filters Filter strings.
   * @throws IllegalArgumentException if no arguments supplied or an argument is {@code null}.
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
   * @see #watermark(ThumborUrlBuilder)
   * @see #watermark(String, int, int)
   * @see #watermark(ThumborUrlBuilder, int, int)
   * @see #watermark(String, int, int, int)
   * @see #watermark(ThumborUrlBuilder, int, int, int)
   */
  public ThumborUrlBuilder filter(String... filters) {
    if (filters.length == 0) {
      throw new IllegalArgumentException("You must provide at least one filter.");
    }
    if (this.filters == null) {
      this.filters = new ArrayList<String>(filters.length);
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
   * Build the URL. This will either call {@link #toUrlSafe()} or {@link #toUrlUnsafe()} depending
   * on whether a key was set.
   */
  public String toUrl() {
    return (key == null) ? toUrlUnsafe() : toUrlSafe();
  }

  /** Build an unsafe version of the URL. */
  public String toUrlUnsafe() {
    return host + PREFIX_UNSAFE + assembleConfig(false);
  }

  /**
   * Build a safe version of the URL. Requires a non-{@code null} key.
   */
  public String toUrlSafe() {
    if (key == null) {
      throw new IllegalStateException("Cannot build safe URL without a key.");
    }

    boolean legacy = isLegacy;

    StringBuilder config = assembleConfig(false);
    byte[] encrypted = legacy ? aes128Encrypt(config, key) : hmacSha1(config, key);
    String encoded = base64Encode(encrypted);

    CharSequence suffix = legacy ? image : config;
    return host + encoded + "/" + suffix;
  }

  /**
   * Build the metadata URL. This will either call {@link #toMetaSafe()} or {@link #toMetaUnsafe()}
   * depending on whether a key was set.
   */
  public String toMeta() {
    return (key == null) ? toMetaUnsafe() : toMetaSafe();
  }

  /** Build an unsafe version of the metadata URL. */
  public String toMetaUnsafe() {
    return host + PREFIX_UNSAFE + assembleConfig(true);
  }

  /**
   * Build a safe version of the metadata URL. Requires a non-{@code null} key.
   */
  public String toMetaSafe() {
    StringBuilder config = assembleConfig(true);
    byte[] encrypted = hmacSha1(config, key);
    String encoded = base64Encode(encrypted);

    return host + encoded + "/" + config;
  }

  @Override public String toString() {
    return toUrl();
  }

  /** Assemble the configuration section of the URL. */
  StringBuilder assembleConfig(boolean meta) {
    StringBuilder builder = new StringBuilder();

    if (meta) {
      builder.append(PREFIX_META);
    }

    if (isTrim) {
      builder.append(PART_TRIM);
      if (trimPixelColor != null) {
        builder.append(":").append(trimPixelColor.value);
        if (trimColorTolerance > 0) {
          builder.append(":").append(trimColorTolerance);
        }
      }
      builder.append("/");
    }

    if (hasCrop) {
      builder.append(cropLeft).append("x").append(cropTop) //
          .append(":").append(cropRight).append("x").append(cropBottom);

      builder.append("/");
    }

    if (hasResize) {
      if (fitInStyle != null) {
        builder.append(fitInStyle.value).append("/");
      }
      if (flipHorizontally) {
        builder.append("-");
      }
      if (resizeWidth == ORIGINAL_SIZE) {
        builder.append("orig");
      } else {
        builder.append(resizeWidth);
      }
      builder.append("x");
      if (flipVertically) {
        builder.append("-");
      }
      if (resizeHeight == ORIGINAL_SIZE) {
        builder.append("orig");
      } else {
        builder.append(resizeHeight);
      }
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

    if (filters != null) {
      builder.append(PART_FILTERS);
      for (String filter : filters) {
        builder.append(":").append(filter);
      }
      builder.append("/");
    }

    builder.append(isLegacy ? md5(image) : image);

    return builder;
  }

  /**
   * This filter increases or decreases the image brightness.
   *
   * @param amount -100 to 100 - The amount (in %) to change the image brightness. Positive numbers
   * make the image brighter and negative numbers make the image darker.
   * @throws IllegalArgumentException if {@code amount} outside bounds.
   */
  public static String brightness(int amount) {
    if (amount < -100 || amount > 100) {
      throw new IllegalArgumentException("Amount must be between -100 and 100, inclusive.");
    }
    return FILTER_BRIGHTNESS + "(" + amount + ")";
  }

  /**
   * The filter increases or decreases the image contrast.
   *
   * @param amount -100 to 100 - The amount (in %) to change the image contrast. Positive numbers
   * increase contrast and negative numbers decrease contrast.
   * @throws IllegalArgumentException if {@code amount} outside bounds.
   */
  public static String contrast(int amount) {
    if (amount < -100 || amount > 100) {
      throw new IllegalArgumentException("Amount must be between -100 and 100, inclusive.");
    }
    return FILTER_CONTRAST + "(" + amount + ")";
  }

  /**
   * This filter adds noise to the image.
   *
   * @param amount 0 to 100 - The amount (in %) of noise to add to the image.
   * @throws IllegalArgumentException if {@code amount} outside bounds.
   */
  public static String noise(int amount) {
    if (amount < 0 || amount > 100) {
      throw new IllegalArgumentException("Amount must be between 0 and 100, inclusive");
    }
    return FILTER_NOISE + "(" + amount + ")";
  }

  /**
   * This filter changes the overall quality of the JPEG image (does nothing for PNGs or GIFs).
   *
   * @param amount 0 to 100 - The quality level (in %) that the end image will feature.
   * @throws IllegalArgumentException if {@code amount} outside bounds.
   */
  public static String quality(int amount) {
    if (amount < 0 || amount > 100) {
      throw new IllegalArgumentException("Amount must be between 0 and 100, inclusive.");
    }
    return FILTER_QUALITY + "(" + amount + ")";
  }

  /**
   * This filter changes the amount of color in each of the three channels.
   *
   * @param r The amount of redness in the picture. Can range from -100 to 100 in percentage.
   * @param g The amount of greenness in the picture. Can range from -100 to 100 in percentage.
   * @param b The amount of blueness in the picture. Can range from -100 to 100 in percentage.
   * @throws IllegalArgumentException if {@code r}, {@code g}, or {@code b} are outside of bounds.
   */
  public static String rgb(int r, int g, int b) {
    if (r < -100 || r > 100) {
      throw new IllegalArgumentException("Red value must be between -100 and 100, inclusive.");
    }
    if (g < -100 || g > 100) {
      throw new IllegalArgumentException("Green value must be between -100 and 100, inclusive.");
    }
    if (b < -100 || b > 100) {
      throw new IllegalArgumentException("Blue value must be between -100 and 100, inclusive.");
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
   * @param color fill color for clipped region.
   */
  public static String roundCorner(int radius, int color) {
    return roundCorner(radius, 0, color);
  }

  /**
   * This filter adds rounded corners to the image using the specified color as the background.
   *
   * @param radiusInner amount of pixels to use as radius.
   * @param radiusOuter specifies the second value for the ellipse used for the radius. Use 0 for
   * no value.
   * @param color fill color for clipped region.
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
   * loader that Thumbor uses will be used here.
   * @throws IllegalArgumentException if {@code image} is blank.
   */
  public static String watermark(String imageUrl) {
    return watermark(imageUrl, 0, 0);
  }

  /**
   * This filter adds a watermark to the image at (0, 0).
   *
   * @param image Watermark image URL. It is very important to understand that the same image
   * loader that Thumbor uses will be used here.
   * @throws IllegalArgumentException if {@code image} is null.
   */
  public static String watermark(ThumborUrlBuilder image) {
    return watermark(image, 0, 0);
  }

  /**
   * This filter adds a watermark to the image.
   *
   * @param imageUrl Watermark image URL. It is very important to understand that the same image
   * loader that Thumbor uses will be used here.
   * @param x Horizontal position that the watermark will be in. Positive numbers indicate position
   * from the left and negative numbers indicate position from the right.
   * @param y Vertical position that the watermark will be in. Positive numbers indicate position
   * from the top and negative numbers indicate position from the bottom.
   * @throws IllegalArgumentException if {@code image} is blank.
   */
  public static String watermark(String imageUrl, int x, int y) {
    return watermark(imageUrl, x, y, 0);
  }

  /**
   * This filter adds a watermark to the image.
   *
   * @param image Watermark image URL. It is very important to understand that the same image
   * loader that Thumbor uses will be used here.
   * @param x Horizontal position that the watermark will be in. Positive numbers indicate position
   * from the left and negative numbers indicate position from the right.
   * @param y Vertical position that the watermark will be in. Positive numbers indicate position
   * from the top and negative numbers indicate position from the bottom.
   * @throws IllegalArgumentException if {@code image} is null.
   */
  public static String watermark(ThumborUrlBuilder image, int x, int y) {
    if (image == null) {
      throw new IllegalArgumentException("Image must not be null.");
    }
    return watermark(image.toString(), x, y, 0);
  }

  /**
   * This filter adds a watermark to the image.
   *
   * @param imageUrl Watermark image URL. It is very important to understand that the same image
   * loader that Thumbor uses will be used here.
   * @param x Horizontal position that the watermark will be in. Positive numbers indicate position
   * from the left and negative numbers indicate position from the right.
   * @param y Vertical position that the watermark will be in. Positive numbers indicate position
   * from the top and negative numbers indicate position from the bottom.
   * @param transparency Watermark image transparency. Should be a number between 0 (fully opaque)
   * and 100 (fully transparent).
   * @throws IllegalArgumentException if {@code image} is blank or {@code transparency} is outside
   * bounds.
   */
  public static String watermark(String imageUrl, int x, int y, int transparency) {
    if (imageUrl == null || imageUrl.length() == 0) {
      throw new IllegalArgumentException("Image URL must not be blank.");
    }
    if (transparency < 0 || transparency > 100) {
      throw new IllegalArgumentException("Transparency must be between 0 and 100, inclusive.");
    }
    return FILTER_WATERMARK + "(" + imageUrl + "," + x + "," + y + "," + transparency + ")";
  }

  /**
   * This filter adds a watermark to the image.
   *
   * @param image Watermark image URL. It is very important to understand that the same image
   * loader that Thumbor uses will be used here.
   * @param x Horizontal position that the watermark will be in. Positive numbers indicate position
   * from the left and negative numbers indicate position from the right.
   * @param y Vertical position that the watermark will be in. Positive numbers indicate position
   * from the top and negative numbers indicate position from the bottom.
   * @param transparency Watermark image transparency. Should be a number between 0 (fully opaque)
   * and 100 (fully transparent).
   * @throws IllegalArgumentException if {@code image} is null.
   */
  public static String watermark(ThumborUrlBuilder image, int x, int y, int transparency) {
    if (image == null) {
      throw new IllegalArgumentException("Image must not be null.");
    }
    return watermark(image.toString(), x, y, transparency);
  }

  /**
   * This filter enhances apparent sharpness of the image. It's heavily based on Marco Rossini's
   * excellent Wavelet sharpen GIMP plugin. Check http://registry.gimp.org/node/9836 for details
   * about how it work.
   *
   * @param amount Sharpen amount. Typical values are between 0.0 and 10.0.
   * @param radius Sharpen radius. Typical values are between 0.0 and 2.0.
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
   * Specify the output format of the image.
   *
   * @see ImageFormat
   */
  public static String format(ImageFormat format) {
    if (format == null) {
      throw new IllegalArgumentException("You must specify an image format.");
    }
    return FILTER_FORMAT + "(" + format.value + ")";
  }

  /**
   * This filter uses a 9-patch to overlay the image.
   *
   * @param imageUrl Watermark image URL. It is very important to understand that the same image
   * loader that Thumbor uses will be used here.
   */
  public static String frame(String imageUrl) {
    if (imageUrl == null || imageUrl.length() == 0) {
      throw new IllegalArgumentException("Image URL must not be blank.");
    }
    return FILTER_FRAME + "(" + imageUrl + ")";
  }

  /** This filter strips the ICC profile from the image. */
  public static String stripicc() {
    return FILTER_STRIP_ICC + "()";
  }

  /** This filter changes the image to grayscale. */
  public static String grayscale() {
    return FILTER_GRAYSCALE + "()";
  }

  /** This filter equalizes the color distribution in the image. */
  public static String equalize() {
    return FILTER_EQUALIZE + "()";
  }

  /**
   * This filter adds a blur effect to the image using the specified radius and a default
   * sigma (equal to the radius).
   * @param radius Radius used in the gaussian function to generate a matrix, maximum value is 150.
   *               The bigger the radius more blurred will be the image.
   */
  public static String blur(int radius) {
    return blur(radius, 0);
  }

  /**
   * This filter adds a blur effect to the image using the specified radius and sigma.
   * @param radius Radius used in the gaussian function to generate a matrix, maximum value is 150.
   *               The bigger the radius more blurred will be the image.
   * @param sigma Sigma used in the gaussian function.
   */
  public static String blur(int radius, int sigma) {
    if (radius < 1) {
      throw new IllegalArgumentException("Radius must be greater than zero.");
    }
    if (radius > 150) {
      throw new IllegalArgumentException("Radius must be lower or equal than 150.");
    }
    if (sigma < 0) {
      throw new IllegalArgumentException("Sigma must be greater than zero.");
    }
    return FILTER_BLUR + "(" + radius + "," + sigma + ")";
  }

  /** This filter tells thumbor not to upscale your images. */
  public static String noUpscale() {
    return FILTER_NO_UPSCALE + "()";
  }

  /**
   * This filter rotates the given image according to the angle passed.
   * @param angle The angle of rotation. Values can be either 0°, 90°, 180° or 270° – multiples of
   *              90°. Angles equal to or greater than 360° will be replaced by their coterminal
   *              angle of rotation.
   */
  public static String rotate(int angle) {
    if (angle % 90 != 0) {
      throw new IllegalArgumentException("Angle must be multiple of 90°");
    }
    return FILTER_ROTATE + "(" + angle + ")";
  }
}
