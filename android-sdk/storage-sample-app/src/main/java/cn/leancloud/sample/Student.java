package cn.leancloud.sample;

import java.util.List;

import cn.leancloud.AVFile;
import cn.leancloud.AVObject;
import cn.leancloud.annotation.AVClassName;

/**
 * Created by fengjunwen on 2018/5/10.
 */
@AVClassName("Student")
public class Student extends AVObject {

  public static final String NAME = "name";
  public static final String AGE = "age";
  public static final String AVATAR = "avatar";
  public static final String HOBBIES = "hobbies";
  public static final String ANY = "any";

  public Student() {

  }

  public String getName() {
    return getString(NAME);
  }

  public void setName(String name) {
    put(NAME, name);
  }

  public int getAge() {
    return getInt(AGE);
  }

  public void setAge(int age) {
    put(AGE, age);
  }

  public AVFile getAvatar() {
    return getAVFile(AVATAR);
  }

  public void setAvatar(AVFile avatar) {
    put(AVATAR, avatar);
  }

  public Object getAny() {
    return get(ANY);
  }

  public void setAny(Object any) {
    put(ANY, any);
  }

  public List<String> getHobbies() {
    return getList(HOBBIES);
  }

  public void setHobbies(List<String> hobbies) {
    put(HOBBIES, hobbies);
  }
}
