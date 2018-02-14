package cn.leancloud.core;

import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;
import java.util.Map;

public class AVACL {
  private static final String PUBLIC_KEY = "*";
  private static final String ROLE_PREFIX = "role:";

  private static class Permissions {
    @JSONField(name = "read")
    private final boolean readPermission;
    @JSONField(name = "write")
    private final boolean writePermission;
    Permissions(boolean read, boolean write) {
      readPermission = read;
      writePermission = write;
    }
    Permissions(Permissions permissions) {
      this.readPermission = permissions.readPermission;
      this.writePermission = permissions.writePermission;
    }
    boolean getReadPermission() {
      return readPermission;
    }

    boolean getWritePermission() {
      return writePermission;
    }
  }

  private final Map<String, Permissions> permissionsById = new HashMap<String, Permissions>();

  public AVACL() {
  }
  public AVACL(AVACL other) {
    permissionsById.putAll(other.permissionsById);
  }
  public AVACL(AVUser owner) {
    setReadAccess(owner, true);
    setWriteAccess(owner, true);
  }

  private void setPermissionsIfNonEmpty(String userId, boolean readPermission, boolean writePermission) {
    if (!(readPermission || writePermission)) {
      permissionsById.remove(userId);
    }
    else {
      permissionsById.put(userId, new Permissions(readPermission, writePermission));
    }
  }
  /**
   * Set whether the public is allowed to read this object.
   */
  public void setPublicReadAccess(boolean allowed) {
    setReadAccess(PUBLIC_KEY, allowed);
  }

  /**
   * Get whether the public is allowed to read this object.
   */
  public boolean getPublicReadAccess() {
    return getReadAccess(PUBLIC_KEY);
  }

  /**
   * Set whether the public is allowed to write this object.
   */
  public void setPublicWriteAccess(boolean allowed) {
    setWriteAccess(PUBLIC_KEY, allowed);
  }

  /**
   * Set whether the public is allowed to write this object.
   */
  public boolean getPublicWriteAccess() {
    return getWriteAccess(PUBLIC_KEY);
  }

  /**
   * Set whether the given user id is allowed to read this object.
   */
  public void setReadAccess(String userId, boolean allowed) {
    if (StringUtil.isEmpty(userId)) {
      throw new IllegalArgumentException("cannot setRead/WriteAccess for null userId");
    }
    boolean writePermission = getWriteAccess(userId);
    setPermissionsIfNonEmpty(userId, allowed, writePermission);
  }

  /**
   * Get whether the given user id is *explicitly* allowed to read this object. Even if this returns
   * {@code false}, the user may still be able to access it if getPublicReadAccess returns
   * {@code true} or a role  that the user belongs to has read access.
   */
  public boolean getReadAccess(String userId) {
    if (StringUtil.isEmpty(userId)) {
      return false;
    }
    Permissions permissions = permissionsById.get(userId);
    return permissions != null && permissions.getReadPermission();
  }

  /**
   * Set whether the given user id is allowed to write this object.
   */
  public void setWriteAccess(String userId, boolean allowed) {
    if (StringUtil.isEmpty(userId)) {
      throw new IllegalArgumentException("cannot setRead/WriteAccess for null userId");
    }
    boolean readPermission = getReadAccess(userId);
    setPermissionsIfNonEmpty(userId, readPermission, allowed);
  }

  /**
   * Get whether the given user id is *explicitly* allowed to write this object. Even if this
   * returns {@code false}, the user may still be able to write it if getPublicWriteAccess returns
   * {@code true} or a role that the user belongs to has write access.
   */
  public boolean getWriteAccess(String userId) {
    if (StringUtil.isEmpty(userId)) {
      return false;
    }
    Permissions permissions = permissionsById.get(userId);
    return permissions != null && permissions.getWritePermission();
  }

  public void setReadAccess(AVUser user, boolean allowed) {
    if (null == user || StringUtil.isEmpty(user.getObjectId())) {
      throw new IllegalArgumentException("cannot setRead/WriteAccess for a user with null id");
    }
    setReadAccess(user.getObjectId(), allowed);
  }

  public boolean getReadAccess(AVUser user) {
    if (null == user || StringUtil.isEmpty(user.getObjectId())) {
      return false;
    }
    return this.getReadAccess(user.getObjectId());
  }

  public void setWriteAccess(AVUser user, boolean allowed) {
    if (null == user || StringUtil.isEmpty(user.getObjectId())) {
      throw new IllegalArgumentException("cannot setRead/WriteAccess for a user with null id");
    }
    setWriteAccess(user.getObjectId(), allowed);
  }

  public boolean getWriteAccess(AVUser user) {
    if (null == user || StringUtil.isEmpty(user.getObjectId())) {
      return false;
    }
    return this.getWriteAccess(user.getObjectId());
  }

  public void setRoleReadAccess(String role, boolean allowed) {
    if (StringUtil.isEmpty(role)) {
      throw new IllegalArgumentException("cannot setRead/WriteAccess to a empty role");
    }
    this.setReadAccess(ROLE_PREFIX + role, allowed);
  }

  public boolean getRoleReadAccess(String role) {
    if (StringUtil.isEmpty(role)) {
      return false;
    }
    return getReadAccess(ROLE_PREFIX + role);
  }

  public void setRoleWriteAccess(String role, boolean allowed) {
    if (StringUtil.isEmpty(role)) {
      throw new IllegalArgumentException("cannot setRead/WriteAccess to a empty role");
    }
    this.setWriteAccess(ROLE_PREFIX + role, allowed);
  }

  public boolean getRoleWriteAccess(String role) {
    if (StringUtil.isEmpty(role)) {
      return false;
    }
    return getWriteAccess(ROLE_PREFIX + role);
  }
}
