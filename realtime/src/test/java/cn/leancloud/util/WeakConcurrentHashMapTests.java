package cn.leancloud.util;

import junit.framework.TestCase;

public class WeakConcurrentHashMapTests extends TestCase {
  public WeakConcurrentHashMapTests(String name) {
    super(name);
  }

  public void testExpiredElement() throws Exception {
    WeakConcurrentHashMap<String, String> hashMap = new WeakConcurrentHashMap<>(3000, new WeakConcurrentHashMapListener<String, String>() {
      @Override
      public void notifyOnAdd(String key, String value) {
        System.out.println("add element: key=" + key + ", value=" + value);
      }

      @Override
      public void notifyOnRemoval(String key, String value) {
        System.out.println("remove element: key=" + key + ", value=" + value);
      }
    });
    String key1 = "key1";
    String key2 = "key2";
    String value = "test something";
    hashMap.addElement(key1, value);
    hashMap.addElement(key1, value);
    hashMap.addElement(key1, value);
    hashMap.addElement(key2, value);
    assertEquals(2, hashMap.size());

    Thread.sleep(5000);
    assertEquals(0, hashMap.size());
  }
}
