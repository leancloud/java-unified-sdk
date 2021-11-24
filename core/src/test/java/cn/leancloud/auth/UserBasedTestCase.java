package cn.leancloud.auth;

import cn.leancloud.Configure;
import cn.leancloud.LCUser;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;


import java.util.concurrent.CountDownLatch;

public class UserBasedTestCase extends TestCase {
  protected String username = null;
  protected String passwd = null;
  protected Exception runningException = null;

  public UserBasedTestCase(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public UserBasedTestCase(String name, String appId, String appKey, String masterKey, String serverURL) {
    super(name);
    if (StringUtil.isEmpty(masterKey)) {
      Configure.initializeWithApp(appId, appKey, serverURL);
    } else {
      Configure.initializeWithMasterKey(appId, masterKey, serverURL);
    }
  }

  protected void setAuthUser(String username, String passwd) {
    this.username = username;
    this.passwd = passwd;
  }

  protected void clearCurrentAuthenticatedUser() {
    LCUser.changeCurrentUser(null, true);
  }

  @Override
  protected void setUp() throws Exception {
    runningException = null;
    if (StringUtil.isEmpty(this.username) || StringUtil.isEmpty(this.passwd)) {
      return;
    }
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logIn(this.username, this.passwd).subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCUser lcUser) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        runningException = new Exception(throwable);
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    if (null != this.runningException) {
      throw this.runningException;
    }
  }

  @Override
  protected void tearDown() throws Exception {
    clearCurrentAuthenticatedUser();
  }

  public void testDummy() throws Exception {
    ;
  }
}
