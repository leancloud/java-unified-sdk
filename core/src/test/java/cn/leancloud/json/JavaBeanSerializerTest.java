package cn.leancloud.json;

import cn.leancloud.Configure;
import junit.framework.TestCase;

import java.util.HashMap;

public class JavaBeanSerializerTest extends TestCase {
  private static final String KEY_READ_PERMISSION = "read";
  private static final String KEY_WRITE_PERMISSION = "write";

  public static class Permissions extends HashMap<String, Boolean> {
    public Permissions() {
      super();
    }

    public Permissions(boolean read, boolean write) {
      super();
      if (read) {
        put(KEY_READ_PERMISSION, read);
      }
      if (write) {
        put(KEY_WRITE_PERMISSION, write);
      }
    }

    public Permissions(HashMap<String, Object> map) {
      super();
      if (null == map) {
        return;
      }
      Object readValue = map.get(KEY_READ_PERMISSION);
      Object writeValue = map.get(KEY_WRITE_PERMISSION);
      if (null == readValue || !(readValue instanceof Boolean)) {
        put(KEY_READ_PERMISSION, false);
      } else {
        put(KEY_READ_PERMISSION, (Boolean)readValue);
      }
      if (null == writeValue || !(writeValue instanceof Boolean)) {
        put(KEY_WRITE_PERMISSION, false);
      } else {
        put(KEY_WRITE_PERMISSION, (Boolean)writeValue);
      }
    }

    public Permissions(Permissions permissions) {
      super();
      if (null == permissions) {
        return;
      }
      if (permissions.getRead()) {
        put(KEY_READ_PERMISSION, true);
      }
      if (permissions.getWrite()) {
        put(KEY_WRITE_PERMISSION, true);
      }
    }

    public void setRead(boolean v) {
      ;
    }
    public void setWrite(boolean v) {
      ;
    }
    public boolean getRead() {
      if (containsKey(KEY_READ_PERMISSION)) {
        return get(KEY_READ_PERMISSION);
      }
      return false;
    }

    public boolean getWrite() {
      if (containsKey(KEY_WRITE_PERMISSION)) {
        return get(KEY_WRITE_PERMISSION);
      }
      return false;
    }
  }

  public JavaBeanSerializerTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public void testPermissions() {
    Permissions perm = new Permissions(true, false);
    String permString = JSON.toJSONString(perm);
    System.out.println("permString=" + permString);
    Permissions other = JSON.parseObject(permString, Permissions.class);
    assertEquals(other.getRead(), true);
    assertEquals(other.getWrite(), false);
  }
}
