package cn.leancloud.codec;

import junit.framework.TestCase;

public class SHA1Test extends TestCase {
    public SHA1Test(String name) {
        super(name);
    }

    public void testCommons() throws Exception {
        String input = "searchQuery.orderByAscending";
        String encodedString = SHA1.compute(input.getBytes());
        System.out.println(encodedString);
        assertTrue(encodedString.equals("2256f4f30dfe6198e1d6a2b94fe2c69bc5ed559e"));
    }
}
