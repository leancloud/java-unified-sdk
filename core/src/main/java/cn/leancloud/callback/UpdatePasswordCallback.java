package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.types.LCNull;

public abstract class UpdatePasswordCallback extends LCCallback<LCNull> {
  /**
   * 请用您需要在修改密码完成以后的逻辑重载本方法
   *
   * @param e 修改密码请求可能产生的异常
   *
   */
  public abstract void done(LCException e);

  @Override
  protected final void internalDone0(LCNull t, LCException LCException) {
    this.done(LCException);
  }
}