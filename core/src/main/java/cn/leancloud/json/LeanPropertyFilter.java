package cn.leancloud.json;

import cn.leancloud.AVACL;
import cn.leancloud.AVRole;
import cn.leancloud.types.AVDate;
import com.alibaba.fastjson.serializer.PropertyFilter;

public class LeanPropertyFilter implements PropertyFilter {
  protected static LeanPropertyFilter instance = new LeanPropertyFilter();

  public boolean apply(Object object, String name, Object value) {
    System.out.println("LeanPropertyFilter#appley, object=" + object.getClass().getSimpleName() + ", field=" + name);
    if (object instanceof AVRole) {
      if ("name".equals(name) || "query".equals(name) || "roles".equals(name)) {
        return false;
      }
      return true;
    } else if (object instanceof AVACL) {
      if ("publicReadAccess".equals(name) || "publicWriteAccess".equals(name)) {
        return false;
      }
      return true;
    } else if (object instanceof AVDate) {
      if ("date".equals(name)) {
        return false;
      }
      return true;
    }
    return true;
  }
}
