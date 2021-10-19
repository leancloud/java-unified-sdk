package cn.leancloud;

import cn.leancloud.core.LeanCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.util.concurrent.CountDownLatch;

public class LCObjectHookTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;
  private static final String HOOK_OBJ_CLASS = "HookObject";

  public LCObjectHookTest(String testName) {
    super(testName);
    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    Configure.initializeRuntime();
    LeanCloud.setHookKey("{your hook key}");
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testEnableHook() throws Exception {
    LCObject object = new LCObject(HOOK_OBJ_CLASS);
    object.put("occ", System.currentTimeMillis());
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject object) {
        System.out.println("occ: " + object.get("occ"));
        object.put("modify", System.currentTimeMillis());
        object.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object) {
            object.delete();
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to update object. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to create object. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  @Ignore
  public void tsetDisableBeforeSaveHook() throws Exception {
    LCObject object = new LCObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.ignoreHook(LCObject.Hook.beforeSave);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") != now) {
          System.out.println("occ attr is wrong. expected:" + now + ", actual:" + object.getLong("occ"));
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());
        object.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object) {
            object.delete();
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to update object. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to create object. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testDisableBeforeUpdateHook() throws Exception {
    LCObject object = new LCObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());
        object.ignoreHook(LCObject.Hook.beforeUpdate);
        object.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object) {
            object.delete();
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to update object. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to create object. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
  public void testDisableBeforeDeleteHook() throws Exception {
    LCObject object = new LCObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());

        object.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object) {
            object.ignoreHook(LCObject.Hook.beforeDelete);
            object.delete();
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to update object. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to create object. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testDisableAfterSaveHook() throws Exception {
    LCObject object = new LCObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.ignoreHook(LCObject.Hook.afterSave);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());
        object.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object) {
            object.delete();
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to update object. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to create object. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
  public void testDisableAfterUpdateHook() throws Exception {
    LCObject object = new LCObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());
        object.ignoreHook(LCObject.Hook.afterUpdate);
        object.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object) {
            object.delete();
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to update object. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to create object. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
  public void testDisableAfterDeleteHook() throws Exception {
    LCObject object = new LCObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());

        object.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object) {
            object.ignoreHook(LCObject.Hook.afterDelete);
            object.delete();
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to update object. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to create object. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  @Ignore
  public void tsetDisableBeforeHook() throws Exception {
    LCObject object = new LCObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.disableBeforeHook();
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") != now) {
          System.out.println("occ attr is wrong. expected:" + now + ", actual:" + object.getLong("occ"));
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());

        object.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object) {
            object.delete();
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to update object. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to create object. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testDisableAfterHook() throws Exception {
    LCObject object = new LCObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.disableAfterHook();
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());

        object.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object) {
            object.delete();
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to update object. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to create object. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  @Ignore
  public void tsetDisableAllHook() throws Exception {
    LCObject object = new LCObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.disableAfterHook();
    object.disableBeforeHook();
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject object) {
        System.out.println("step 1: succeed to save object. occ: " + object.get("occ"));
        if (object.getLong("occ") != now) {
          System.out.println("error: occ is wrong. expected:" + now + ", actual:" + object.getLong("occ"));
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());

        object.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object) {
            System.out.println("step 2: succeed to update object with modify attr.");
            object.delete();
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("step 2: failed to update object. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("step 1: failed to save object. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
}
