package cn.leancloud.sample;

import cn.leancloud.LCObject;
import cn.leancloud.annotation.LCClassName;

/**
 * Created by fengjunwen on 2018/5/10.
 */

@LCClassName("Armor")
public class Armor extends LCObject {
  public String getDisplayName() {
    return getString("displayName");
  }

  public void setDisplayName(String value) {
    put("displayName", value);
  }

  public int getDurability() {
    return getInt("durability");
  }

  public void setBroken(boolean broken) {
    this.put("broken", broken);
  }

  public void takeDamage(int amount) {
    // Decrease the armor's durability and determine whether it has broken
    increment("durability", -amount);
    if (getDurability() < 0) {
      setBroken(true);
    }
  }
}