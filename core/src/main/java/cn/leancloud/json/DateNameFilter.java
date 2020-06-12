package cn.leancloud.json;

import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.serializer.NameFilter;

public class DateNameFilter implements NameFilter {
  static DateNameFilter instance = new DateNameFilter();

  public String process(Object object, String propertyName, Object propertyValue) {
    if (StringUtil.isEmpty(propertyName)) {
      return propertyName;
    }
    if (propertyName.equals("type")) {
      return "__type";
    }
    return propertyName;
  }
}
