package cn.leancloud;

import cn.leancloud.auth.UserBasedTestCase;
import cn.leancloud.types.LCNull;
import cn.leancloud.json.JSONObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FollowAndStatusTest extends UserBasedTestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public FollowAndStatusTest(String name) {
    super(name);
    setAuthUser("jfeng", UserFollowshipTest.DEFAULT_PASSWD);
  }

  public static Test suite() {
    return new TestSuite(FollowAndStatusTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  private void userLogin(String username, String password) throws Exception {
//    AVUser user = new AVUser();
//    user.setEmail("jfeng@test.com");
//    user.setUsername("jfeng");
//    user.setPassword("FER$@$@#Ffwe");
    final CountDownLatch userLatch = new CountDownLatch(1);
    LCUser.logIn(username, password).subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCUser avUser) {
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
    LCUser.currentUser().logOut();

    LCStatus status = LCStatus.createStatus("", "just a test");
    status.sendToFollowersInBackground().subscribe(new Observer<LCStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCStatus avNull) {
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
    LCStatus status = LCStatus.createStatus("", "just a test");
    status.sendPrivatelyInBackground("notExistedUSer").subscribe(new Observer<LCStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCStatus avStatus) {
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
    LCStatus status = LCStatus.createStatus("", "just a test");
    LCQuery userQuery = LCUser.getQuery();
    userQuery.whereEqualTo("objectId", "anotherNotExistedUser");
    status.sendToUsersInBackground("test", userQuery).subscribe(new Observer<LCStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCStatus avStatus) {
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
    LCStatus status = LCStatus.createStatus("", "just a test");
    status.sendToFollowersInBackground().subscribe(new Observer<LCStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCStatus avStatus) {
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
    LCUser currentUser = LCUser.currentUser();
    final String currentUserObjectId = currentUser.getObjectId();
    currentUser.logOut();

    LCStatus.statusQuery(LCObject.createWithoutData(LCUser.class, currentUserObjectId))
            .findInBackground()
            .subscribe(new Observer<List<LCStatus>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<LCStatus> avStatuses) {
        testSucceed = true;
        for (LCStatus status: avStatuses) {
          System.out.println(status);
          if (LCStatus.INBOX_TYPE.PRIVATE.toString().equals(status.getInboxType())) {
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
    LCUser currentUser = LCUser.currentUser();
    LCStatus.statusQuery(currentUser)
            .findInBackground()
            .subscribe(new Observer<List<LCStatus>>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(List<LCStatus> avStatuses) {
                testSucceed = true;
                for (LCStatus status: avStatuses) {
                  System.out.println(status);
                  if (LCStatus.INBOX_TYPE.PRIVATE.toString().equals(status.getInboxType())) {
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
    LCUser currentUser = LCUser.currentUser();
    LCStatus.statusQuery(currentUser).countInBackground().subscribe(new Observer<Integer>() {
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
    LCUser currentUser = LCUser.currentUser();
    LCStatus.inboxQuery(currentUser, LCStatus.INBOX_TYPE.TIMELINE.toString()).countInBackground().subscribe(new Observer<Integer>() {
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
    LCUser currentUser = LCUser.currentUser();
    final String currentUserObjectId = currentUser.getObjectId();
    currentUser.logOut();

    LCUser owner = LCObject.createWithoutData(LCUser.class, currentUserObjectId);
    LCStatus.inboxQuery(owner, LCStatus.INBOX_TYPE.PRIVATE.toString())
            .findInBackground()
            .subscribe(new Observer<List<LCStatus>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<LCStatus> avStatuses) {
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
    LCStatus.inboxQuery(LCUser.currentUser(), LCStatus.INBOX_TYPE.PRIVATE.toString())
            .findInBackground()
            .subscribe(new Observer<List<LCStatus>>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(List<LCStatus> avStatuses) {
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
    LCStatus.inboxQuery(LCUser.currentUser(), LCStatus.INBOX_TYPE.TIMELINE.toString())
            .findInBackground()
            .subscribe(new Observer<List<LCStatus>>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(List<LCStatus> avStatuses) {
                testSucceed = true;
                for (LCStatus status: avStatuses) {
                  System.out.println(status);
                  System.out.println(status.getInboxType());
                  if (LCStatus.INBOX_TYPE.PRIVATE.toString().equals(status.getInboxType())) {
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
    LCUser currentUser = LCUser.currentUser();
    final String currentUserObjectId = currentUser.getObjectId();
    currentUser.logOut();

    LCUser owner = LCObject.createWithoutData(LCUser.class, currentUserObjectId);
    LCStatus.inboxQuery(owner, LCStatus.INBOX_TYPE.PRIVATE.toString())
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
    LCStatus.inboxQuery(LCUser.currentUser(), LCStatus.INBOX_TYPE.TIMELINE.toString())
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
    final LCStatus status = LCStatus.createStatus("", "just a test from testDeleteStatusAsSource");
    LCQuery userQuery = LCUser.getQuery();
    userQuery.whereEqualTo("objectId", "anotherNotExistedUser");
    status.sendToUsersInBackground("test", userQuery).subscribe(new Observer<LCStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCStatus avStatus) {
        System.out.println(avStatus.getObjectId());
        System.out.println(avStatus.getCreatedAtString());
        status.setObjectId(avStatus.getObjectId());
        status.deleteInBackground().subscribe(new Observer<LCNull>() {
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
    LCStatus status = LCStatus.createStatus("", "just a test from testDeleteStatusAsOwner at " + new Date());
    status.sendToFollowersInBackground().subscribe(new Observer<LCStatus>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(final LCStatus avStatus) {
        System.out.println(avStatus);
        System.out.println("change login user and try to query inbox status...");
        try {
          userLogin("jfeng001", UserFollowshipTest.DEFAULT_PASSWD);
          LCStatus.inboxQuery(LCUser.currentUser(), LCStatus.INBOX_TYPE.TIMELINE.toString())
                  .findInBackground()
                  .subscribe(new Observer<List<LCStatus>>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(List<LCStatus> avStatuses) {
              if (null == avStatuses || avStatuses.size() < 1) {
                System.out.println("unfortunately, new user has no inbox status, test failed.");
                latch.countDown();
              }
              avStatuses.get(0).deleteInBackground().subscribe(new Observer<LCNull>() {
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
    String jfengObjectId = LCUser.currentUser().getObjectId();
    System.out.println("follower-jfeng001 login...");
    userLogin("jfeng001", UserFollowshipTest.DEFAULT_PASSWD);
    LCUser jfeng001 = LCUser.currentUser();
    System.out.println("follower-jfeng001 follow jfeng...");
    jfeng001.followInBackground(jfengObjectId).blockingFirst();

    System.out.println("jfeng login...");
    userLogin("jfeng", UserFollowshipTest.DEFAULT_PASSWD);
    LCUser jfeng = LCUser.currentUser();

    int pageSize = 50;
    System.out.println("jfeng send status to followers...");
    for(int i = 0; i < 200; i++) {
      LCStatus status = LCStatus.createStatus("", "just a test, index=" + i);
      final CountDownLatch tmpLatch = new CountDownLatch(1);
      status.sendToFollowersInBackground().subscribe(new Observer<LCStatus>() {
        @Override
        public void onSubscribe(Disposable disposable) {

        }

        @Override
        public void onNext(LCStatus avStatus) {
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
    final List<LCStatus> ownedStatuses = new ArrayList<>();

    LCStatusQuery statusQuery = LCStatus.statusQuery(jfeng);
    statusQuery.setPageSize(pageSize);
    List<LCStatus> tmpResult = statusQuery.find();
    System.out.println("statusQuery first round result: " + tmpResult.size());
    assertTrue(null != tmpResult && tmpResult.size() > 0);
    ownedStatuses.addAll(tmpResult);

    boolean queryEnd = (null == tmpResult || tmpResult.size() < 1);
    while (!queryEnd) {
      System.out.println("try to query status for user:jfeng....");
      tmpResult = statusQuery.nextInBackground().blockingLast();
      if (null != tmpResult) {
        System.out.println("statusQuery next round result: " + tmpResult.size());
        ownedStatuses.addAll(tmpResult);
      }
      if (null == tmpResult || tmpResult.size() < pageSize) {
        queryEnd = true;
      }
    }

    System.out.println("follower-jfeng001 login...");
    userLogin("jfeng001", UserFollowshipTest.DEFAULT_PASSWD);

    List<LCStatus> inboxStatuses = new ArrayList<>();

    System.out.println("jfeng001 objectId: " + LCUser.currentUser().getObjectId());
    System.out.println("try to query inbox for user:follower-jfeng001....");

    statusQuery = LCStatus.inboxQuery(LCUser.currentUser(), LCStatus.INBOX_TYPE.TIMELINE.toString());
    statusQuery.setPageSize(pageSize);
    tmpResult = statusQuery.find();
    inboxStatuses.addAll(tmpResult);
    System.out.println("inboxQuery first round result: " + tmpResult.size());
    assertTrue(null != tmpResult && tmpResult.size() > 0);
    for (LCStatus s: tmpResult) {
      System.out.println("INBOX STATUS: " + s.toJSONString());
    }

    queryEnd = (null == tmpResult || tmpResult.size() < 1);
    while (!queryEnd) {
      System.out.println("try to query inbox for user:follower-jfeng001....");
      tmpResult = statusQuery.nextInBackground().blockingLast();
      if (null != tmpResult) {
        System.out.println("inboxQuery next round result: " + tmpResult.size());
        inboxStatuses.addAll(tmpResult);
        for (LCStatus s: tmpResult) {
          System.out.println("INBOX STATUS: " + s.toJSONString());
        }
      }
      if (null == tmpResult || tmpResult.size() < pageSize) {
        queryEnd = true;
      }
    }

    System.out.println("follower-jfeng001 delete inbox status, count: " + inboxStatuses.size() + "...");
    int inboxDeleteError = 0;
    for (LCStatus sts : inboxStatuses) {
      try {
        sts.deleteInBackground().blockingFirst();
      } catch(Exception ex) {
        inboxDeleteError++;
        System.out.println("failed to delete inbox status: " + sts + ", cause: " + ex.getMessage());
      }
    }

    System.out.println("jfeng login...");
    userLogin("jfeng", UserFollowshipTest.DEFAULT_PASSWD);
    System.out.println("jfeng delete owned status, count:" + ownedStatuses.size() + "...");
    int ownedDeleteError = 0;
    for (LCStatus sts : ownedStatuses) {
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
