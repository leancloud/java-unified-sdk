package cn.leancloud;

import cn.leancloud.types.LCGeoPoint;
import cn.leancloud.types.LCNull;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class LCObjectTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public LCObjectTest(String testName) {
    super(testName);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(LCObjectTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testDateAttribute() throws Exception {
    final Date now = new Date();
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", null);
    object.put("lastOcc", now);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println("saveObject field finished.");
        Date savedDate = LCObject.getDate("lastOcc");
        testSucceed = now.equals(savedDate);
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testFetchRemovedAttr() throws Exception {
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject lcObject) {
        System.out.println("try to remove grade field.");
        LCObject tmpObj = LCObject.createWithoutData("Student", object.getObjectId());
        tmpObj.remove("grade");
        tmpObj.saveInBackground().subscribe(new Observer<LCObject>() {
          public void onSubscribe(Disposable disposable) {
          }

          public void onNext(LCObject lcObject) {
            System.out.println("remove field finished.");
            object.fetchInBackground("grade").subscribe(new Observer<LCObject>() {
              @Override
              public void onSubscribe(@NotNull Disposable disposable) {

              }

              @Override
              public void onNext(@NotNull LCObject aObject) {
                testSucceed = aObject.get("grade") == null;
                if (!testSucceed) {
                  latch.countDown();
                  return;
                }
                object.deleteInBackground().subscribe(new Observer<LCNull>() {
                  @Override
                  public void onSubscribe(Disposable disposable) {

                  }

                  @Override
                  public void onNext(LCNull LCNull) {
                    testSucceed = true;
                    latch.countDown();
                  }

                  @Override
                  public void onError(Throwable throwable) {
                    latch.countDown();
                  }

                  @Override
                  public void onComplete() {

                  }
                });
              }

              @Override
              public void onError(@NotNull Throwable throwable) {
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });
          }

          public void onError(Throwable throwable) {
            latch.countDown();
          }

          public void onComplete() {
          }
        });

      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testFetchRemovedPointerAttr() throws Exception {
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    final LCObject friend = new LCObject("Student");
    friend.put("name", "tom");
    object.put("friend", friend);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject lcObject) {
        System.out.println("try to remove grade field.");
        LCObject tmpObj = LCObject.createWithoutData("Student", object.getObjectId());
        tmpObj.remove("grade");
        tmpObj.remove("friend");
        tmpObj.saveInBackground().subscribe(new Observer<LCObject>() {
          public void onSubscribe(Disposable disposable) {
          }

          public void onNext(LCObject lcObject) {
            System.out.println("remove field finished.");
            object.fetchInBackground("grade,friend.name").subscribe(new Observer<LCObject>() {
              @Override
              public void onSubscribe(@NotNull Disposable disposable) {

              }

              @Override
              public void onNext(@NotNull LCObject aObject) {
                testSucceed = aObject.get("grade") == null;
                if (!testSucceed) {
                  System.out.println("failed to remote grade attr");
                  latch.countDown();
                  return;
                }
                testSucceed = aObject.get("friend") == null;
                if (!testSucceed) {
                  System.out.println("failed to remote friend attr");
                  latch.countDown();
                  return;
                }
                System.out.println("succeed to remote grade/friend attr");
                object.deleteInBackground().subscribe(new Observer<LCNull>() {
                  @Override
                  public void onSubscribe(Disposable disposable) {

                  }

                  @Override
                  public void onNext(LCNull LCNull) {
                    System.out.println("succeed to delete origin student object");
                    friend.deleteInBackground().blockingFirst();
                    testSucceed = true;
                    latch.countDown();
                  }

                  @Override
                  public void onError(Throwable throwable) {
                    latch.countDown();
                  }

                  @Override
                  public void onComplete() {

                  }
                });
              }

              @Override
              public void onError(@NotNull Throwable throwable) {
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });
          }

          public void onError(Throwable throwable) {
            latch.countDown();
          }

          public void onComplete() {
          }
        });

      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testPutNull() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", null);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println("saveObject field finished.");
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testDeleteField() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        System.out.println("try to remove grade field.");
        LCObject.remove("grade");
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          public void onSubscribe(Disposable disposable) {
          }

          public void onNext(LCObject LCObject) {
            System.out.println("remove field finished.");
            LCObject.deleteInBackground().subscribe(new Observer<LCNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCNull LCNull) {
                testSucceed = true;
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });
          }

          public void onError(Throwable throwable) {
            latch.countDown();
          }

          public void onComplete() {
          }
        });

      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testIncrementField() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.put("ratings", 3.5);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        System.out.println("try to increment grade and ratings field.");
        LCObject.increment("grade", -1);
        LCObject.increment("ratings", 0.8);
        LCObject.setFetchWhenSave(true);
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          public void onSubscribe(Disposable disposable) {
          }

          public void onNext(LCObject LCObject2) {
            System.out.println("update finished: " + LCObject2);
            LCObject2.deleteInBackground().subscribe(new Observer<LCNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCNull LCNull) {
                testSucceed = true;
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });
          }

          public void onError(Throwable throwable) {
            latch.countDown();
          }

          public void onComplete() {
          }
        });

      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCreateObjectWithPublicACL() throws Exception {
    LCACL acl = new LCACL();
    acl.setPublicWriteAccess(true);
    acl.setPublicReadAccess(true);
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.setACL(acl);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        LCObject.addUnique("course", Arrays.asList("Math", "Reading"));
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(LCObject LCObject) {
            System.out.println("[Thread:" + Thread.currentThread().getId() +
                    "]update object finished. objectId=" + LCObject.getObjectId() + ", className=" + LCObject.getClassName());
            LCObject.deleteInBackground().subscribe(new Observer<LCNull>() {
              public void onSubscribe(Disposable disposable) {
                ;
              }

              public void onNext(LCNull aVoid) {
                System.out.println("delete object finished!");
                testSucceed = true;
                latch.countDown();
              }

              public void onError(Throwable throwable) {
                latch.countDown();
              }

              public void onComplete() {
              }
            });

          }

          public void onError(Throwable throwable) {
            latch.countDown();
          }

          public void onComplete() {

          }
        });
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCreateObjectWithReadOnlyACL() throws Exception {
    LCACL acl = new LCACL();
    acl.setPublicWriteAccess(false);
    acl.setPublicReadAccess(true);
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.setACL(acl);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        System.out.println("[Thread:" + Thread.currentThread().getId() +
                "]update object finished. objectId=" + LCObject.getObjectId() + ", className=" + LCObject.getClassName());
        LCObject.deleteInBackground().subscribe(new Observer<LCNull>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(LCNull aVoid) {
            System.out.println("delete object finished, but ACL doesn't work!");
            latch.countDown();
          }

          public void onError(Throwable throwable) {
            System.out.println("delete object failed as expected.");
            testSucceed = (null != throwable);
            latch.countDown();
          }

          public void onComplete() {
          }
        });
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCreateObject() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 17);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {
      }

      public void onNext(LCObject LCObject) {
        System.out.println("create object finished. objectId=" + LCObject.getObjectId() + ", className=" + LCObject.getClassName());
        LCObject.deleteInBackground().subscribe(new Observer<LCNull>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(LCNull aVoid) {
            System.out.println("delete object finished.");
            testSucceed = true;
            latch.countDown();
          }

          public void onError(Throwable throwable) {
            System.out.println("delete object failed.");
            latch.countDown();
          }

          public void onComplete() {
          }
        });

      }

      public void onError(Throwable throwable) {
        System.out.println("create object failed.");
        latch.countDown();
      }

      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCreateAndFetchObject() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 17);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {
      }

      public void onNext(LCObject LCObject) {
        System.out.println("create object finished. objectId=" + LCObject.getObjectId()
                + ", className=" + LCObject.getClassName());
        Date updatedAtDate = LCObject.getUpdatedAt();
        Date createdAtDate = LCObject.getCreatedAt();
        String updatedString = LCObject.getUpdatedAtString();
        String createdString = LCObject.getCreatedAtString();
        System.out.println("updatedAt:" + updatedAtDate + ", createdAt:" + createdAtDate + ", updatedString:"
                + updatedString + ", createdString:" + createdString);
        LCObject.deleteInBackground().subscribe(new Observer<LCNull>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(LCNull aVoid) {
            System.out.println("delete object finished.");
            testSucceed = true;
            latch.countDown();
          }

          public void onError(Throwable throwable) {
            System.out.println("delete object failed.");
            latch.countDown();
          }

          public void onComplete() {
          }
        });

      }

      public void onError(Throwable throwable) {
        System.out.println("create object failed.");
        latch.countDown();
      }

      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testObjectRefresh() throws Exception {
    LCObject object = new LCObject("Student");
    object.setObjectId("5a7a4ac8128fe1003768d2b1");
    object.refreshInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        System.out.println("subscribe result: " + LCObject.toString());
        testSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("subscribe completed.");
      }
    });
    latch.await();
    assertTrue(testSucceed);
    System.out.println("test completed.");
  }

  public void testIncrementOperation() throws Exception {
    final LCObject prepare = new LCObject("Student");
    prepare.put("name", "Automatic Tester");
    prepare.put("age", 17);
    prepare.save();

    LCObject object = new LCObject("Student");
    object.setObjectId(prepare.getObjectId());
    object.increment("age", 5);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        System.out.println("new value of age: " + LCObject.getInt("age"));
        LCObject.decrement("age");
        LCObject.setFetchWhenSave(false);
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(LCObject LCObject) {
            System.out.println("new value of age: " + LCObject.getInt("age"));
            testSucceed = true;
            latch.countDown();
          }

          public void onError(Throwable throwable) {
            latch.countDown();
          }

          public void onComplete() {

          }
        });

      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);

    prepare.delete();
  }

  public void testCreateObjectThenBitOperation() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 39);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        LCObject.bitAnd("age", 0x32);
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(LCObject LCObject) {
            LCObject.bitOr("age", 0x12);
            LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
              public void onSubscribe(Disposable disposable) {

              }

              public void onNext(LCObject LCObject) {
                LCObject.bitXor("age", 0x3208);
                LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
                  public void onSubscribe(Disposable disposable) {

                  }

                  public void onNext(LCObject LCObject) {
                    LCObject.deleteInBackground().subscribe(new Observer<LCNull>() {
                      public void onSubscribe(Disposable disposable) {

                      }

                      public void onNext(LCNull LCNull) {
                        System.out.println("OK!");
                        testSucceed = true;
                        latch.countDown();
                      }

                      public void onError(Throwable throwable) {
                        latch.countDown();
                      }

                      public void onComplete() {

                      }
                    });
                  }

                  public void onError(Throwable throwable) {
                    latch.countDown();
                  }

                  public void onComplete() {

                  }
                });
              }

              public void onError(Throwable throwable) {
                latch.countDown();
              }

              public void onComplete() {

              }
            });
          }

          public void onError(Throwable throwable) {
            latch.countDown();
          }

          public void onComplete() {

          }
        });

      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCreateObjectThenRemoveOperation() throws Exception {
    LCACL acl = new LCACL();
    acl.setPublicWriteAccess(true);
    acl.setPublicReadAccess(true);
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.setACL(acl);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        LCObject.removeAll("course", Arrays.asList("Math", "Reading"));
        LCObject.removeAll("course", Arrays.asList("Sport"));
        LCObject.setFetchWhenSave(true);
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(LCObject LCObject2) {
            System.out.println("[Thread:" + Thread.currentThread().getId() +
                    "]update object finished. objectId=" + LCObject2.getObjectId() + ", className=" + LCObject2.getClassName());
            System.out.println(LCObject2.get("course").toString());
            LCObject2.deleteInBackground().subscribe(new Observer<LCNull>() {
              public void onSubscribe(Disposable disposable) {
                ;
              }

              public void onNext(LCNull aVoid) {
                System.out.println("delete object finished!");
                testSucceed = true;
                latch.countDown();
              }

              public void onError(Throwable throwable) {
                latch.countDown();
              }

              public void onComplete() {
              }
            });

          }

          public void onError(Throwable throwable) {
            latch.countDown();
          }

          public void onComplete() {

          }
        });
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCompoundOperation() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        LCObject.add("course", "Sports");
        LCObject.addUnique("course", "Art");
        LCObject.removeAll("course", Arrays.asList("Math"));
        LCObject.removeAll("course", Arrays.asList("Sports"));
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(LCObject LCObject) {
            LCObject.deleteInBackground().subscribe(new Observer<LCNull>() {
              public void onSubscribe(Disposable disposable) {

              }

              public void onNext(LCNull LCNull) {
                testSucceed = true;
                latch.countDown();
              }

              public void onError(Throwable throwable) {
                latch.countDown();
              }

              public void onComplete() {

              }
            });
          }

          public void onError(Throwable throwable) {
            latch.countDown();;
          }

          public void onComplete() {

          }
        });

      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testBatchSaveOperation() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    object.removeAll("course", Arrays.asList("Math"));
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        testSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testSaveFailureWithCircleReference() throws Exception {
    LCObject objectA = new LCObject("Student");
    LCObject objectB = new LCObject("Student");
    objectA.put("friend", objectB);
    objectB.put("friend", objectA);
    objectB.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        testSucceed = (null != throwable);
        if (throwable instanceof LCException) {
          LCException ex = (LCException)throwable;
          testSucceed = testSucceed & (ex.getCode() == LCException.CIRCLE_REFERENCE);
        }
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testSaveFailureWithIndirectlyCircleReference() throws Exception {
    LCObject objectA = new LCObject("Student");
    LCObject objectB = new LCObject("Student");
    LCObject objectC = new LCObject("Student");
    objectA.put("friend", objectC);
    objectB.put("friend", objectA);
    objectC.put("friend", objectB);
    objectC.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        assertNotNull(throwable);
        if (throwable instanceof LCException) {
          LCException ex = (LCException)throwable;
          testSucceed = (ex.getCode() == LCException.CIRCLE_REFERENCE);
        }
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCreateWithSaveOptionShouldAdd() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    LCQuery<LCObject> query = new LCQuery<>("Student");
    query.whereGreaterThan("age", "19");
    LCSaveOption option = new LCSaveOption();
    option.matchQuery = query;
    option.fetchWhenSave = true;
    object.saveInBackground(option).subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
        ;
      }

      @Override
      public void onNext(LCObject LCObject) {
        testSucceed = (null != LCObject);
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCreateWithSaveOptionShouldNotAdd() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    LCQuery<LCObject> query = new LCQuery<>("Student");
    query.whereGreaterThan("age", "900");
    LCSaveOption option = new LCSaveOption();
    option.matchQuery = query;
    option.fetchWhenSave = true;
    object.saveInBackground(option).subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
        ;
      }

      @Override
      public void onNext(LCObject LCObject) {
        testSucceed = (null != LCObject);
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testUpdateInBackground() throws Exception {
    LCObject student = new LCObject("Student");
    student.setObjectId("fparuew3r141233");
    student.put("age", 20);
    student.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        testSucceed = (null != LCObject);
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
  }
  public void testUpdateWithSaveOptionShouldChange() throws Exception {
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    object.save();

    LCQuery<LCObject> query = new LCQuery<>("Student");
    query.whereEqualTo("age", 19);
    query.whereEqualTo("objectId", object.getObjectId());
    LCSaveOption option = new LCSaveOption();
    option.matchQuery = query;

    object.put("age", 30);
    object.saveInBackground(option).subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        object.delete();
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testSaveEventually() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    try {
      object.saveEventually();
      Thread.sleep(2000);
    } catch (Exception ex) {
      fail();
    }
  }

  public void testSaveAllWithCreateOperation() throws Exception {
    List<LCObject> objects = new ArrayList<>(4);
    for (int i = 0; i < 4; i++) {
      LCObject object = new LCObject("Student");
      object.put("name", "Automatic Tester");
      object.put("age", System.currentTimeMillis() / 1000);
      object.add("course", "Art");
      objects.add(object);
    }
    LCObject.saveAll(objects);
    for (int i = 0; i < 4; i++) {
      System.out.println(objects.get(i).getObjectId());
    }
    LCObject.deleteAll(objects);
  }

  public void testSaveAllWithAheadFiles() throws Exception {
    List<LCObject> objects = new ArrayList<>(4);
    for (int i = 0; i < 4; i++) {
      LCObject object = new LCObject("Student");
      object.put("name", "Automatic Tester");
      object.put("age", System.currentTimeMillis() / 1000);
      object.add("course", "Art");
      LCFile test = new LCFile("current Student", StringUtil.getRandomString(64).getBytes());
      object.add("exercise", test);
      objects.add(object);
    }
    LCObject.saveAll(objects);
    for (int i = 0; i < 4; i++) {
      System.out.println(objects.get(i).getObjectId());
    }
    LCObject.deleteAll(objects);
  }

  public void testSaveAllWithMultiOperation() throws Exception {
    List<LCObject> objects = new ArrayList<>(4);
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", System.currentTimeMillis() / 1000);
    object.add("course", "Art");
    object.save();
    System.out.println(object.getObjectId());

    object.put("age", System.currentTimeMillis() / 1000);
    objects.add(object);

    for (int i = 0; i < 3; i++) {
      LCObject tmp = new LCObject("Student");
      tmp.put("name", "Automatic Tester");
      tmp.put("age", System.currentTimeMillis() / 1000);
      tmp.add("course", "Art");
      objects.add(tmp);
    }
    LCObject.saveAll(objects);
    for (int i = 0; i < 4; i++) {
      System.out.println(objects.get(i).getObjectId());
    }

    LCObject.deleteAll(objects);
  }

  public void testDeleteEventually() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    object.save();
    try {
      object.deleteEventually();
      Thread.sleep(2000);
    } catch (Exception ex) {
      fail();
    }
  }

  public void testACLDeserialized() {
    LCObject object = new LCObject("Student");
    HashMap<String, Object> acl = new HashMap<>();
    HashMap<String, Object> aclValue = new HashMap<>();
    aclValue.put("read", true);
    aclValue.put("write", true);
    acl.put("*", aclValue);
    object.serverData.put("ACL",acl);
    LCACL getACL = object.getACL();
    assertNotNull(getACL);
  }

  public void testEmbedLocation() throws Exception {
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    object.put("location", new LCGeoPoint(34.6, 76.43));
    object.save();
  }

  public void testStringIndexOutOfBoundsException() throws Exception {
    try {
      Map<String, String> payMap = new HashMap<>();
      LCObject payInfo = new LCObject("PayInfo");
      payInfo.put("notifyState", 0);
      payInfo.put("prepayId", payMap.get("prepayid"));
      payInfo.put("payState", 0);
      payInfo.put("transactionId", "");
      payInfo.put("vip_expiry_date", "");
      payInfo.put("tradeType", "APP");
      payInfo.put("bankType", "");
      payInfo.put("timeEnd", "");
      payInfo.put("totalFee", 432423);
      payInfo.put("outTradeNo", payMap.get("mchTradeNo"));
      payInfo.put("vipStatus", "");
      payInfo.put("userPhone", "18600433132");
      payInfo.put("vipStatus", false);
      payInfo.put("body", "body");
      payInfo.put("cashFee", "");
      payInfo.saveInBackground().subscribe(new Observer<LCObject>() {
        public void onSubscribe(Disposable disposable) {

        }

        public void onNext(LCObject LCObject) {
          System.out.println("succeed to save Object. objectId:" + LCObject.getObjectId());
          testSucceed = true;
          latch.countDown();
        }

        public void onError(Throwable throwable) {
          // ingore exception.
          testSucceed = true;
          latch.countDown();
        }

        public void onComplete() {

        }
      });
      latch.await();
      assertTrue(testSucceed);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void testUpdateWithSaveOptionShouldNotChange() throws Exception {
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    object.put("location", new LCGeoPoint(34.6, 76.43));
    object.save();

    LCQuery<LCObject> query = new LCQuery<>("Student");
    query.whereEqualTo("age", 29);
    query.whereEqualTo("objectId", object.getObjectId());
    LCSaveOption option = new LCSaveOption();
    option.matchQuery = query;

    object.put("age", 30);
    object.saveInBackground(option).subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        object.delete();
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        object.delete();
        testSucceed = true;
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
