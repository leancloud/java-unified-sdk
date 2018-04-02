package cn.leancloud.query;

import okio.Buffer;

import java.util.HashMap;
import java.util.Map;

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

//  private static class ParameterValuePair {
//    String encodedParam;
//    String param;
//
//    public String getEncodedParam() {
//      return encodedParam;
//    }
//
//    public String getParam() {
//      return param;
//    }
//
//    public static ParameterValuePair getParameterValuePair(String param, String encodedParam) {
//      ParameterValuePair pair = new ParameterValuePair();
//      pair.encodedParam = encodedParam;
//      pair.param = param;
//      return pair;
//    }
//
//    public static ParameterValuePair getParameterValuePair(String param) {
//      ParameterValuePair pair = new ParameterValuePair();
//      pair.encodedParam = canonicalize(param, QUERY_COMPONENT_ENCODE_SET, false, true);
//      pair.param = param;
//      return pair;
//    }
//  }
//
//  HashMap<String, ParameterValuePair> params;
//  public AVRequestParams() {
//    params = new HashMap<String, ParameterValuePair>();
//  }
//
//  public AVRequestParams(Map<String, String> params) {
//    this();
//    if (params != null) {
//      for (Map.Entry<String, String> param : params.entrySet()) {
//        this.put(param.getKey(), param.getValue());
//      }
//    }
//  }
//
//  public void put(String name, Object value) {
//    params.put(canonicalize(name, QUERY_COMPONENT_ENCODE_SET, false, true),
//            ParameterValuePair.getParameterValuePair(value.toString()));
//  }
//  public String getQueryString() {
//    if (params.isEmpty()) {
//      return "";
//    } else {
//      StringBuilder sb = new StringBuilder();
//      int index = 0;
//      for (Map.Entry<String, ParameterValuePair> entry : params.entrySet()) {
//        if (index > 0) {
//          sb.append('&');
//        }
//        sb.append(entry.getKey());
//        sb.append('=');
//        sb.append(entry.getValue().encodedParam);
//        index++;
//      }
//      return sb.toString();
//    }
//  }
//
//  public String getWholeUrl(String url) {
//    if (params.isEmpty()) {
//      return url;
//    } else {
//      StringBuilder sb = new StringBuilder(url);
//      sb.append('?');
//      int index = 0;
//      for (Map.Entry<String, ParameterValuePair> entry : params.entrySet()) {
//        if (index > 0) {
//          sb.append('&');
//        }
//        sb.append(entry.getKey());
//        sb.append('=');
//        sb.append(entry.getValue().encodedParam);
//        index++;
//      }
//      return sb.toString();
//    }
//  }
//
//  public boolean isEmpty() {
//    return params == null || params.isEmpty();
//  }

}
