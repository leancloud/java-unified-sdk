package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AVRoleTest extends TestCase {
  public AVRoleTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AVRoleTest.class);
  }

  public void testCreateRole() {
    AVACL  acl = new AVACL();
    acl.setPublicReadAccess(true);
    acl.setPublicWriteAccess(false);
    AVRole role = new AVRole("Admin", acl);
    role.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println(avObject);
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }
}
