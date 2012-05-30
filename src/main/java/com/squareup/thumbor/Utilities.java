// Copyright 2012 Square, Inc.
package com.squareup.thumbor;

import com.squareup.thumbor.Thumbor.UnableToBuildException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

/**
 * Utility methods for {@link Thumbor}.
 */
final class Utilities {
  private Utilities() {
    // No instances.
  }

  private final static String BASE64_CHARS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
  private final static int BASE64_UPPER_BOUND = Integer.MAX_VALUE / 4 * 3;

  /**
   * Base64 encodes a byte array.
   *
   * @param bytes Bytes to encode.
   * @return Encoded string.
   * @throws IllegalArgumentException if {@code bytes} is null or exceeds 3/4ths of {@code Integer.MAX_VALUE}.
   */
  public static String base64Encode(byte[] bytes) {
    if (bytes == null) {
      throw new IllegalArgumentException("Input bytes must not be null.");
    }
    if (bytes.length >= BASE64_UPPER_BOUND) {
      throw new IllegalArgumentException("Input bytes length must not exceed " + BASE64_UPPER_BOUND);
    }

    // Every three bytes is encoded into four characters.
    //
    // Example:
    //           input |0 1 0 1 0 0 1 0|0 1 1 0 1 1 1 1|0 1 1 0 0 0 1 0|
    // encode grouping |0 1 0 1 0 0|1 0 0 1 1 0|1 1 1 1 0 1|1 0 0 0 1 0|
    //   encoded ascii |     U     |     m     |     9     |     i     |

    int triples = bytes.length / 3;

    // If the number of input bytes is not a multiple of three, padding characters will be added.
    if (bytes.length % 3 != 0) {
      triples += 1;
    }

    // The encoded string will have four characters for every three bytes.
    char[] encoding = new char[triples << 2];

    for (int in = 0, out = 0; in < bytes.length; in += 3, out += 4) {
      int triple = (bytes[in] & 0xff) << 16;
      if (in + 1 < bytes.length) {
        triple |= ((bytes[in + 1] & 0xff) << 8);
      }
      if (in + 2 < bytes.length) {
        triple |= (bytes[in + 2] & 0xff);
      }
      encoding[out] = BASE64_CHARS.charAt((triple >> 18) & 0x3f);
      encoding[out + 1] = BASE64_CHARS.charAt((triple >> 12) & 0x3f);
      encoding[out + 2] = BASE64_CHARS.charAt((triple >> 6) & 0x3f);
      encoding[out + 3] = BASE64_CHARS.charAt(triple & 0x3f);
    }

    // Add padding characters if needed.
    for (int i = encoding.length - (triples * 3 - bytes.length); i < encoding.length; i++) {
      encoding[i] = '=';
    }

    return String.valueOf(encoding);
  }

  /**
   * Remove any protocol or parameters from the specified URL.
   *
   * @param url URL.
   * @return Stripped URL.
   */
  static String stripProtocolAndParams(String url) {
    if (url == null) {
      return null;
    }

    final int length = url.length();

    int start = 0;
    int end = length;

    if (url.startsWith("http://")) {
      start = 7;
    } else if (url.startsWith("https://")) {
      start = 8;
    }

    int param = url.indexOf("?");
    if (param != -1) {
      end = param;
    }
    param = url.indexOf("#");
    if (param != -1 && param < end) {
      end = param;
    }

    if (start > 0 || end < length) {
      return url.substring(start, end);
    }
    return url;
  }

  /**
   * Pad a {@link StringBuilder} to a desired multiple on the right using a specified character.
   *
   * @param builder Builder to pad.
   * @param padding Padding character.
   * @param multipleOf Number which the length must be a multiple of.
   * @throws IllegalArgumentException if {@code builder} is null or {@code multipleOf} is less than 2.
   */
  static void rightPadString(StringBuilder builder, char padding, int multipleOf) {
    if (builder == null) {
      throw new IllegalArgumentException("Builder input must not be empty.");
    }
    if (multipleOf < 2) {
      throw new IllegalArgumentException("Multiple must be greater than one.");
    }
    int needed = multipleOf - (builder.length() % multipleOf);
    if (needed < multipleOf) {
      for (int i = needed; i > 0; i--) {
        builder.append(padding);
      }
    }
  }

  /**
   * Normalize a string to a desired length by repeatedly appending itself and/or truncating.
   *
   * @param string Input string.
   * @param desiredLength Desired length of string.
   * @return Output string which is guaranteed to have a length equal to the desired length argument.
   * @throws IllegalArgumentException if {@code string} is blank or {@code desiredLength} is not greater than 0.
   */
  static String normalizeString(String string, int desiredLength) {
    if (string == null || string.length() == 0) {
      throw new IllegalArgumentException("Must supply a non-null, non-empty string.");
    }
    if (desiredLength <= 0) {
      throw new IllegalArgumentException("Desired length must be greater than zero.");
    }
    if (string.length() >= desiredLength) {
      return string.substring(0, desiredLength);
    } else {
      StringBuilder builder = new StringBuilder(string);
      while (builder.length() < desiredLength) {
        builder.append(string);
      }
      return builder.substring(0, desiredLength);
    }
  }

  /**
   * Create an MD5 hash of a string.
   *
   * @param input Input string.
   * @return Hash of input.
   * @throws IllegalArgumentException if {@code input} is blank.
   */
  static String md5(String input) {
    if (input == null || input.length() == 0) {
      throw new IllegalArgumentException("Input string must not be blank.");
    }
    try {
      MessageDigest algorithm = MessageDigest.getInstance("MD5");
      algorithm.reset();
      algorithm.update(input.getBytes());
      byte[] messageDigest = algorithm.digest();

      StringBuilder hexString = new StringBuilder();
      for (int i = 0; i < messageDigest.length; i++) {
        hexString.append(Integer.toHexString((messageDigest[i] & 0xFF) | 0x100).substring(1, 3));
      }
      return hexString.toString();
    } catch (Exception e) {
      throw new UnableToBuildException(e);
    }
  }

  /**
   * Encrypt a string using the specified key.
   *
   * @param message Input string.
   * @param key Encryption key.
   * @return Encrypted output.
   */
  static byte[] aes128Encrypt(StringBuilder message, String key) {
    try {
      rightPadString(message, '{', 16);
      byte[] messageBytes = message.toString().getBytes();
      SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, keySpec);

      byte[] cipherText = new byte[cipher.getOutputSize(messageBytes.length)];
      int ctLength = cipher.update(messageBytes, 0, messageBytes.length, cipherText, 0);
      ctLength += cipher.doFinal(cipherText, ctLength);
      return cipherText;
    } catch (Exception e) {
      throw new UnableToBuildException(e);
    }
  }
}
