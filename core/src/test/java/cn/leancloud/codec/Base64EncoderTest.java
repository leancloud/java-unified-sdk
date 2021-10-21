package cn.leancloud.codec;

import junit.framework.TestCase;

public class Base64EncoderTest extends TestCase {
    public Base64EncoderTest(String name) {
        super(name);
    }

    public void testCommonFunc() throws Exception {
        String input = "searchQuery.orderByAscending";
        String encodeString = Base64Encoder.encode(input);
        assertTrue(input.equals(Base64Decoder.decode(encodeString)));
    }
}
