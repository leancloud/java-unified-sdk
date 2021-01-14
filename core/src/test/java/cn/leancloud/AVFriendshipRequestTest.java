package cn.leancloud;

import cn.leancloud.json.JSON;
import cn.leancloud.types.AVNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

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
    AVUser.logOut();
  }

  @Override
  protected void tearDown() throws Exception {
    ;
  }

  public void testStatusSerializer() throws Exception {
    System.out.println(AVFriendshipRequest.RequestStatus.Accepted.name());
    System.out.println(AVFriendshipRequest.RequestStatus.Accepted.name().toLowerCase());
    AVFriendshipRequest request = AVFriendshipRequest.createWithCurrentUser();
    assertTrue(null == request);
  }

  public void testSimpleRequestWithLoginedUser() throws Exception {
    AVUser.logIn(testUser1UserName, testUser1Password).subscribe(
            new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {
      }

      public void onNext(AVUser avUser) {
        AVUser currentUser = AVUser.getCurrentUser();
        System.out.println("currentUser. result=" + JSON.toJSONString(currentUser));
        System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

        AVUser friend = null;
        try {
          friend = AVUser.createWithoutData(AVUser.class, "5f5048abd67d4e29e52d21c0");
        } catch (AVException e) {
          e.printStackTrace();
        }
        avUser.applyFriendshipInBackground(friend, null)
                .subscribe(new Observer<AVFriendshipRequest>() {
                  @Override
                  public void onSubscribe(@NotNull Disposable disposable) {

                  }

                  @Override
                  public void onNext(@NotNull final AVFriendshipRequest friendshipRequest) {
                    System.out.println("succeed to create new friend request. result=" + JSON.toJSONString(friendshipRequest));
                    System.out.println("objectId=" + friendshipRequest.getObjectId());
                    AVUser.becomeWithSessionToken("52g2hsrptizbuyygafbhav4p3");
                    friendshipRequest.accept().subscribe(new Observer<AVObject>() {
                      @Override
                      public void onSubscribe(@NotNull Disposable disposable) {

                      }

                      @Override
                      public void onNext(@NotNull AVObject avObject) {
                        System.out.println("succeed to accept new friend request. result=" + avObject);
                        friendshipRequest.deleteInBackground().subscribe(new Observer<AVNull>() {
                          @Override
                          public void onSubscribe(@NotNull Disposable disposable) {

                          }

                          @Override
                          public void onNext(@NotNull AVNull avNull) {
                            System.out.println("succeed to delete new friend request.");
                            testSucceed = true;
                            latch.countDown();
                          }

                          @Override
                          public void onError(@NotNull Throwable throwable) {
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
                      public void onError(@NotNull Throwable throwable) {
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
                  public void onError(@NotNull Throwable throwable) {
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
    AVUser.logInAnonymously().subscribe(
            new Observer<AVUser>() {
              public void onSubscribe(Disposable disposable) {
              }

              public void onNext(AVUser avUser) {
                AVUser currentUser = AVUser.getCurrentUser();
                System.out.println("currentUser. result=" + JSON.toJSONString(currentUser));
                System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

                AVUser friend = null;
                try {
                  friend = AVUser.createWithoutData(AVUser.class, "5f5048abd67d4e29e52d21c0");
                } catch (AVException e) {
                  e.printStackTrace();
                }
                avUser.applyFriendshipInBackground(friend, null)
                        .subscribe(new Observer<AVFriendshipRequest>() {
                          @Override
                          public void onSubscribe(@NotNull Disposable disposable) {

                          }

                          @Override
                          public void onNext(@NotNull final AVFriendshipRequest friendshipRequest) {
                            System.out.println("succeed to create new friend request. result=" + JSON.toJSONString(friendshipRequest));
                            System.out.println("objectId=" + friendshipRequest.getObjectId());
                            AVUser.becomeWithSessionToken("52g2hsrptizbuyygafbhav4p3");
                            friendshipRequest.decline().subscribe(new Observer<AVObject>() {
                              @Override
                              public void onSubscribe(@NotNull Disposable disposable) {

                              }

                              @Override
                              public void onNext(@NotNull AVObject avObject) {
                                System.out.println("succeed to decline new friend request. result=" + avObject);
                                try {
                                  System.out.println("sleep 2000 ms...");
                                  Thread.sleep(2000);
                                  System.out.println("try to accept friend request again...");
                                } catch (Exception ex) {
                                  ex.printStackTrace();
                                }
                                friendshipRequest.accept().subscribe(new Observer<AVObject>() {
                                  @Override
                                  public void onSubscribe(@NotNull Disposable disposable) {

                                  }

                                  @Override
                                  public void onNext(@NotNull AVObject avObject) {
                                    System.out.println("succeed to accept the declined friend request.");
                                    friendshipRequest.deleteInBackground().subscribe(new Observer<AVNull>() {
                                      @Override
                                      public void onSubscribe(@NotNull Disposable disposable) {

                                      }

                                      @Override
                                      public void onNext(@NotNull AVNull avNull) {
                                        System.out.println("succeed to delete new friend request.");
                                        testSucceed = true;
                                        latch.countDown();
                                      }

                                      @Override
                                      public void onError(@NotNull Throwable throwable) {
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
                                  public void onError(@NotNull Throwable throwable) {
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
                              public void onError(@NotNull Throwable throwable) {
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
                          public void onError(@NotNull Throwable throwable) {
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

  public void testSimpleRequestWithAnonymousUserAccept() throws Exception {
    AVUser.logInAnonymously().subscribe(new Observer<AVUser>() {
      @Override
      public void onSubscribe(@NotNull Disposable disposable) {

      }

      @Override
      public void onNext(@NotNull final AVUser anonymousUser) {
        final AVUser target;
        try {
          target = AVUser.createWithoutData(AVUser.class, testUser1ObjectId);
        }catch (Exception ex) {
          latch.countDown();
          return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("group", "collage");
        anonymousUser.applyFriendshipInBackground(target, param).subscribe(new Observer<AVFriendshipRequest>() {
          @Override
          public void onSubscribe(@NotNull Disposable disposable) {

          }

          @Override
          public void onNext(@NotNull AVFriendshipRequest avFriendshipRequest) {
            System.out.println("try to query all request from current User");
            AVQuery<AVFriendshipRequest> query = anonymousUser.friendshipRequestQuery(AVFriendshipRequest.STATUS_PENDING,
                    true, false);
            query.findInBackground().subscribe(new Observer<List<AVFriendshipRequest>>() {
              @Override
              public void onSubscribe(@NotNull Disposable disposable) {

              }

              @Override
              public void onNext(@NotNull List<AVFriendshipRequest> avFriendshipRequests) {
                System.out.println("succeed to query pending request from anonymous user. resultSize=" + avFriendshipRequests.size());
                if (avFriendshipRequests.size() < 1) {
                  latch.countDown();
                  return;
                }
                final AVFriendshipRequest targetFriendshipRequest = avFriendshipRequests.get(0);
                AVUser.logIn(testUser1UserName, testUser1Password).subscribe(new Observer<AVUser>() {
                  @Override
                  public void onSubscribe(@NotNull Disposable disposable) {

                  }

                  @Override
                  public void onNext(@NotNull final AVUser secondUser) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("group", "fans");
                    secondUser.acceptFriendshipRequest(targetFriendshipRequest, param).subscribe(new Observer<AVFriendshipRequest>() {
                      @Override
                      public void onSubscribe(@NotNull Disposable disposable) {

                      }

                      @Override
                      public void onNext(@NotNull AVFriendshipRequest avFriendshipRequest) {
                        AVQuery<AVFriendshipRequest> query = secondUser.friendshipRequestQuery(AVFriendshipRequest.STATUS_ACCEPTED, true, true);
                        query.findInBackground().subscribe(new Observer<List<AVFriendshipRequest>>() {
                          @Override
                          public void onSubscribe(@NotNull Disposable disposable) {

                          }

                          @Override
                          public void onNext(@NotNull List<AVFriendshipRequest> tmpRequests) {
                            AVQuery<AVFriendship> query = secondUser.friendshipQuery(false);
                            query.whereEqualTo(AVFriendship.ATTR_FRIEND_STATUS, true);
                            query.addDescendingOrder(AVObject.KEY_UPDATED_AT);
                            List<AVFriendship> followees = query.find();
                            if (followees == null || followees.size() < 1) {
                              latch.countDown();
                              return;
                            }
                            try {
                              AVFriendship friendship = followees.get(0);
                              friendship.put("remark", "丐帮帮主");
                              secondUser.updateFriendship(friendship).subscribe(new Observer<AVObject>() {
                                @Override
                                public void onSubscribe(@NotNull Disposable disposable) {

                                }

                                @Override
                                public void onNext(@NotNull AVObject avObject) {
                                  System.out.println("succeed to update friendship: " + avObject);
                                  testSucceed = true;
                                  latch.countDown();
                                }

                                @Override
                                public void onError(@NotNull Throwable throwable) {
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
                          public void onError(@NotNull Throwable throwable) {
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
                      public void onError(@NotNull Throwable throwable) {
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
                  public void onError(@NotNull Throwable throwable) {
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
              public void onError(@NotNull Throwable throwable) {
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
          public void onError(@NotNull Throwable throwable) {
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
      public void onError(@NotNull Throwable throwable) {
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
    AVUser.logInAnonymously().subscribe(new Observer<AVUser>() {
      @Override
      public void onSubscribe(@NotNull Disposable disposable) {

      }

      @Override
      public void onNext(@NotNull final AVUser anonymousUser) {
        final AVUser target;
        try {
          target = AVUser.createWithoutData(AVUser.class, testUser1ObjectId);
        }catch (Exception ex) {
          latch.countDown();
          return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("group", "collage");
        anonymousUser.applyFriendshipInBackground(target, param).subscribe(new Observer<AVFriendshipRequest>() {
          @Override
          public void onSubscribe(@NotNull Disposable disposable) {

          }

          @Override
          public void onNext(@NotNull AVFriendshipRequest avFriendshipRequest) {
            System.out.println("try to query all request from current User");
            AVQuery<AVFriendshipRequest> query = anonymousUser.friendshipRequestQuery(AVFriendshipRequest.STATUS_PENDING,
                    false, false);
            query.findInBackground().subscribe(new Observer<List<AVFriendshipRequest>>() {
              @Override
              public void onSubscribe(@NotNull Disposable disposable) {

              }

              @Override
              public void onNext(@NotNull List<AVFriendshipRequest> avFriendshipRequests) {
                System.out.println("succeed to query pending request from anonymous user. resultSize=" + avFriendshipRequests.size());
                if (avFriendshipRequests.size() < 1) {
                  latch.countDown();
                  return;
                }
                final AVFriendshipRequest targetFriendshipRequest = avFriendshipRequests.get(0);
                AVUser.logIn(testUser1UserName, testUser1Password).subscribe(new Observer<AVUser>() {
                  @Override
                  public void onSubscribe(@NotNull Disposable disposable) {

                  }

                  @Override
                  public void onNext(@NotNull final AVUser secondUser) {
                    secondUser.declineFriendshipRequest(targetFriendshipRequest).subscribe(new Observer<AVFriendshipRequest>() {
                      @Override
                      public void onSubscribe(@NotNull Disposable disposable) {

                      }

                      @Override
                      public void onNext(@NotNull AVFriendshipRequest avFriendshipRequest) {
                        AVQuery<AVFriendshipRequest> query =
                                secondUser.friendshipRequestQuery(AVFriendshipRequest.STATUS_DECLINED, false, true);
                        query.findInBackground().subscribe(new Observer<List<AVFriendshipRequest>>() {
                          @Override
                          public void onSubscribe(@NotNull Disposable disposable) {

                          }

                          @Override
                          public void onNext(@NotNull List<AVFriendshipRequest> tmpRequests) {
                            testSucceed = tmpRequests.size() > 0;
                            latch.countDown();
                          }

                          @Override
                          public void onError(@NotNull Throwable throwable) {
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
                      public void onError(@NotNull Throwable throwable) {
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
                  public void onError(@NotNull Throwable throwable) {
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
              public void onError(@NotNull Throwable throwable) {
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
          public void onError(@NotNull Throwable throwable) {
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
      public void onError(@NotNull Throwable throwable) {
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
