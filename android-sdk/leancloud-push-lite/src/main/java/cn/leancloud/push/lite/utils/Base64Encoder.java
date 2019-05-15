package cn.leancloud.push.lite.utils;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class Base64Encoder extends FilterOutputStream {

  private static final char[] chars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
      'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
      'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
      'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

  private int charCount;
  private int carryOver;

  /***
   * Constructs a new Base64 encoder that writes output to the given OutputStream.
   *
   * @param out the output stream
   */
  public Base64Encoder(OutputStream out) {
    super(out);
  }

  /***
   * Writes the given byte to the output stream in an encoded form.
   *
   * @exception IOException if an I/O error occurs
   */
  public void write(int b) throws IOException {
    // Take 24-bits from three octets, translate into four encoded chars
    // Break lines at 76 chars
    // If necessary, pad with 0 bits on the right at the end
    // Use = signs as padding at the end to ensure encodedLength % 4 == 0

    // Remove the sign bit,
    // thanks to Christian Schweingruber <chrigu@lorraine.ch>
    if (b < 0) {
      b += 256;
    }

    // First byte use first six bits, save last two bits
    if (charCount % 3 == 0) {
      int lookup = b >> 2;
      carryOver = b & 3; // last two bits
      out.write(chars[lookup]);
    }
    // Second byte use previous two bits and first four new bits,
    // save last four bits
    else if (charCount % 3 == 1) {
      int lookup = ((carryOver << 4) + (b >> 4)) & 63;
      carryOver = b & 15; // last four bits
      out.write(chars[lookup]);
    }
    // Third byte use previous four bits and first two new bits,
    // then use last six new bits
    else if (charCount % 3 == 2) {
      int lookup = ((carryOver << 2) + (b >> 6)) & 63;
      out.write(chars[lookup]);
      lookup = b & 63; // last six bits
      out.write(chars[lookup]);
      carryOver = 0;
    }
    charCount++;

    // Add newline every 76 output chars (that's 57 input chars)
    if (charCount % 57 == 0) {
      out.write('\n');
    }
  }

  /***
   * Writes the given byte array to the output stream in an encoded form.
   *
   * @param buf the data to be written
   * @param off the start offset of the data
   * @param len the length of the data
   * @exception IOException if an I/O error occurs
   */
  public void write(byte[] buf, int off, int len) throws IOException {
    // This could of course be optimized
    for (int i = 0; i < len; i++) {
      write(buf[off + i]);
    }
  }

  /***
   * Closes the stream, this MUST be called to ensure proper padding is written to the end of the
   * output stream.
   *
   * @exception IOException if an I/O error occurs
   */
  public void close() throws IOException {
    // Handle leftover bytes
    if (charCount % 3 == 1) { // one leftover
      int lookup = (carryOver << 4) & 63;
      out.write(chars[lookup]);
      out.write('=');
      out.write('=');
    } else if (charCount % 3 == 2) { // two leftovers
      int lookup = (carryOver << 2) & 63;
      out.write(chars[lookup]);
      out.write('=');
    }
    super.close();
  }

  /***
   * Returns the encoded form of the given unencoded string. The encoder uses the ISO-8859-1
   * (Latin-1) encoding to convert the string to bytes. For greater control over the encoding,
   * encode the string to bytes yourself and use encode(byte[]).
   *
   * @param unencoded the string to encode
   * @return the encoded form of the unencoded string
   */
  public static String encode(String unencoded) {
    byte[] bytes = null;
    try {
      bytes = unencoded.getBytes("UTF-8");
    } catch (UnsupportedEncodingException ignored) {}
    return encode(bytes);
  }

  /***
   * Returns the encoded form of the given unencoded string.
   *
   * @param bytes the bytes to encode
   * @return the encoded form of the unencoded string
   */
  public static String encode(byte[] bytes) {
    ByteArrayOutputStream out = new ByteArrayOutputStream((int) (bytes.length * 1.37));
    Base64Encoder encodedOut = new Base64Encoder(out);

    try {
      encodedOut.write(bytes);
      encodedOut.close();

      return out.toString("UTF-8");
    } catch (IOException ignored) {
      return null;
    }
  }
}
