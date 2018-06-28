package cn.leancloud.sample.testcase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;

import cn.leancloud.AVException;
import cn.leancloud.AVUser;
import cn.leancloud.sample.DemoBaseActivity;
import cn.leancloud.sample.R;
import cn.leancloud.callback.LogInCallback;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.callback.SignUpCallback;
import cn.leancloud.convertor.ObserverBuilder;

/**
 * Created by fengjunwen on 2018/5/10.
 */

public class UserAuthDataDemoActivity extends DemoBaseActivity {
  public interface InputDialogListener {
    void onAction(final String username, final String password);
  }
  protected void showInputDialog(final String title, final InputDialogListener listener) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(demoRunActivity);
        LayoutInflater inflater = LayoutInflater.from(demoRunActivity);
        final LinearLayout layout = (LinearLayout) inflater.inflate(cn.leancloud.sample.R.layout.login_dialog, null);

        final EditText userNameET = (EditText) layout.findViewById(R.id.usernameInput);
        final EditText passwordET = (EditText) layout.findViewById(R.id.passwordInput);
        builder.setView(layout);
        builder.setTitle(title).setPositiveButton(title, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String username = userNameET.getText().toString();
            String password = passwordET.getText().toString();
            if (listener != null && username.length() > 0 && password.length() > 0) {
              listener.onAction(username, password);
            }
          }
        });
        AlertDialog ad = builder.create();
        ad.show();
      }
    });
  }
  public void testLoginWithAuthData() {
    Map<String, Object> authData = new HashMap<String, Object>();
    authData.put("expires_at", "2019-01-07T02:41:13.580Z");
    authData.put("openid", "6A83158");
    authData.put("access_token", "DCIF");
    authData.put("platform", "weixin");
    AVUser.loginWithAuthData(authData, "weixin_darenbangbang").subscribe(ObserverBuilder.buildSingleObserver(new LogInCallback() {
      @Override
      public void done(AVUser avUser, AVException e) {
        if (null != e) {
          log("尝试使用第三方账号登录，发生错误。cause：" + e.getMessage());
        } else {
          log("成功登录，当前用户：" + avUser);
        }
      }
    }));
  }
  public void testLoginWithAuthDataEx() {
    final Map<String, Object> authData = new HashMap<String, Object>();
    authData.put("expires_at", "2019-01-07T02:41:13.580Z");
    authData.put("openid", "6A83158");
    authData.put("access_token", "DCIF");
    authData.put("platform", "weixin");
    AVUser.loginWithAuthData(authData, "weixin_darenbangbang")
        .subscribe(ObserverBuilder.buildSingleObserver(new LogInCallback() {
      @Override
      public void done(final AVUser avUser, AVException e) {
        if (null != e) {
          log("尝试使用第三方账号登录，发生错误。cause：" + e.getMessage());
        } else {
          log("第一次成功登录，当前用户：" + avUser.getObjectId());
          Map<String, Object> authData2 = new HashMap<String, Object>();
          authData2.put("expires_at", "2019-11-07T02:41:13.580Z");
          authData2.put("openid", "6A8315fwirw328");
          authData2.put("access_token", "Dfaef21CIF");
          authData2.put("platform", "weixin");
          AVUser.loginWithAuthData(authData2, "weixin_darenxiu")
              .subscribe(ObserverBuilder.buildSingleObserver(new LogInCallback() {
                @Override
                public void done(AVUser avUser2, AVException ex) {
                  if (null != ex) {
                    log("尝试使用第三方账号登录，发生错误。cause：" + ex.getMessage());
                  } else {
                    log("第二次成功登录，当前用户：" + avUser2.getObjectId());
                    AVUser.loginWithAuthData(authData, "weixin_darenbangbang", "ThisisaunionId", "weixin", true)
                        .subscribe(ObserverBuilder.buildSingleObserver(new LogInCallback() {
                          @Override
                          public void done(AVUser au, AVException e2) {
                            if (null != e2) {
                              log("尝试使用第三方账号登录，发生错误。cause：" + e2.getMessage());
                            } else {
                              log("第三次成功登录，当前用户：" + au.getObjectId());
                              if (au.getObjectId().equals(avUser.getObjectId())) {
                                log("expected: bind to correct user with unionId");
                              } else {
                                log("not expected: cannot bind to correct user with unionId");
                              }
                            }
                          }
                        }));
                  }
                }
              }));
        }
      }
    }));
  }
  public void testAssociateWithAuthData() {
    showInputDialog("Sign Up", new InputDialogListener(){
      public void onAction(final String username, final String password) {
        final AVUser user = new AVUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SignUpCallback() {
          @Override
          public void done(AVException e) {
            if (null != e) {
              log("用户注册失败。 cause：" + e.getMessage());
            } else {
              log("注册成功 uesr: " + user.getObjectId());
              final Map<String, Object> authData = new HashMap<String, Object>();
              authData.put("expires_at", "2019-01-07T02:41:13.580Z");
              authData.put("openid", "6A83faefewfew158");
              authData.put("access_token", "DCfafewerEWDWIF");
              authData.put("platform", "weixin");
              user.associateWithAuthData(authData, "weixin_darenbangbang")
                  .subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
                    @Override
                    public void done(AVException ex) {
                      if (null != ex) {
                        log("第三方信息关联失败。 cause：" + ex.getMessage());
                      } else {
                        log("第三方信息关联成功");
                      }
                    }
                  }));
            }
          }
        }));
      }
    });
  }
  public void testAssociateWithAuthDataEx() {
    showInputDialog("Sign Up", new InputDialogListener(){
      public void onAction(final String username, final String password) {
        final AVUser user = new AVUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SignUpCallback() {
          @Override
          public void done(AVException e) {
            if (null != e) {
              log("用户注册失败。 cause：" + e.getMessage());
            } else {
              log("注册成功 uesr: " + user.getObjectId());
              final Map<String, Object> authData = new HashMap<String, Object>();
              authData.put("expires_at", "2019-01-07T02:41:13.580Z");
              authData.put("openid", "6A83faefewfew158");
              authData.put("access_token", "DCfafewerEWDWIF");
              authData.put("platform", "weixin");
              user.associateWithAuthData(authData, "weixin_darenbangbang",
                  "ThisisAUnionIDXXX", "weixin", false)
                  .subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
                    @Override
                    public void done(AVException ex) {
                      if (null != ex) {
                        log("第三方信息关联失败。 cause：" + ex.getMessage());
                      } else {
                        log("第三方信息关联成功");
                      }
                    }
                  }));
            }
          }
        }));
        ;
      }
    });
  }
}
