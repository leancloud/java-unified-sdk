package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public class AVObjectHookTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;
  private static final String HOOK_OBJ_CLASS = "HookObject";

  public AVObjectHookTest(String testName) {
    super(testName);
    AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
    Configure.initializeRuntime();
    AVOSCloud.setHookKey("{your hook key}");
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testEnableHook() throws Exception {
    AVObject object = new AVObject(HOOK_OBJ_CLASS);
    object.put("occ", System.currentTimeMillis());
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject object) {
        System.out.println("occ: " + object.get("occ"));
        object.put("modify", System.currentTimeMillis());
        object.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object) {
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

  public void testDisableBeforeSaveHook() throws Exception {
    AVObject object = new AVObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.ignoreHook(AVObject.Hook.beforeSave);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") != now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());
        object.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object) {
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
    AVObject object = new AVObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());
        object.ignoreHook(AVObject.Hook.beforeUpdate);
        object.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object) {
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
    AVObject object = new AVObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());

        object.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object) {
            object.ignoreHook(AVObject.Hook.beforeDelete);
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
    AVObject object = new AVObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.ignoreHook(AVObject.Hook.afterSave);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());
        object.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object) {
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
    AVObject object = new AVObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());
        object.ignoreHook(AVObject.Hook.afterUpdate);
        object.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object) {
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
    AVObject object = new AVObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());

        object.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object) {
            object.ignoreHook(AVObject.Hook.afterDelete);
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

  public void testDisableBeforeHook() throws Exception {
    AVObject object = new AVObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.disableBeforeHook();
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") != now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());

        object.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object) {
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
    AVObject object = new AVObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.disableAfterHook();
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") == now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());

        object.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object) {
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

  public void testDisableAllHook() throws Exception {
    AVObject object = new AVObject(HOOK_OBJ_CLASS);
    final long now = System.currentTimeMillis();
    object.put("occ", now);
    object.setFetchWhenSave(true);
    object.disableAfterHook();
    object.disableBeforeHook();
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject object) {
        System.out.println("occ: " + object.get("occ"));
        if (object.getLong("occ") != now) {
          latch.countDown();
          return;
        }
        object.put("modify", System.currentTimeMillis());

        object.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object) {
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
}
