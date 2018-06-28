package cn.leancloud;

public enum EngineHookType {
  /**
   * 在将对象保存到云端数据表之前，可以对数据做一些清理或验证
   */
  beforeSave("__before_save", true),
  /**
   * 在数据保存后触发指定操作
   */
  afterSave("__after_save", false),
  /**
   * 在更新对象之前做一些检查工作
   */
  beforeUpdate("__before_update", true),
  /**
   * 在更新对象后执行特定的动作
   */
  afterUpdate("__after_update", false),
  /**
   * 在删除一个对象之前做一些检查工作
   */
  beforeDelete("__before_delete", true),
  /**
   * 在被删一个对象后执行操作
   */
  afterDelete("__after_delete", false),
  /**
   * 当用户通过短信验证时，对该用户执行特定操作
   */
  onVerifiedSMS("__on_verified_sms", false),
  /**
   * 当用户通过邮箱验证时，对该用户执行特定操作
   * 
   */
  onVerifiedEmail("__on_verified_email", false),
  /**
   * 在用户登录之时执行指定操作
   */
  onLogin("__on_login__User", true);

  String endPoint;
  boolean isResponseNeed;

  EngineHookType(String endPointer, boolean response) {
    this.endPoint = endPointer;
    this.isResponseNeed = response;
  }

  @Override
  public String toString() {
    return this.endPoint;
  }

  public static EngineHookType parse(String string) {
    if ("beforeSave".equals(string)) {
      return beforeSave;
    } else if ("afterSave".equals(string)) {
      return afterSave;
    } else if ("beforeUpdate".equals(string)) {
      return beforeUpdate;
    } else if ("afterUpdate".equals(string)) {
      return afterUpdate;
    } else if ("beforeDelete".equals(string)) {
      return beforeDelete;
    } else if ("afterDelete".equals(string)) {
      return afterDelete;
    } else if ("onLogin".equals(string)) {
      return onLogin;
    } else if ("sms".equals(string)) {
      return onVerifiedSMS;
    } else if ("email".equals(string)) {
      return onVerifiedEmail;
    }
    return null;
  }
}
