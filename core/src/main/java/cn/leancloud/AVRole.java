package cn.leancloud;

public class AVRole extends AVObject {
  public static String CLASS_NAME = "_Role";
  private static final String ATTR_NAME = "name";
  private static final String RELATION_ROLE_NAME = "roles";
  private static final String RELATION_USER_NAME = "users";

  private String name;

  public AVRole() {
    super(CLASS_NAME);
  }
  public AVRole(String name) {
    super(CLASS_NAME);
    put(ATTR_NAME, name);
  }
  public AVRole(String name, AVACL acl) {
    this(name);
    this.acl = acl;
  }

  public void setName(String name) {
    super.put(ATTR_NAME, name);
  }

  public String getName() {
    return this.getString(ATTR_NAME);
  }

  public static AVQuery<AVRole> getQuery() {
    AVQuery<AVRole> query = new AVQuery<AVRole>(CLASS_NAME);
    return query;
  }
  public AVRelation getRoles() {
    return super.getRelation(RELATION_ROLE_NAME);
  }

  public AVRelation getUsers() {
    return super.getRelation(RELATION_USER_NAME);
  }
}
