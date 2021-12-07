package cn.leancloud;

import cn.leancloud.auth.UserBasedTestCase;
import cn.leancloud.json.JSON;
import cn.leancloud.types.LCNull;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Ignore;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FriendshipRequestTest extends UserBasedTestCase {
  private boolean testSucceed = false;
  private CountDownLatch latch = null;

  private String testUser1ObjectId = "";
  private String testUser1UserName = "jfeng2020";
  private String testUser1Password = LCUserTest.PASSWORD;
  private String fengObjectId;
  private String fengSessionToken;

  private String dennisObjectId;
  private String dennisSessionToken;

  public FriendshipRequestTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(FriendshipRequestTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testSucceed = false;
    latch = new CountDownLatch(1);

    if (StringUtil.isEmpty(testUser1ObjectId)) {
      testUser1ObjectId = LCUser.logIn(testUser1UserName, testUser1Password).blockingFirst().getObjectId();
    }
    if (StringUtil.isEmpty(fengObjectId)) {
      LCUser feng = LCUserTest.loginOrSignin("jfeng", "FER$@$@#Ffwe", "jfeng@test.com");
      fengObjectId = feng.getObjectId();
      fengSessionToken = feng.getSessionToken();
    }
    if (StringUtil.isEmpty(dennisObjectId)) {
      LCUser dennis = LCUserTest.loginOrSignin("dennis", "FER$@$@#Ffwe", "dennis@test.com");
      dennisObjectId = dennis.getObjectId();
      dennisSessionToken = dennis.getSessionToken();
    }
    System.out.println("FriendshipRequestTest setUp. testUserObjectId=" + testUser1ObjectId
            + ", fengObjectId=" + fengObjectId + ", dennisObjectId=" + dennisObjectId);

    try {
      LCQuery query = new LCQuery("_FriendshipRequest");
      query.whereEqualTo("user", LCUser.createWithoutData(LCUser.class, fengObjectId));
      LCUser feng = LCUserTest.loginOrSignin("jfeng", "FER$@$@#Ffwe", "jfeng@test.com");
      LCObject.deleteAll(feng, query.find());

      query = new LCQuery("_FriendshipRequest");
      query.whereEqualTo("user", LCUser.createWithoutData(LCUser.class, dennisObjectId));
      LCUser dennis = LCUserTest.loginOrSignin("dennis", "FER$@$@#Ffwe", "dennis@test.com");
      LCObject.deleteAll(dennis, query.find());

      query = new LCQuery("_FriendshipRequest");
      query.whereEqualTo("user", LCUser.createWithoutData(LCUser.class, testUser1ObjectId));
      LCUser testUser = LCUser.logIn(testUser1UserName, testUser1Password).blockingFirst();
      LCObject.deleteAll(testUser, query.find());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testStatusSerializer() throws Exception {
    System.out.println(LCFriendshipRequest.RequestStatus.Accepted.name());
    System.out.println(LCFriendshipRequest.RequestStatus.Accepted.name().toLowerCase());
  }

  public void testSimpleRequestWithLoginedUser() throws Exception {
    LCUser.logIn(testUser1UserName, testUser1Password).subscribe(
            new Observer<LCUser>() {
      public void onSubscribe(Disposable disposable) {
      }

      public void onNext(LCUser avUser) {
        LCUser currentUser = LCUser.getCurrentUser();
        System.out.println("step 1: login with testUser. sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

        LCUser friend = null;
        try {
          friend = LCUser.createWithoutData(LCUser.class, fengObjectId);
        } catch (LCException e) {
          e.printStackTrace();
        }
        avUser.applyFriendshipInBackground(friend, null)
                .subscribe(new Observer<LCFriendshipRequest>() {
                  @Override
                  public void onSubscribe(Disposable disposable) {

                  }

                  @Override
                  public void onNext(final LCFriendshipRequest friendshipRequest) {
                    System.out.println("step 2: succeed to create new friend request: testUser -> feng");
                    System.out.println("objectId=" + friendshipRequest.getObjectId());
                    LCUser.becomeWithSessionToken(fengSessionToken, true);
                    friendshipRequest.accept(null).subscribe(new Observer<LCObject>() {
                      @Override
                      public void onSubscribe(Disposable disposable) {

                      }

                      @Override
                      public void onNext(LCObject LCObject) {
                        System.out.println("step 3: succeed to accept new friend request from feng.");
                        friendshipRequest.deleteInBackground().subscribe(new Observer<LCNull>() {
                          @Override
                          public void onSubscribe(Disposable disposable) {

                          }

                          @Override
                          public void onNext(LCNull LCNull) {
                            System.out.println("step 4: succeed to delete new friend request from feng.");
                            testSucceed = true;
                            latch.countDown();
                          }

                          @Override
                          public void onError(Throwable throwable) {
                            System.out.println("failed to delete new friend request from feng.");
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
                        System.out.println("failed to accept new friend request from feng. cause: " + throwable.getMessage());
                        latch.countDown();
                      }

                      @Override
                      public void onComplete() {

                      }
                    });
                  }

                  @Override
                  public void onError(Throwable throwable) {
                    System.out.println("failed to create new friend request from testUser to feng. result=" + throwable.getMessage());
                    if (throwable.getMessage().contains("Friendship already exists.")) {
                      testSucceed = true;
                    }
                    latch.countDown();
                  }

                  @Override
                  public void onComplete() {
                  }
                });
      }

      public void onError(Throwable throwable) {
        System.out.println("failed to login with testUser");
        latch.countDown();
      }

      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testLoginedUserDeclineThenAccept() throws Exception {
    LCUser.logInAnonymously().subscribe(
            new Observer<LCUser>() {
              public void onSubscribe(Disposable disposable) {
              }

              public void onNext(LCUser avUser) {
                LCUser currentUser = LCUser.getCurrentUser();
                System.out.println("login as anonymous user. sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

                LCUser friend = null;
                try {
                  friend = LCUser.createWithoutData(LCUser.class, fengObjectId);
                } catch (LCException e) {
                  e.printStackTrace();
                }
                avUser.applyFriendshipInBackground(friend, null)
                        .subscribe(new Observer<LCFriendshipRequest>() {
                          @Override
                          public void onSubscribe(Disposable disposable) {

                          }

                          @Override
                          public void onNext(final LCFriendshipRequest friendshipRequest) {
                            System.out.println("succeed to create new friend request from anonymouse User to feng.");
                            System.out.println("objectId=" + friendshipRequest.getObjectId());
                            LCUser.becomeWithSessionToken(fengSessionToken, true);
                            friendshipRequest.decline().subscribe(new Observer<LCObject>() {
                              @Override
                              public void onSubscribe(Disposable disposable) {

                              }

                              @Override
                              public void onNext(LCObject LCObject) {
                                System.out.println("succeed to decline new friend request from feng.");
                                try {
                                  System.out.println("sleep 2000 ms...");
                                  Thread.sleep(2000);
                                  System.out.println("try to accept friend request again...");
                                } catch (Exception ex) {
                                  ex.printStackTrace();
                                }
                                friendshipRequest.accept(null).subscribe(new Observer<LCObject>() {
                                  @Override
                                  public void onSubscribe(Disposable disposable) {

                                  }

                                  @Override
                                  public void onNext(LCObject LCObject) {
                                    System.out.println("succeed to accept the declined friend request from feng.");
                                    friendshipRequest.deleteInBackground().subscribe(new Observer<LCNull>() {
                                      @Override
                                      public void onSubscribe(Disposable disposable) {

                                      }

                                      @Override
                                      public void onNext(LCNull LCNull) {
                                        System.out.println("succeed to delete new friend request from feng.");
                                        testSucceed = true;
                                        latch.countDown();
                                      }

                                      @Override
                                      public void onError(Throwable throwable) {
                                        System.out.println("failed to delete new friend request from feng.");
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
                                    System.out.println("failed to accept the declined friend request from feng. cause: " + throwable.getMessage());
                                    latch.countDown();
                                  }

                                  @Override
                                  public void onComplete() {

                                  }
                                });
                              }

                              @Override
                              public void onError(Throwable throwable) {
                                System.out.println("failed to decline new friend request from feng.");
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
                            System.out.println("failed to create new friend request from anonymous User to feng.");
                            if (throwable.getMessage().indexOf("Friendship already exists") >= 0) {
                              testSucceed = true;
                            } else {
                              throwable.printStackTrace();
                            }
                            latch.countDown();
                          }

                          @Override
                          public void onComplete() {
                          }
                        });
              }

              public void onError(Throwable throwable) {
                System.out.println("failed to login as anonymous user. cause: " + throwable.getMessage());
                latch.countDown();
              }

              public void onComplete() {
              }
            });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testQueryAllFriendshipRequests() throws Exception {
    LCUser.logIn(testUser1UserName, testUser1Password).subscribe(
            new Observer<LCUser>() {
              public void onSubscribe(Disposable disposable) {
              }

              public void onNext(LCUser avUser) {
                LCUser currentUser = LCUser.getCurrentUser();
                System.out.println("currentUser. result=" + JSON.toJSONString(currentUser));
                System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

                LCUser friend = null;
                try {
                  friend = LCUser.createWithoutData(LCUser.class, dennisObjectId);
                } catch (LCException e) {
                  e.printStackTrace();
                }
                avUser.applyFriendshipInBackground(friend, null).subscribe(new Observer<LCFriendshipRequest>() {
                  @Override
                  public void onSubscribe(Disposable disposable) {

                  }

                  @Override
                  public void onNext(LCFriendshipRequest avFriendshipRequest) {
                    LCUser.becomeWithSessionToken(dennisSessionToken, true);
                    LCUser.currentUser().friendshipRequestQuery(
                            LCFriendshipRequest.STATUS_ANY,
                            true, true)
                            .findInBackground()
                            .subscribe(new Observer<List<LCFriendshipRequest>>() {
                              @Override
                              public void onSubscribe(Disposable disposable) {

                              }

                              @Override
                              public void onNext(List<LCFriendshipRequest> avFriendshipRequests) {
                                if (null != avFriendshipRequests && avFriendshipRequests.size() > 0) {
                                  testSucceed = true;
                                }
                                latch.countDown();
                              }

                              @Override
                              public void onError(Throwable throwable) {
                                System.out.println();
                                latch.countDown();
                              }

                              @Override
                              public void onComplete() {

                              }
                            });
                  }

                  @Override
                  public void onError(Throwable throwable) {
                    if (throwable.getMessage().contains("Friendship already exists.")) {
                      LCUser.becomeWithSessionToken("fftsmscei51yyzfgjyuzhlwkl", true);
                      LCUser.currentUser().friendshipRequestQuery(
                              LCFriendshipRequest.STATUS_ANY,
                              true, true)
                              .findInBackground()
                              .subscribe(new Observer<List<LCFriendshipRequest>>() {
                                @Override
                                public void onSubscribe(Disposable disposable) {

                                }

                                @Override
                                public void onNext(List<LCFriendshipRequest> avFriendshipRequests) {
                                  if (null != avFriendshipRequests && avFriendshipRequests.size() > 0) {
                                    testSucceed = true;
                                  }
                                  latch.countDown();
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                  System.out.println();
                                  latch.countDown();
                                }

                                @Override
                                public void onComplete() {

                                }
                              });
                      return;
                    }
                    System.out.println();
                    latch.countDown();
                  }

                  @Override
                  public void onComplete() {

                  }
                });
              }

              public void onError(Throwable throwable) {
                System.out.println();
                latch.countDown();
              }

              public void onComplete() {
              }
            });
    latch.await();
    assertTrue(testSucceed);
  }

  @Ignore
  public void testSimpleRequestWithAnonymousUserAccept() throws Exception {
    LCUser.logInAnonymously().subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(final LCUser anonymousUser) {
        System.out.println("step 1: succeed to login as anonymous user.");
        final LCUser target;
        try {
          target = LCUser.createWithoutData(LCUser.class, testUser1ObjectId);
        }catch (Exception ex) {
          latch.countDown();
          return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("group", "collage");
        anonymousUser.applyFriendshipInBackground(target, param).subscribe(new Observer<LCFriendshipRequest>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCFriendshipRequest avFriendshipRequest) {
            System.out.println("Step 2: succeed to apply friendship request from anonymous user to testUser." +
                    " then try to query all request from current User");
            LCQuery<LCFriendshipRequest> query = anonymousUser.friendshipRequestQuery(LCFriendshipRequest.STATUS_PENDING,
                    true, false);
            query.findInBackground().subscribe(new Observer<List<LCFriendshipRequest>>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(List<LCFriendshipRequest> avFriendshipRequests) {
                System.out.println("Step 3: succeed to query pending request from anonymous user. resultSize=" + avFriendshipRequests.size());
                if (avFriendshipRequests.size() < 1) {
                  latch.countDown();
                  return;
                }
                final LCFriendshipRequest targetFriendshipRequest = avFriendshipRequests.get(0);
                LCUser.logIn(testUser1UserName, testUser1Password).subscribe(new Observer<LCUser>() {
                  @Override
                  public void onSubscribe(Disposable disposable) {

                  }

                  @Override
                  public void onNext(final LCUser secondUser) {
                    System.out.println("Step 4: succeed to login as testUser.");
                    Map<String, Object> param = new HashMap<>();
                    param.put("group", "fans");
                    secondUser.acceptFriendshipRequest(targetFriendshipRequest, param).subscribe(new Observer<LCFriendshipRequest>() {
                      @Override
                      public void onSubscribe(Disposable disposable) {

                      }

                      @Override
                      public void onNext(LCFriendshipRequest avFriendshipRequest) {
                        System.out.println("Step 5: test user succeed to accept friendship request sent from anonymous user.");
                        LCQuery<LCFriendshipRequest> query = secondUser.friendshipRequestQuery(LCFriendshipRequest.STATUS_ACCEPTED, true, true);
                        query.findInBackground().subscribe(new Observer<List<LCFriendshipRequest>>() {
                          @Override
                          public void onSubscribe(Disposable disposable) {

                          }

                          @Override
                          public void onNext(List<LCFriendshipRequest> tmpRequests) {
                            System.out.println("Step 6: test user succeed to query accepted request.");
                            LCQuery<LCFriendship> query = secondUser.friendshipQuery(false);
                            query.whereEqualTo(LCFriendship.ATTR_FRIEND_STATUS, true);
                            query.addDescendingOrder(LCObject.KEY_UPDATED_AT);
                            List<LCFriendship> followees = query.find();
                            if (followees == null || followees.size() < 1) {
                              System.out.println("Step 7: test user failed to query friendship.");
                              latch.countDown();
                              return;
                            }
                            try {
                              LCFriendship friendship = followees.get(0);
                              friendship.put("remark", "丐帮帮主");
                              secondUser.updateFriendship(friendship).subscribe(new Observer<LCObject>() {
                                @Override
                                public void onSubscribe(Disposable disposable) {

                                }

                                @Override
                                public void onNext(LCObject LCObject) {
                                  System.out.println("Step 8: succeed to update friendship: " + LCObject);
                                  testSucceed = true;
                                  latch.countDown();
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                  System.out.println("Step 8: failed to update friendship.");
                                  throwable.printStackTrace();
                                  latch.countDown();
                                }

                                @Override
                                public void onComplete() {

                                }
                              });

                            } catch (Exception ex) {
                              ex.printStackTrace();
                              latch.countDown();
                            }

                          }

                          @Override
                          public void onError(Throwable throwable) {
                            System.out.println("Step 6: test user failed to query friendship request by user: " + testUser1UserName);
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
                        System.out.println("Step 5: test user failed to accept friendship request by user: " + testUser1UserName);
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
                    System.out.println("Step 4: failed to login with user: " + testUser1UserName);
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
                System.out.println("Step 3: failed to query pending Friendship as anonymous user");
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
            System.out.println("Step 2: failed to apply new Friendship as anonymous user");
            if (throwable.getMessage().indexOf("Friendship already exists") >= 0) {
              testSucceed = true;
            } else if (throwable.getMessage().indexOf("Friendship request had previously been declined") >= 0) {
              testSucceed = true;
            } else {
              throwable.printStackTrace();
            }
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("step 1: failed to login as anonymous user");
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

  public void testSimpleRequestWithAnonymousUserDecline() throws Exception {
    LCUser.logInAnonymously().subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(final LCUser anonymousUser) {
        final LCUser target;
        try {
          target = LCUser.createWithoutData(LCUser.class, testUser1ObjectId);
        }catch (Exception ex) {
          latch.countDown();
          return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("group", "collage");
        System.out.println("try to apply friendship to user:" + testUser1ObjectId + " from anonymousUser");
        anonymousUser.applyFriendshipInBackground(target, param).subscribe(new Observer<LCFriendshipRequest>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCFriendshipRequest avFriendshipRequest) {
            System.out.println("try to query all request from current User");
            LCQuery<LCFriendshipRequest> query = anonymousUser.friendshipRequestQuery(LCFriendshipRequest.STATUS_PENDING,
                    false, false);
            query.findInBackground().subscribe(new Observer<List<LCFriendshipRequest>>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(List<LCFriendshipRequest> avFriendshipRequests) {
                System.out.println("succeed to query pending request from anonymous user. resultSize=" + avFriendshipRequests.size());
                if (avFriendshipRequests.size() < 1) {
                  latch.countDown();
                  return;
                }
                final LCFriendshipRequest targetFriendshipRequest = avFriendshipRequests.get(0);
                LCUser.logIn(testUser1UserName, testUser1Password).subscribe(new Observer<LCUser>() {
                  @Override
                  public void onSubscribe(Disposable disposable) {

                  }

                  @Override
                  public void onNext(final LCUser secondUser) {
                    secondUser.declineFriendshipRequest(targetFriendshipRequest).subscribe(new Observer<LCFriendshipRequest>() {
                      @Override
                      public void onSubscribe(Disposable disposable) {

                      }

                      @Override
                      public void onNext(LCFriendshipRequest avFriendshipRequest) {
                        LCQuery<LCFriendshipRequest> query =
                                secondUser.friendshipRequestQuery(LCFriendshipRequest.STATUS_DECLINED, false, true);
                        query.findInBackground().subscribe(new Observer<List<LCFriendshipRequest>>() {
                          @Override
                          public void onSubscribe(Disposable disposable) {

                          }

                          @Override
                          public void onNext(List<LCFriendshipRequest> tmpRequests) {
                            testSucceed = tmpRequests.size() > 0;
                            latch.countDown();
                          }

                          @Override
                          public void onError(Throwable throwable) {
                            System.out.println("failed to query friendship request by user: " + testUser1UserName);
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
                        System.out.println("failed to accept friendship request by user: " + testUser1UserName);
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
                    System.out.println("failed to login with user: " + testUser1UserName);
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
                System.out.println("failed to query pending Friendship as anonymous user");
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
            System.out.println("failed to apply new Friendship as anonymous user, cause: " + throwable.getMessage());
            if (throwable.getMessage().indexOf("Friendship already exists") >= 0) {
              testSucceed = true;
            } else if (throwable.getMessage().indexOf("Friendship request had previously been declined") >= 0) {
              testSucceed = true;
            } else {
              throwable.printStackTrace();
            }
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to login as anonymous user");
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
}
