package cn.leancloud.sample.testcase;

import cn.leancloud.LCException;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import cn.leancloud.sample.DemoBaseActivity;
import cn.leancloud.sample.DemoUtils;
import cn.leancloud.callback.LogInCallback;
import cn.leancloud.callback.MobilePhoneVerifyCallback;
import cn.leancloud.callback.RequestMobileCodeCallback;
import cn.leancloud.callback.RequestPasswordResetCallback;
import cn.leancloud.callback.SignUpCallback;
import cn.leancloud.callback.UpdatePasswordCallback;
import cn.leancloud.convertor.ObserverBuilder;

/**
 * Created by fengjunwen on 2018/5/15.
 */

public class UserDemoActivity extends DemoBaseActivity {
  private String demoPassword = "123456";

  public void testCurrentUser() {
    LCUser user = LCUser.getCurrentUser();
    if (user != null) {
      log("当前已登录的用户为：" + user);
    } else {
      log("当前未有用户登录");
    }
  }

  public void testLogOut() {
    LCUser user = LCUser.getCurrentUser();
    if (user != null) {
      log("当前用户为 " + user);
      LCUser.logOut();
      log("已注销当前用户");
    } else {
      promptLogin();
    }
  }

  void promptLogin() {
    log("当前用户为空，请运行登录的例子，登录一个用户");
  }

  public void testDeleteCurrentUser() throws LCException {
    LCUser user = LCUser.getCurrentUser();
    if (user != null) {
      user.delete();
      log("已删除当前用户");
    } else {
      promptLogin();
    }
  }

  public void testWriteOtherUserData() throws LCException {
    LCQuery<LCUser> q = LCUser.getQuery();
    LCUser first = q.getFirst();
    log("获取了一个用户，但未登录该用户");
    first.put("city", "ShangHai");
    first.save();

    log("结论：不能修改未登录用户的数据");
  }

