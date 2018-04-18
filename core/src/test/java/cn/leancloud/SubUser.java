package cn.leancloud;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType(ignores = {"blackListRelation"})
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

  public AVRelation<AVObject> getBlackListRelation() {
    return this.getRelation("blacklist");
  }

  // public void addBlackList(AVObject o) {
  // this.getBlackListRelation().add(o);
  // this.saveInBackground();
  // }
  //
  // public void removeBlackList(AVObject o) {
  // this.getBlackListRelation().remove(o);
  // this.saveInBackground();
  // }
}