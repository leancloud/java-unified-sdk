package cn.leancloud;

import cn.leancloud.types.AVGeoPoint;
import cn.leancloud.types.AVNull;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class AVObjectTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public AVObjectTest(String testName) {
    super(testName);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(AVObjectTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testDateAttribute() throws Exception {
    final Date now = new Date();
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", null);
    object.put("lastOcc", now);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println("saveObject field finished.");
        Date savedDate = avObject.getDate("lastOcc");
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

  public void testPutNull() throws Exception {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", null);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
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
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println("try to remove grade field.");
        avObject.remove("grade");
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {
          }

          public void onNext(AVObject avObject) {
            System.out.println("remove field finished.");
            avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(AVNull avNull) {
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
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.put("ratings", 3.5);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println("try to increment grade and ratings field.");
        avObject.increment("grade", -1);
        avObject.increment("ratings", 0.8);
        avObject.setFetchWhenSave(true);
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {
          }

          public void onNext(AVObject avObject2) {
            System.out.println("update finished: " + avObject2);
            avObject2.deleteInBackground().subscribe(new Observer<AVNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(AVNull avNull) {
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
    AVACL acl = new AVACL();
    acl.setPublicWriteAccess(true);
    acl.setPublicReadAccess(true);
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.setACL(acl);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        avObject.addUnique("course", Arrays.asList("Math", "Reading"));
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(AVObject avObject) {
            System.out.println("[Thread:" + Thread.currentThread().getId() +
                    "]update object finished. objectId=" + avObject.getObjectId() + ", className=" + avObject.getClassName());
            avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
              public void onSubscribe(Disposable disposable) {
                ;
              }

              public void onNext(AVNull aVoid) {
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
    AVACL acl = new AVACL();
    acl.setPublicWriteAccess(false);
    acl.setPublicReadAccess(true);
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.setACL(acl);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println("[Thread:" + Thread.currentThread().getId() +
                "]update object finished. objectId=" + avObject.getObjectId() + ", className=" + avObject.getClassName());
        avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(AVNull aVoid) {
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
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 17);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {
      }

      public void onNext(AVObject avObject) {
        System.out.println("create object finished. objectId=" + avObject.getObjectId() + ", className=" + avObject.getClassName());
        avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(AVNull aVoid) {
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
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 17);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {
      }

      public void onNext(AVObject avObject) {
        System.out.println("create object finished. objectId=" + avObject.getObjectId()
                + ", className=" + avObject.getClassName());
        Date updatedAtDate = avObject.getUpdatedAt();
        Date createdAtDate = avObject.getCreatedAt();
        String updatedString = avObject.getUpdatedAtString();
        String createdString = avObject.getCreatedAtString();
        System.out.println("updatedAt:" + updatedAtDate + ", createdAt:" + createdAtDate + ", updatedString:"
                + updatedString + ", createdString:" + createdString);
        avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(AVNull aVoid) {
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
    AVObject object = new AVObject("Student");
    object.setObjectId("5a7a4ac8128fe1003768d2b1");
    object.refreshInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println("subscribe result: " + avObject.toString());
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
    final AVObject prepare = new AVObject("Student");
    prepare.put("name", "Automatic Tester");
    prepare.put("age", 17);
    prepare.save();

    AVObject object = new AVObject("Student");
    object.setObjectId(prepare.getObjectId());
    object.increment("age", 5);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println("new value of age: " + avObject.getInt("age"));
        avObject.decrement("age");
        avObject.setFetchWhenSave(false);
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(AVObject avObject) {
            System.out.println("new value of age: " + avObject.getInt("age"));
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
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 39);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        avObject.bitAnd("age", 0x32);
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(AVObject avObject) {
            avObject.bitOr("age", 0x12);
            avObject.saveInBackground().subscribe(new Observer<AVObject>() {
              public void onSubscribe(Disposable disposable) {

              }

              public void onNext(AVObject avObject) {
                avObject.bitXor("age", 0x3208);
                avObject.saveInBackground().subscribe(new Observer<AVObject>() {
                  public void onSubscribe(Disposable disposable) {

                  }

                  public void onNext(AVObject avObject) {
                    avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
                      public void onSubscribe(Disposable disposable) {

                      }

                      public void onNext(AVNull avNull) {
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
    AVACL acl = new AVACL();
    acl.setPublicWriteAccess(true);
    acl.setPublicReadAccess(true);
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.setACL(acl);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        avObject.removeAll("course", Arrays.asList("Math", "Reading"));
        avObject.removeAll("course", Arrays.asList("Sport"));
        avObject.setFetchWhenSave(true);
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(AVObject avObject2) {
            System.out.println("[Thread:" + Thread.currentThread().getId() +
                    "]update object finished. objectId=" + avObject2.getObjectId() + ", className=" + avObject2.getClassName());
            System.out.println(avObject2.get("course").toString());
            avObject2.deleteInBackground().subscribe(new Observer<AVNull>() {
              public void onSubscribe(Disposable disposable) {
                ;
              }

              public void onNext(AVNull aVoid) {
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
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        avObject.add("course", "Sports");
        avObject.addUnique("course", "Art");
        avObject.removeAll("course", Arrays.asList("Math"));
        avObject.removeAll("course", Arrays.asList("Sports"));
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(AVObject avObject) {
            avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
              public void onSubscribe(Disposable disposable) {

              }

              public void onNext(AVNull avNull) {
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
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    object.removeAll("course", Arrays.asList("Math"));
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
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
    AVObject objectA = new AVObject("Student");
    AVObject objectB = new AVObject("Student");
    objectA.put("friend", objectB);
    objectB.put("friend", objectA);
    objectB.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        testSucceed = (null != throwable);
        if (throwable instanceof AVException) {
          AVException ex = (AVException)throwable;
          testSucceed = testSucceed & (ex.getCode() == AVException.CIRCLE_REFERENCE);
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
    AVObject objectA = new AVObject("Student");
    AVObject objectB = new AVObject("Student");
    AVObject objectC = new AVObject("Student");
    objectA.put("friend", objectC);
    objectB.put("friend", objectA);
    objectC.put("friend", objectB);
    objectC.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        assertNotNull(throwable);
        if (throwable instanceof AVException) {
          AVException ex = (AVException)throwable;
          testSucceed = (ex.getCode() == AVException.CIRCLE_REFERENCE);
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
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    AVQuery<AVObject> query = new AVQuery<>("Student");
    query.whereGreaterThan("age", "19");
    AVSaveOption option = new AVSaveOption();
    option.matchQuery = query;
    option.fetchWhenSave = true;
    object.saveInBackground(option).subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
        ;
      }

      @Override
      public void onNext(AVObject avObject) {
        testSucceed = (null != avObject);
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
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    AVQuery<AVObject> query = new AVQuery<>("Student");
    query.whereGreaterThan("age", "900");
    AVSaveOption option = new AVSaveOption();
    option.matchQuery = query;
    option.fetchWhenSave = true;
    object.saveInBackground(option).subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
        ;
      }

      @Override
      public void onNext(AVObject avObject) {
        testSucceed = (null != avObject);
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
    AVObject student = new AVObject("Student");
    student.setObjectId("fparuew3r141233");
    student.put("age", 20);
    student.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        testSucceed = (null != avObject);
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
    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    object.save();

    AVQuery<AVObject> query = new AVQuery<>("Student");
    query.whereEqualTo("age", 19);
    query.whereEqualTo("objectId", object.getObjectId());
    AVSaveOption option = new AVSaveOption();
    option.matchQuery = query;

    object.put("age", 30);
    object.saveInBackground(option).subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
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
    AVObject object = new AVObject("Student");
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
    List<AVObject> objects = new ArrayList<>(4);
    for (int i = 0; i < 4; i++) {
      AVObject object = new AVObject("Student");
      object.put("name", "Automatic Tester");
      object.put("age", System.currentTimeMillis() / 1000);
      object.add("course", "Art");
      objects.add(object);
    }
    AVObject.saveAll(objects);
    for (int i = 0; i < 4; i++) {
      System.out.println(objects.get(i).getObjectId());
    }
    AVObject.deleteAll(objects);
  }

  public void testSaveAllWithMultiOperation() throws Exception {
    List<AVObject> objects = new ArrayList<>(4);
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", System.currentTimeMillis() / 1000);
    object.add("course", "Art");
    object.save();
    System.out.println(object.getObjectId());

    object.put("age", System.currentTimeMillis() / 1000);
    objects.add(object);

    for (int i = 0; i < 3; i++) {
      AVObject tmp = new AVObject("Student");
      tmp.put("name", "Automatic Tester");
      tmp.put("age", System.currentTimeMillis() / 1000);
      tmp.add("course", "Art");
      objects.add(tmp);
    }
    AVObject.saveAll(objects);
    for (int i = 0; i < 4; i++) {
      System.out.println(objects.get(i).getObjectId());
    }

    AVObject.deleteAll(objects);
  }

  public void testDeleteEventually() throws Exception {
    AVObject object = new AVObject("Student");
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
    AVObject object = new AVObject("Student");
    HashMap<String, Object> acl = new HashMap<>();
    HashMap<String, Object> aclValue = new HashMap<>();
    aclValue.put("read", true);
    aclValue.put("write", true);
    acl.put("*", aclValue);
    object.serverData.put("ACL",acl);
    AVACL getACL = object.getACL();
    assertNotNull(getACL);
  }

  public void testEmbedLocation() throws Exception {
    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    JSONObject localtion = new JSONObject();
    object.put("location", new AVGeoPoint(34.6, 76.43));
    object.save();
  }

  public void testStringIndexOutOfBoundsException() throws Exception {
    try {
      Map<String, String> payMap = new HashMap<>();
      AVObject payInfo = new AVObject("PayInfo");
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
      payInfo.saveInBackground().subscribe(new Observer<AVObject>() {
        public void onSubscribe(Disposable disposable) {

        }

        public void onNext(AVObject avObject) {
          System.out.println("succeed to save Object. objectId:" + avObject.getObjectId());
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
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void testUpdateWithSaveOptionShouldNotChange() throws Exception {
    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    object.put("location", new AVGeoPoint(34.6, 76.43));
    object.save();

    AVQuery<AVObject> query = new AVQuery<>("Student");
    query.whereEqualTo("age", 29);
    query.whereEqualTo("objectId", object.getObjectId());
    AVSaveOption option = new AVSaveOption();
    option.matchQuery = query;

    object.put("age", 30);
    object.saveInBackground(option).subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
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
