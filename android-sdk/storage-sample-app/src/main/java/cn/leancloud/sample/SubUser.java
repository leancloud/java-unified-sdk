package cn.leancloud.sample;

import cn.leancloud.LCObject;
import cn.leancloud.LCUser;

/**
 * Created by fengjunwen on 2018/5/10.
 */


public class SubUser extends LCUser {
  public LCObject getArmor() {
    return getLCObject("armor");
  }

  public void setArmor(LCObject armor) {
    this.put("armor", armor);
  }

  public void setNickName(String name) {
    this.put("nickName", name);
  }

  public String getNickName() {
    return this.getString("nickName");
  }
}
