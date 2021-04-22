package cn.leancloud;

import cn.leancloud.json.JSON;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class AVFriendshipRequestTest extends TestCase {
  private boolean testSucceed = false;
  private CountDownLatch latch = null;

  private String testUser1ObjectId = "5eeacc973ec3ec0008e6023c";
  private String testUser1UserName = AVUserTest.USERNAME;
  private String testUser1Password = AVUserTest.PASSWORD;

  public AVFriendshipRequestTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AVFriendshipRequestTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    testSucceed = false;
    latch = new CountDownLatch(1);
    LCUser.logOut();
  }

  @Override
  protected void tearDown() throws Exception {
    ;
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
        System.out.println("currentUser. result=" + JSON.toJSONString(currentUser));
        System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

        LCUser friend = null;
        try {
          friend = LCUser.createWithoutData(LCUser.class, "5f5048abd67d4e29e52d21c0");
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
                    System.out.println("succeed to create new friend request. result=" + JSON.toJSONString(friendshipRequest));
                    System.out.println("objectId=" + friendshipRequest.getObjectId());
                    LCUser.becomeWithSessionToken("52g2hsrptizbuyygafbhav4p3");
                    friendshipRequest.accept(null).subscribe(new Observer<LCObject>() {
                      @Override
                      public void onSubscribe(Disposable disposable) {

                      }

                      @Override
                      public void onNext(LCObject LCObject) {
                        System.out.println("succeed to accept new friend request. result=" + LCObject);
                        friendshipRequest.deleteInBackground().subscribe(new Observer<LCNull>() {
                          @Override
                          public void onSubscribe(Disposable disposable) {

                          }

                          @Override
                          public void onNext(LCNull LCNull) {
                            System.out.println("succeed to delete new friend request.");
                            testSucceed = true;
                            latch.countDown();
                          }

                          @Override
                          public void onError(Throwable throwable) {
                            System.out.println("failed to delete new friend request.");
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
                        System.out.println("failed to accept new friend request. result=");
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
                    System.out.println("failed to create new friend request. result=" + throwable.getMessage());

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
                System.out.println("currentUser. result=" + JSON.toJSONString(currentUser));
                System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

                LCUser friend = null;
                try {
                  friend = LCUser.createWithoutData(LCUser.class, "5f5048abd67d4e29e52d21c0");
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
                            System.out.println("succeed to create new friend request. result=" + JSON.toJSONString(friendshipRequest));
                            System.out.println("objectId=" + friendshipRequest.getObjectId());
                            LCUser.becomeWithSessionToken("52g2hsrptizbuyygafbhav4p3", true);
                            friendshipRequest.decline().subscribe(new Observer<LCObject>() {
                              @Override
                              public void onSubscribe(Disposable disposable) {

                              }

                              @Override
                              public void onNext(LCObject LCObject) {
                                System.out.println("succeed to decline new friend request. result=" + LCObject);
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
                                    System.out.println("succeed to accept the declined friend request.");
                                    friendshipRequest.deleteInBackground().subscribe(new Observer<LCNull>() {
                                      @Override
                                      public void onSubscribe(Disposable disposable) {

                                      }

                                      @Override
                                      public void onNext(LCNull LCNull) {
                                        System.out.println("succeed to delete new friend request.");
                                        testSucceed = true;
                                        latch.countDown();
                                      }

                                      @Override
                                      public void onError(Throwable throwable) {
                                        System.out.println("failed to delete new friend request.");
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
                                    System.out.println("failed to accept the declined friend request.");
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
                                System.out.println("failed to accept new friend request. result=");
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
                            System.out.println("failed to create new friend request. result=");
                            throwable.printStackTrace();
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
                  friend = LCUser.createWithoutData(LCUser.class, "5dd7892143c2570074c96ca9");
                } catch (LCException e) {
                  e.printStackTrace();
                }
                avUser.applyFriendshipInBackground(friend, null).subscribe(new Observer<LCFriendshipRequest>() {
                  @Override
                  public void onSubscribe(Disposable disposable) {

                  }

                  @Override
                  public void onNext(LCFriendshipRequest avFriendshipRequest) {
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

  public void testSimpleRequestWithAnonymousUserAccept() throws Exception {
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
        anonymousUser.applyFriendshipInBackground(target, param).subscribe(new Observer<LCFriendshipRequest>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCFriendshipRequest avFriendshipRequest) {
            System.out.println("try to query all request from current User");
            LCQuery<LCFriendshipRequest> query = anonymousUser.friendshipRequestQuery(LCFriendshipRequest.STATUS_PENDING,
                    true, false);
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
                    Map<String, Object> param = new HashMap<>();
                    param.put("group", "fans");
                    secondUser.acceptFriendshipRequest(targetFriendshipRequest, param).subscribe(new Observer<LCFriendshipRequest>() {
                      @Override
                      public void onSubscribe(Disposable disposable) {

                      }

                      @Override
                      public void onNext(LCFriendshipRequest avFriendshipRequest) {
                        LCQuery<LCFriendshipRequest> query = secondUser.friendshipRequestQuery(LCFriendshipRequest.STATUS_ACCEPTED, true, true);
                        query.findInBackground().subscribe(new Observer<List<LCFriendshipRequest>>() {
                          @Override
                          public void onSubscribe(Disposable disposable) {

                          }

                          @Override
                          public void onNext(List<LCFriendshipRequest> tmpRequests) {
                            LCQuery<LCFriendship> query = secondUser.friendshipQuery(false);
                            query.whereEqualTo(LCFriendship.ATTR_FRIEND_STATUS, true);
                            query.addDescendingOrder(LCObject.KEY_UPDATED_AT);
                            List<LCFriendship> followees = query.find();
                            if (followees == null || followees.size() < 1) {
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
                                  System.out.println("succeed to update friendship: " + LCObject);
                                  testSucceed = true;
                                  latch.countDown();
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                  System.out.println("failed to update friendship.");
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
            System.out.println("failed to apply new Friendship as anonymous user");
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
            System.out.println("failed to apply new Friendship as anonymous user");
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
