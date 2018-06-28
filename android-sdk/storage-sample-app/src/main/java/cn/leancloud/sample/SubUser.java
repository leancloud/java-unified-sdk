package cn.leancloud.sample;

import cn.leancloud.AVObject;
import cn.leancloud.AVUser;

/**
 * Created by fengjunwen on 2018/5/10.
 */


public class SubUser extends AVUser {
  public AVObject getArmor() {
    return getAVObject("armor");
  }

  public void setArmor(AVObject armor) {
    this.put("armor", armor);
  }

  public void setNickName(String name) {
    this.put("nickName", name);
  }

  public String getNickName() {
    return this.getString("nickName");
  }
}
