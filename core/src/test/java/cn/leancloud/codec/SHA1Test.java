package cn.leancloud.codec;

import junit.framework.TestCase;

import java.util.UUID;

public class SHA1Test extends TestCase {
    public SHA1Test(String name) {
        super(name);
    }

    public void testCommons() throws Exception {
        AES aesAlgo = new AES();
        String input = "searchQuery.orderByAscending";
        String encodedString = SHA1.compute(input.getBytes());
        String md5 = MDFive.computeMD5(input);
        String aes = aesAlgo.encrypt(input.getBytes());
        System.out.println(input + "-(MD5)->" + md5);
        System.out.println(input + "-(AES)->" + aes);
        System.out.println(input + "-(SHA1)->" + encodedString);
        assertTrue(encodedString.equals("2256f4f30dfe6198e1d6a2b94fe2c69bc5ed559e"));

        input = UUID.randomUUID().toString();
        encodedString = SHA1.compute(input.getBytes());
        aes = aesAlgo.encrypt(input.getBytes());
        md5 = MDFive.computeMD5(input);
        System.out.println(input + "-(MD5)->" + md5);
        System.out.println(input + "-(AES)->" + aes);
        System.out.println(input + "-(SHA1)->" + encodedString);
    }
}