  public void testUserSignUp() throws Exception {
    showInputDialog("Sign Up", new InputDialogListener() {
      @Override
      public void onAction(String username, String password) {
        LCUser.logOut();
        final LCUser user = new LCUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SignUpCallback() {
          @Override
          public void done(LCException e) {
            log("注册成功 uesr:" + user);
          }
        }));
      }
    });
  }


  public void testLogin() {
    showInputDialog("Login", new InputDialogListener() {
      @Override
      public void onAction(String username, String password) {
        LCUser.logOut();
        LCUser.logIn(username, password)
            .subscribe(ObserverBuilder.buildSingleObserver(new LogInCallback<LCUser>() {
          @Override
          public void done(LCUser LCUser, LCException e) {
            if (e != null) {
              log(e.getMessage());
            } else {
              log("登录成功 user：" + LCUser.toString());
            }
          }
        }));
      }
    });
  }

  public void testOldPasswordUpdatePassword() throws LCException {
    LCUser user = LCUser.getCurrentUser();
    if (user == null) {
      promptLogin();
      return;
    }
    String newPassword = "1111111";
    user.updatePasswordInBackground(demoPassword, newPassword).blockingSubscribe();
    log("重置密码成功，新密码为 " + newPassword);
    logThreadTips();
  }

  public void testPhoneNumberRegister() {
    // 请在网站勾选 "验证注册用户手机号码" 选项，否则不会发送验证短信
    showSimpleInputDialog("请输入手机号码来注册", new SimpleInputDialogListner() {
      @Override
      public void onConfirm(String text) {
        final LCUser user = new LCUser();
        user.setUsername(DemoUtils.getRandomString(6));
        user.setPassword(demoPassword);
        user.setMobilePhoneNumber(text);
        user.signUpInBackground()
            .subscribe(ObserverBuilder.buildSingleObserver(new SignUpCallback() {
          @Override
          public void done(LCException e) {
            if (filterException(e)) {
              showSimpleInputDialog("验证短信已发送，请输入验证码", new SimpleInputDialogListner() {
                @Override
                public void onConfirm(String code) {
                  LCUser.verifyMobilePhoneInBackground(code, text)
                      .subscribe(ObserverBuilder.buildSingleObserver(new MobilePhoneVerifyCallback() {
                        @Override
                        public void done(LCException e) {
                          if (filterException(e)) {
                            log("注册成功, user:" + user);
                          }
                        }
                      }));
                }
              });
            }
          }
        }));
      }
    });
  }

  public void testPhoneNumberAndPasswordLogin() {
    showSimpleInputDialog("请输入手机号码来登录", new SimpleInputDialogListner() {
      @Override
      public void onConfirm(String text) {
        LCUser.loginByMobilePhoneNumber(text, demoPassword)
            .subscribe(ObserverBuilder.buildSingleObserver( new LogInCallback<LCUser>() {
          @Override
          public void done(LCUser LCUser, LCException e) {
            if (filterException(e)) {
              log("登录成功, user:" + LCUser);
            }
          }
        }));
      }
    });
  }

  public void testPhoneNumberAndCodeLogin() {
    showSimpleInputDialog("请输入手机号码来登录", new SimpleInputDialogListner() {
      @Override
      public void onConfirm(final String phone) {
        LCUser.requestLoginSmsCodeInBackground(phone)
            .subscribe(ObserverBuilder.buildSingleObserver(new RequestMobileCodeCallback() {
          @Override
          public void done(LCException e) {
            if (filterException(e)) {
              showSimpleInputDialog("验证码已发送，请输入验证码", new SimpleInputDialogListner() {
                @Override
                public void onConfirm(String smsCode) {
                  LCUser.loginByMobilePhoneNumber(phone, smsCode)
                      .subscribe(ObserverBuilder.buildSingleObserver(new LogInCallback<LCUser>() {
                        @Override
                        public void done(LCUser LCUser, LCException e) {
                          if (filterException(e)) {
                            log("登录成功, user: " + LCUser);
                          }
                        }
                      }));
                }
              });
            }
          }
        }));
      }
    });
  }

  public void testPhoneNumberResetPassword() {
    showSimpleInputDialog("请输入需要重置密码的手机号", new SimpleInputDialogListner() {
      @Override
      public void onConfirm(final String phone) {
        LCUser.requestPasswordResetBySmsCodeInBackground(phone)
            .subscribe(ObserverBuilder.buildSingleObserver(new RequestMobileCodeCallback() {
          @Override
          public void done(LCException e) {
            if (filterException(e)) {
              showSimpleInputDialog("短信已发送，请输入验证码来重置密码", new SimpleInputDialogListner() {
                @Override
                public void onConfirm(String smsCode) {
                  final String newPassword = "abcdefg";
                  LCUser.resetPasswordBySmsCodeInBackground(smsCode, newPassword, phone)
                      .subscribe(ObserverBuilder.buildSingleObserver(new UpdatePasswordCallback() {
                        @Override
                        public void done(LCException e) {
                          if (filterException(e)) {
                            log("密码更改成功，新密码 " + newPassword);
                            log("试着用手机号和新密码登录吧");
                          }
                        }
                      }));

                }
              });
            }
          }
        }));

      }
    });
  }

  public void testEmailRegister() {
    log("请确认控制台已开启注册时开启邮箱验证，这样才能收到验证邮件");
    showSimpleInputDialog("请输入您的邮箱来注册", new SimpleInputDialogListner() {
      @Override
      public void onConfirm(String text) {
        final LCUser user = new LCUser();
        user.setUsername(text);
        user.setPassword(demoPassword);
        user.setEmail(text);
        user.signUpInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SignUpCallback() {
          @Override
          public void done(LCException e) {
            if (filterException(e)) {
              log("注册成功，user: " + user);
            }
          }
        }));
      }
    });
  }

  public void testEmailLogin() {
    showSimpleInputDialog("请输入邮箱来登录", new SimpleInputDialogListner() {
      @Override
      public void onConfirm(String text) {
        LCUser.logIn(text, demoPassword)
            .subscribe(ObserverBuilder.buildSingleObserver(new LogInCallback<LCUser>() {
          @Override
          public void done(LCUser LCUser, LCException e) {
            if (filterException(e)) {
              log("登录成功 user:" + LCUser);
            }
          }
        }));
      }
    });
  }

  public void testEmailResetPassword() {
    showSimpleInputDialog("请输入邮箱进行密码重置", new SimpleInputDialogListner() {
      @Override
      public void onConfirm(final String text) {
        LCUser.requestPasswordResetInBackground(text)
            .subscribe(ObserverBuilder.buildSingleObserver(new RequestPasswordResetCallback() {
          @Override
          public void done(LCException e) {
            if (filterException(e)) {
              log("重置密码的邮件已发送到邮箱 " + text);
            }
          }
        }));
      }
    });
  }

  public void testBecomeWithSessionToken() {
    LCUser user = LCUser.becomeWithSessionToken("thisisa fake session token");
    log("succeed to initiate user with a fake session token: " + user);
  }

  public void testAnonymousUserLogin() {
    LCUser.logInAnonymously()
        .subscribe(ObserverBuilder.buildSingleObserver(new LogInCallback<LCUser>() {
      @Override
      public void done(LCUser LCUser, LCException e) {
        if (filterException(e)) {
          log("创建了一个匿名用户并登录，user:" + LCUser);
        }
      }
    }));
  }
}
