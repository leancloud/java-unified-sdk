package cn.leancloud;

import cn.leancloud.annotation.LCClassName;

@LCClassName("_Role")
public class LCRole extends LCObject {
  public final static String CLASS_NAME = "_Role";
  private static final String ATTR_NAME = "name";
  private static final String RELATION_ROLE_NAME = "roles";
  private static final String RELATION_USER_NAME = "users";

  public LCRole() {
    super(CLASS_NAME);
  }
  public LCRole(String name) {
    super(CLASS_NAME);
    put(ATTR_NAME, name);
  }
  public LCRole(String name, LCACL acl) {
    this(name);
    this.acl = acl;
  }

  public void setName(String name) {
    super.put(ATTR_NAME, name);
  }

  public String getName() {
    return this.getString(ATTR_NAME);
  }

  public static LCQuery<LCRole> getQuery() {
    LCQuery<LCRole> query = new LCQuery<LCRole>(CLASS_NAME);
    return query;
  }
  public LCRelation getRoles() {
    return super.getRelation(RELATION_ROLE_NAME);
  }

  public LCRelation getUsers() {
    return super.getRelation(RELATION_USER_NAME);
  }
}
