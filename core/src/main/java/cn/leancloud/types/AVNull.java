package cn.leancloud.types;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType
public final class AVNull {
  private static final AVNull INSTANCE = new AVNull();

  public static AVNull getINSTANCE() {
    return INSTANCE;
  }
}
