package cn.leancloud.query;

import okio.Buffer;

public class AVRequestParams {
  private static final char[] HEX_DIGITS =
          {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  static final String QUERY_COMPONENT_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";

  public static String urlEncode(String param) {
    return canonicalize(param, QUERY_COMPONENT_ENCODE_SET, false, true);
  }

  static String canonicalize(String input, int pos, int limit, String encodeSet,
                             boolean alreadyEncoded, boolean query) {
    int codePoint;
    for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
      codePoint = input.codePointAt(i);
      if (codePoint < 0x20
              || codePoint >= 0x7f
              || encodeSet.indexOf(codePoint) != -1
              || (codePoint == '%' && !alreadyEncoded)
              || (query && codePoint == '+')) {
        // Slow path: the character at i requires encoding!
        Buffer out = new Buffer();
        out.writeUtf8(input, pos, i);
        canonicalize(out, input, i, limit, encodeSet, alreadyEncoded, query);
        return out.readUtf8();
      }
    }

    // Fast path: no characters in [pos..limit) required encoding.
    return input.substring(pos, limit);
  }

  static void canonicalize(Buffer out, String input, int pos, int limit,
                           String encodeSet, boolean alreadyEncoded, boolean query) {
    Buffer utf8Buffer = null; // Lazily allocated.
    int codePoint;
    for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
      codePoint = input.codePointAt(i);
      if (alreadyEncoded
              && (codePoint == '\t' || codePoint == '\n' || codePoint == '\f' || codePoint == '\r')) {
        // Skip this character.
      } else if (query && codePoint == '+') {
        // HTML permits space to be encoded as '+'. We use '%20' to avoid special cases.
        out.writeUtf8(alreadyEncoded ? "%20" : "%2B");
      } else if (codePoint < 0x20
              || codePoint >= 0x7f
              || encodeSet.indexOf(codePoint) != -1
              || (codePoint == '%' && !alreadyEncoded)) {
        // Percent encode this character.
        if (utf8Buffer == null) {
          utf8Buffer = new Buffer();
        }
        utf8Buffer.writeUtf8CodePoint(codePoint);
        while (!utf8Buffer.exhausted()) {
          int b = utf8Buffer.readByte() & 0xff;
          out.writeByte('%');
          out.writeByte(HEX_DIGITS[(b >> 4) & 0xf]);
          out.writeByte(HEX_DIGITS[b & 0xf]);
        }
      } else {
        // This character doesn't need encoding. Just copy it over.
        out.writeUtf8CodePoint(codePoint);
      }
    }
  }

  static String canonicalize(
          String input, String encodeSet, boolean alreadyEncoded, boolean query) {
    return canonicalize(input, 0, input.length(), encodeSet, alreadyEncoded, query);
  }
}
