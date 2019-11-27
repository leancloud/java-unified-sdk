package cn.leancloud;

import cn.leancloud.types.AVNull;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FollowAndStatusTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public FollowAndStatusTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(FollowAndStatusTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
    userLogin("jfeng", AVUserFollowshipTest.DEFAULT_PASSWD);
  }

  @Override
  protected void tearDown() throws Exception {
    latch = null;

    AVUser current = AVUser.currentUser();
    if (null != current) {
      current.logOut();
    }
  }

  private void userLogin(String username, String password) throws Exception {
//    AVUser user = new AVUser();
//    user.setEmail("jfeng@test.com");
//    user.setUsername("jfeng");
//    user.setPassword("FER$@$@#Ffwe");
    final CountDownLatch userLatch = new CountDownLatch(1);
    AVUser.logIn(username, password).subscribe(new Observer<AVUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVUser avUser) {
        userLatch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        userLatch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    userLatch.await();
  }

  public void testPostStatusWithoutLoginedUser() throws Exception {
    AVUser.currentUser().logOut();

    AVStatus status = AVStatus.createStatus("", "just a test");
    status.sendToFollowersInBackground().subscribe(new Observer<AVStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVStatus avNull) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
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

  public void testPostPrivateStatus() throws Exception {
    AVStatus status = AVStatus.createStatus("", "just a test");
    status.sendPrivatelyInBackground("notExistedUSer").subscribe(new Observer<AVStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVStatus avStatus) {
        testSucceed = true;
        System.out.println(avStatus.getObjectId());
        System.out.println(avStatus.getCreatedAtString());
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

  public void testPostStatusToSpecificUsers() throws Exception {
    AVStatus status = AVStatus.createStatus("", "just a test");
    AVQuery userQuery = AVUser.getQuery();
    userQuery.whereEqualTo("objectId", "anotherNotExistedUser");
    status.sendToUsersInBackground("test", userQuery).subscribe(new Observer<AVStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVStatus avStatus) {
        testSucceed = true;
        System.out.println(avStatus.getObjectId());
        System.out.println(avStatus.getCreatedAtString());
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

  public void testPostStatusWithLoginedUser() throws Exception {
    AVStatus status = AVStatus.createStatus("", "just a test");
    status.sendToFollowersInBackground().subscribe(new Observer<AVStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVStatus avStatus) {
        testSucceed = true;
        System.out.println(avStatus.getObjectId());
        System.out.println(avStatus.getCreatedAtString());
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

  public void testStatusQueryWithoutLogin() throws Exception {
    AVUser currentUser = AVUser.currentUser();
    final String currentUserObjectId = currentUser.getObjectId();
    currentUser.logOut();

    AVStatus.statusQuery(AVObject.createWithoutData(AVUser.class, currentUserObjectId))
            .findInBackground()
            .subscribe(new Observer<List<AVStatus>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<AVStatus> avStatuses) {
        testSucceed = true;
        for (AVStatus status: avStatuses) {
          System.out.println(status);
          if (AVStatus.INBOX_TYPE.PRIVATE.toString().equals(status.getInboxType())) {
            testSucceed = false;
          }
        }
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

  public void testStatusQuery() throws Exception {
    AVUser currentUser = AVUser.currentUser();
    AVStatus.statusQuery(currentUser)
            .findInBackground()
            .subscribe(new Observer<List<AVStatus>>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(List<AVStatus> avStatuses) {
                testSucceed = true;
                for (AVStatus status: avStatuses) {
                  System.out.println(status);
                  if (AVStatus.INBOX_TYPE.PRIVATE.toString().equals(status.getInboxType())) {
                    testSucceed = false;
                  }
                }

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

  public void testStatusCountQuery() throws Exception {
    AVUser currentUser = AVUser.currentUser();
    AVStatus.statusQuery(currentUser).countInBackground().subscribe(new Observer<Integer>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(Integer integer) {
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

  public void testInboxCountQuery() throws Exception {
    AVUser currentUser = AVUser.currentUser();
    AVStatus.inboxQuery(currentUser, AVStatus.INBOX_TYPE.TIMELINE.toString()).countInBackground().subscribe(new Observer<Integer>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(Integer integer) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
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

  public void testInboxQueryWithoutLogin() throws Exception {
    AVUser currentUser = AVUser.currentUser();
    final String currentUserObjectId = currentUser.getObjectId();
    currentUser.logOut();

    AVUser owner = AVObject.createWithoutData(AVUser.class, currentUserObjectId);
    AVStatus.inboxQuery(owner, AVStatus.INBOX_TYPE.PRIVATE.toString())
            .findInBackground()
            .subscribe(new Observer<List<AVStatus>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<AVStatus> avStatuses) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
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

  public void testInboxQueryWithEmptyResult() throws Exception {
    AVStatus.inboxQuery(AVUser.currentUser(), AVStatus.INBOX_TYPE.PRIVATE.toString())
            .findInBackground()
            .subscribe(new Observer<List<AVStatus>>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(List<AVStatus> avStatuses) {
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

  public void testInboxQueryWithResults() throws Exception {
    AVStatus.inboxQuery(AVUser.currentUser(), AVStatus.INBOX_TYPE.TIMELINE.toString())
            .findInBackground()
            .subscribe(new Observer<List<AVStatus>>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(List<AVStatus> avStatuses) {
                testSucceed = true;
                for (AVStatus status: avStatuses) {
                  System.out.println(status);
                  System.out.println(status.getInboxType());
                  if (AVStatus.INBOX_TYPE.PRIVATE.toString().equals(status.getInboxType())) {
                    testSucceed = false;
                  }
                }
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

  public void testInboxQueryCountWithoutLogin() throws Exception {
    AVUser currentUser = AVUser.currentUser();
    final String currentUserObjectId = currentUser.getObjectId();
    currentUser.logOut();

    AVUser owner = AVObject.createWithoutData(AVUser.class, currentUserObjectId);
    AVStatus.inboxQuery(owner, AVStatus.INBOX_TYPE.PRIVATE.toString())
            .unreadCountInBackground()
            .subscribe(new Observer<JSONObject>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(JSONObject jsonObject) {
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                throwable.printStackTrace();
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

  public void testInboxQueryCountWithResults() throws Exception {
    AVStatus.inboxQuery(AVUser.currentUser(), AVStatus.INBOX_TYPE.TIMELINE.toString())
            .unreadCountInBackground()
            .subscribe(new Observer<JSONObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(JSONObject jsonObject) {
        System.out.println(jsonObject);
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


  public void testDeleteStatusAsSource() throws Exception {
    // delete source status
    final AVStatus status = AVStatus.createStatus("", "just a test from testDeleteStatusAsSource");
    AVQuery userQuery = AVUser.getQuery();
    userQuery.whereEqualTo("objectId", "anotherNotExistedUser");
    status.sendToUsersInBackground("test", userQuery).subscribe(new Observer<AVStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVStatus avStatus) {
        System.out.println(avStatus.getObjectId());
        System.out.println(avStatus.getCreatedAtString());
        status.setObjectId(avStatus.getObjectId());
        status.deleteInBackground().subscribe(new Observer<AVNull>() {
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
            throwable.printStackTrace();
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
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

  public void testDeleteStatusAsOwner() throws Exception {
    // delete status from inbox
    AVStatus status = AVStatus.createStatus("", "just a test from testDeleteStatusAsOwner at " + new Date());
    status.sendToFollowersInBackground().subscribe(new Observer<AVStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(final AVStatus avStatus) {
        System.out.println(avStatus);
        System.out.println("change login user and try to query inbox status...");
        try {
          userLogin("jfeng001", AVUserFollowshipTest.DEFAULT_PASSWD);
          AVStatus.inboxQuery(AVUser.currentUser(), AVStatus.INBOX_TYPE.TIMELINE.toString())
                  .findInBackground()
                  .subscribe(new Observer<List<AVStatus>>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(List<AVStatus> avStatuses) {
              if (null == avStatuses || avStatuses.size() < 1) {
                System.out.println("unfortunately, new user has no inbox status, test failed.");
                latch.countDown();
              }
              avStatuses.get(0).deleteInBackground().subscribe(new Observer<AVNull>() {
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
                  System.out.println("failed to delete inbox status, cause: " + throwable.getMessage());
                  latch.countDown();
                }

                @Override
                public void onComplete() {

                }
              });
            }

            @Override
            public void onError(Throwable throwable) {
              System.out.println("failed to query inbox status for new user. cause: " + throwable.getMessage());
              latch.countDown();
            }

            @Override
            public void onComplete() {

            }
          });
        } catch (Exception ex) {
          System.out.println("failed to login with new user. cause: " + ex.getMessage());
          latch.countDown();
        }
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to publish timeline status. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testStatusQueryPagination() throws Exception {
    String jfengObjectId = AVUser.currentUser().getObjectId();
    System.out.println("follower-jfeng001 login...");
    userLogin("jfeng001", AVUserFollowshipTest.DEFAULT_PASSWD);
    AVUser jfeng001 = AVUser.currentUser();
    System.out.println("follower-jfeng001 follow jfeng...");
    jfeng001.followInBackground(jfengObjectId).blockingFirst();

    System.out.println("jfeng login...");
    userLogin("jfeng", AVUserFollowshipTest.DEFAULT_PASSWD);
    AVUser jfeng = AVUser.currentUser();

    int pageSize = 5;
    System.out.println("jfeng send status to followers...");
    for(int i = 0; i < 19; i++) {
      AVStatus status = AVStatus.createStatus("", "just a test, index=" + i);
      final CountDownLatch tmpLatch = new CountDownLatch(1);
      status.sendToFollowersInBackground().subscribe(new Observer<AVStatus>() {
        @Override
        public void onSubscribe(Disposable disposable) {

        }

        @Override
        public void onNext(AVStatus avStatus) {
          System.out.println("publish status: " + avStatus);
          tmpLatch.countDown();
        }

        @Override
        public void onError(Throwable throwable) {
          System.out.println("failed to publish status, cause: " + throwable.getMessage());
          tmpLatch.countDown();
        }

        @Override
        public void onComplete() {
        }
      });
      tmpLatch.await();
    }

    System.out.println("try to query status for user:jfeng....");
    final List<AVStatus> ownedStatuses = new ArrayList<>();

    AVStatusQuery statusQuery = AVStatus.statusQuery(jfeng);
    statusQuery.setPageSize(pageSize);
    List<AVStatus> tmpResult = statusQuery.find();
    assertTrue(null != tmpResult && tmpResult.size() == pageSize);
    ownedStatuses.addAll(tmpResult);

    boolean queryEnd = false;
    while (!queryEnd) {
      System.out.println("try to query status for user:jfeng....");
      tmpResult = statusQuery.nextInBackground().blockingLast();
      if (null != tmpResult) {
        ownedStatuses.addAll(tmpResult);
      }
      if (null == tmpResult || tmpResult.size() < pageSize) {
        queryEnd = true;
      }
    }

    System.out.println("follower-jfeng001 login...");
    userLogin("jfeng001", AVUserFollowshipTest.DEFAULT_PASSWD);

    List<AVStatus> inboxStatuses = new ArrayList<>();

    System.out.println("try to query inbox for user:follower-jfeng001....");

    statusQuery = AVStatus.inboxQuery(AVUser.currentUser(), AVStatus.INBOX_TYPE.TIMELINE.toString());
    statusQuery.setPageSize(pageSize);
    tmpResult = statusQuery.find();
    inboxStatuses.addAll(tmpResult);
    assertTrue(null != tmpResult && tmpResult.size() == pageSize);
    for (AVStatus s: tmpResult) {
      System.out.println("INBOX STATUS: " + s.toJSONString());
    }

    queryEnd = false;
    while (!queryEnd) {
      System.out.println("try to query inbox for user:follower-jfeng001....");
      tmpResult = statusQuery.nextInBackground().blockingLast();
      if (null != tmpResult) {
        inboxStatuses.addAll(tmpResult);
        for (AVStatus s: tmpResult) {
          System.out.println("INBOX STATUS: " + s.toJSONString());
        }
      }
      if (null == tmpResult || tmpResult.size() < pageSize) {
        queryEnd = true;
      }
    }

    System.out.println("follower-jfeng001 delete inbox status, count: " + inboxStatuses.size() + "...");
    int inboxDeleteError = 0;
    for (AVStatus sts : inboxStatuses) {
      try {
        sts.deleteInBackground().blockingFirst();
      } catch(Exception ex) {
        inboxDeleteError++;
        System.out.println("failed to delete inbox status: " + sts + ", cause: " + ex.getMessage());
      }
    }

    System.out.println("jfeng login...");
    userLogin("jfeng", AVUserFollowshipTest.DEFAULT_PASSWD);
    System.out.println("jfeng delete owned status, count:" + ownedStatuses.size() + "...");
    int ownedDeleteError = 0;
    for (AVStatus sts : ownedStatuses) {
      try {
        sts.deleteInBackground().blockingFirst();
      } catch (Exception ex) {
        ownedDeleteError++;
        System.out.println("failed to delete status: " + sts + ", cause: " + ex.getMessage());
      }
    }

    System.out.println("ownedStatusDeleteCount=" + ownedDeleteError + ", inboxStatusDeleteCount=" + inboxDeleteError);
    assertTrue(0 == ownedDeleteError);
    assertTrue(0 == inboxDeleteError);
  }
}
