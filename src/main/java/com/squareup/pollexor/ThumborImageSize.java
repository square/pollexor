// Copyright 2013 Square, Inc.
package com.squareup.pollexor;

/**
 * Object that contains dimensions to which an image
 * should be resized.
 *
 * @see ThumborUrlBuilder#resize(ThumborImageSize)
 */
public class ThumborImageSize {

  public static final String ORIGINAL = "orig";

  String width = "orig";
  String height = "orig";

  /**
   * ImageDimension used to resize to image.
   * ORIGINAL specifies the original size of
   * a dimension should be retained while ZERO
   * specifies that a dimension should be rescaled
   * maintaining aspect ratio
   **/
  public enum ImageDimension {
    ORIGINAL("orig"), ZERO("0");

    private String value;

    private ImageDimension(String value) {
      this.value = value;
    }

  }

  /**
   * Resize the object using original width and height.
   */
  public ThumborImageSize() {
  }

  /**
   * Resize the object using provided width and height.
   */
  public ThumborImageSize(int width, int height) {
    if (width < 0) {
      throw new UnableToBuildException("Width must be a positive number.");
    }
    if (height < 0) {
      throw new UnableToBuildException("Height must be a positive number.");
    }
    if (width == 0 && height == 0) {
      throw new UnableToBuildException("Both width and height must not be zero.");
    }
    this.width = String.valueOf(width);
    this.height = String.valueOf(height);
  }

  /**
   * Resize the image with the provided values.
   * @param width int
   * @param height ImageDimension
   */
  public ThumborImageSize(int width, ImageDimension height) {
    if (width < 0) {
      throw new UnableToBuildException("Width must be a positive number.");
    }
    if (height == null) {
      throw new UnableToBuildException("Height cannot be null.");
    }
    if (width == 0 && height == ImageDimension.ZERO) {
      throw new UnableToBuildException("Both width and height must not be zero.");
    }
    this.width = String.valueOf(width);
    this.height = height.value;
  }

  /**
   * Resize the image with the provided values.
   * @param width ImageDimension
   * @param height int
   */
  public ThumborImageSize(ImageDimension width, int height) {
    if (width == null) {
      throw new UnableToBuildException("Width cannot be null.");
    }
    if (height < 0) {
      throw new UnableToBuildException("Height must be a positive number.");
    }
    if (height == 0 && width == ImageDimension.ZERO) {
      throw new UnableToBuildException("Both width and height must not be zero.");
    }
    this.width = width.value;
    this.height = String.valueOf(height);
  }

  public final String getWidth() {
    return width;
  }

  public final String getHeight() {
    return height;
  }

}
