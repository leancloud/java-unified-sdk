package cn.leancloud.callback;

import cn.leancloud.LCException;
import cn.leancloud.types.LCNull;

public abstract class MobilePhoneVerifyCallback extends LCCallback<LCNull> {

  /**
   * <p>
   * MobilePhoneVerifyCallback 用来验证用户的手机号码
   * </p>
   * @param t null object.
   * @param LCException  exception.
   * <p>
   * 　调用的范例如下
   * </p>
   *
   * <pre>
   * LCUser.verifyMobilePhoneInBackgroud(&quot;123456&quot;,
   *     new MobilePhoneVerifyCallback() {
   *       public void done(LCException e) {
   *         if (e == null) {
   *           requestedSuccessfully();
   *         } else {
   *           requestDidNotSucceed();
   *         }
   *       }
   *     });
   * </pre>
   */
  @Override
  protected final void internalDone0(LCNull t, LCException LCException) {
    this.done(LCException);
  }

  public abstract void done(LCException e);
}