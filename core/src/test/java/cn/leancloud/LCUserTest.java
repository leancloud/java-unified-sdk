package cn.leancloud;

import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import cn.leancloud.types.LCNull;
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

public class LCUserTest extends TestCase {
  private boolean operationSucceed = false;
  public static final String USERNAME = "jfeng";
  public static final String PASSWORD = "FER$@$@#Ffwe";
  public static final String EMAIL = "jfeng20200618@test.com";
  public LCUserTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(LCUserTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    operationSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    ;
  }

  public void testBunchAnonymousLogin() throws Exception {
    LCUser user1 = LCUser.logInAnonymously().blockingFirst();
    String anonymousUserId = user1.objectId;
    user1.logOut();
    for (int i = 0; i < 10; i++) {
      user1 = LCUser.logInAnonymously().blockingFirst();
      assertEquals(anonymousUserId, user1.objectId);
      user1.logOut();
      Thread.sleep(200);
    }
  }

  public void testSingupWithEmail() throws Exception {
    LCUser user = new LCUser();
    user.setEmail(EMAIL);
    user.setUsername(USERNAME);
    user.setPassword(PASSWORD);
    final CountDownLatch latch = new CountDownLatch(1);
    user.signUpInBackground().subscribe(new Observer<LCUser>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCUser avUser) {
        System.out.println(JSON.toJSONString(avUser));
        operationSucceed = true;
        latch.countDown();

      }

      public void onError(Throwable throwable) {
        operationSucceed = true;
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testLogin() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logIn(USERNAME, PASSWORD).subscribe(new Observer<LCUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(LCUser avUser) {
        System.out.println("onNext. result=" + JSON.toJSONString(avUser));

        LCUser currentUser = LCUser.getCurrentUser();
        System.out.println("currentUser. result=" + JSON.toJSONString(currentUser));
        System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

        operationSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testSignupWithAuthDataAndFailFlag() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    Map<String, Object> authData = new HashMap<String, Object>();
    authData.put("expires_in", 7200);
    authData.put("openid", "1234567890");
    authData.put("access_token", "ACCESS_TOKEN1");
    authData.put("refresh_token", "REFRESH_TOKEN2");
    authData.put("scope", "SCOPE");
    LCUser user = new LCUser();
    user.loginWithAuthData(authData,"weixin",true).subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable d) {
      }
      @Override
      public void onNext(LCUser avUser) {
        System.out.println("存在匹配的用户，登录成功");
        latch.countDown();
      }

      @Override
      public void onError(Throwable e) {
        LCException LCException = new LCException(e);
        int code = LCException.getCode();
        if (code == 211){
          // 跳转到输入用户名、密码、手机号等业务页面
        } else {
          System.out.println("发生错误:" + e.getMessage());
        }
        latch.countDown();
      }
      @Override
      public void onComplete() {
      }
    });
    latch.await();
  }

  public void testAnonymousLogin() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logInAnonymously().subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCUser avUser) {
        System.out.println("onNext. result=" + avUser.toString());
        System.out.println("current user is authenticated? " + avUser.isAuthenticated() + ", isAnonymous? " + avUser.isAnonymous());
        String openId = "openid3322dr";
        String accessToken = "access_token";
        String expiresAt = "313830732382";
        final Map<String,Object> userAuth = new HashMap<>();
        userAuth.put("access_token",accessToken);
        userAuth.put("expires_in", expiresAt);
        userAuth.put("openid",openId);

        avUser.put("gender", "male");
        avUser.associateWithAuthData(userAuth, "qq").subscribe(new Observer<LCUser>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCUser tmp) {
            System.out.println("onNext. result=" + tmp.toString());
            System.out.println("current user is authenticated? " + tmp.isAuthenticated() + ", isAnonymous? " + tmp.isAnonymous());

            tmp.associateWithAuthData(userAuth, "weixin_test").subscribe(new Observer<LCUser>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCUser tmp2) {
                System.out.println("onNext. result=" + tmp2.toString());
                System.out.println("current user is authenticated? " + tmp2.isAuthenticated() + ", isAnonymous? " + tmp2.isAnonymous());

                operationSucceed = true;
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                if (throwable.getMessage().indexOf("A unique field was given a value that is already taken") >= 0) {
                  operationSucceed = true;
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
            if (throwable.getMessage().indexOf("A unique field was given a value that is already taken") >= 0) {
              operationSucceed = true;
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
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testDisassociateAnonymousLogin() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logInAnonymously().subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCUser avUser) {
        System.out.println("logInAnonymously onNext. result=" + avUser.toString());
        avUser.dissociateWithAuthData("anonymous").subscribe(new Observer<LCUser>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCUser avUser) {
            System.out.println("dissociateWithAuthData onNext. result=" + avUser.toString());
            operationSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to dissocaite auth data. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
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
    assertTrue(operationSucceed);
  }

  public void testCurrentUser() throws Exception {
    LCUser.disableAutomaticUser();
    LCUser currentUser = LCUser.getCurrentUser();
    assertNotNull(currentUser);
  }

  public void testQueryUser() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCQuery<LCUser> query = new LCQuery<LCUser>(LCUser.CLASS_NAME);
    query.findInBackground().subscribe(new Observer<List<LCUser>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<LCUser> avUsers) {
        operationSucceed = true;
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
    assertTrue(operationSucceed);
  }

  public void testCurrentUserWithNew() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logIn(USERNAME, PASSWORD).subscribe(new Observer<LCUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(LCUser avUser) {
        System.out.println("onNext. result=" + avUser.toString());
        LCUser.changeCurrentUser(avUser, true);
        LCUser u = LCUser.getCurrentUser();
        operationSucceed = avUser.equals(u);
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testCurrentUserWithCached() throws Exception {
    ;
  }

  public void testCurrentUserWithSubclass() throws Exception {
    ;
  }

  public void testCheckAuthenticatedFalse() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser u = new LCUser();
    u.setEmail(EMAIL);
    u.setUsername(USERNAME);
    u.setPassword(PASSWORD);
    u.setObjectId("ferewr2343");
    u.checkAuthenticatedInBackground().subscribe(new Observer<Boolean>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(Boolean aBoolean) {
        operationSucceed = aBoolean;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(!operationSucceed);
  }
  public void testCheckAuthenticatedTrue() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logIn(USERNAME, PASSWORD).subscribe(new Observer<LCUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(LCUser avUser) {
        avUser.checkAuthenticatedInBackground().subscribe(new Observer<Boolean>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(Boolean aBoolean) {
            operationSucceed = aBoolean;
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
    assertTrue(operationSucceed);
  }


  public void testSaveCurrentUserData() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    operationSucceed = false;
    LCUser.logIn(USERNAME, PASSWORD).subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCUser avUser) {
        avUser.put("nickname", "Developer Fong");
        avUser.setFetchWhenSave(true);
        avUser.save();
        String nickName1 = avUser.getString("nickname");
        String nkName2 = LCUser.currentUser().getString("nickname");
        operationSucceed = nickName1.equals(nkName2) && nickName1.equals("Developer Fong");
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
    assertTrue(operationSucceed);
  }

  public void testGetCurrentUserAfterSave() throws Exception {
    testSaveCurrentUserData();
    operationSucceed = false;
    String nkName = LCUser.currentUser().getString("nickname");
    operationSucceed = nkName.equals("Developer Fong");
    assertTrue(operationSucceed);
  }

  public void testFetchThenUpdateCurrentUser() throws Exception {
    operationSucceed = false;
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logIn(USERNAME, PASSWORD).subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(final LCUser avUser) {
        System.out.println("currentUser:" + LCUser.currentUser());
        avUser.fetchInBackground("sessionToken,nickname").subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {
          }

          @Override
          public void onNext(LCObject LCObject) {
            System.out.println("currentUser:" + LCUser.currentUser());
            LCUser.changeCurrentUser(avUser, true);
            System.out.println("currentUser:" + LCUser.currentUser());
            final String newName = String.valueOf(System.currentTimeMillis());
            avUser.put("nickname", newName);
            avUser.setFetchWhenSave(true);
            avUser.saveInBackground().subscribe(new Observer<LCObject>() {
              @Override
              public void onSubscribe(Disposable disposable) {
              }

              @Override
              public void onNext(LCObject finalObject) {
                System.out.println("currentUser:" + LCUser.currentUser());
                operationSucceed = LCUser.currentUser().getString("nickname").equals(newName);
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                System.out.println("failed to save user info. cause: " + throwable.getMessage());
                latch.countDown();
              }

              @Override
              public void onComplete() {
              }
            });
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to fetch user info. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {
          }
        });

      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to login. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testFetchThenUpdateAnotherUserObject() throws Exception {
    operationSucceed = false;
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logIn(USERNAME, PASSWORD).subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(final LCUser avUser) {
        System.out.println("currentUser:" + LCUser.currentUser());
        try {
          final LCUser another = LCObject.createWithoutData(LCUser.class, avUser.getObjectId());
          another.fetchInBackground().subscribe(new Observer<LCObject>() {
            @Override
            public void onSubscribe(Disposable disposable) {
            }

            @Override
            public void onNext(LCObject LCObject) {
              String nickName = LCObject.getString("nickname");
              System.out.println("currentUser:" + LCUser.currentUser());
              String currentUserNickname = LCUser.currentUser().getString("nickname");

              final String newName = String.valueOf(System.currentTimeMillis());
              another.put("nickname", newName);
              another.setFetchWhenSave(true);

              System.out.println("nickname(currentUser)=" + currentUserNickname + ", nickname(server)=" + nickName
                      + ", nickname(new)=" + newName);

              another.saveInBackground().subscribe(new Observer<LCObject>() {
                @Override
                public void onSubscribe(Disposable disposable) {
                }

                @Override
                public void onNext(LCObject finalObject) {
                  System.out.println("currentUser:" + LCUser.currentUser());
                  operationSucceed = !finalObject.getString("nickname").equals(LCUser.currentUser().getString("nickname"));
                  latch.countDown();
                }

                @Override
                public void onError(Throwable throwable) {
                  System.out.println("failed to save user info. cause: " + throwable.getMessage());
                  latch.countDown();
                }

                @Override
                public void onComplete() {
                }
              });
            }

            @Override
            public void onError(Throwable throwable) {
              System.out.println("failed to fetch user info.");
              throwable.printStackTrace();
              latch.countDown();
            }

            @Override
            public void onComplete() {
            }
          });
        } catch (Exception ex) {
          System.out.println("failed to create Empty User Object with objectId. cause: " + ex.getMessage());
          latch.countDown();
        }
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("failed to login. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testAssociateAuthDataWithMultiplePlatforms() throws Exception {
    final Map<String, Object> weixinAuthData = new HashMap<String, Object>();
    weixinAuthData.put("expires_at", "2019-01-07T02:41:13.580Z");
    weixinAuthData.put("openid", "6A83158");
    weixinAuthData.put("access_token", "DCIF");
    weixinAuthData.put("platform", "weixin");

    final Map<String, Object> qqAuthData = new HashMap<String, Object>();
    qqAuthData.put("expires_at", "2019-01-07T02:41:13.580Z");
    qqAuthData.put("openid", "6A83faefewfew158");
    qqAuthData.put("access_token", "DCfafewerEWDWIF");
    qqAuthData.put("platform", "qq");

    final String username = "jfeng2020";
    final String userEmail = "jfeng2020@test.com";
//    AVUser user = new AVUser();
//    user.setEmail(userEmail);
//    user.setUsername(username);
//    user.setPassword("FER$@$@#Ffwe");
//    user.signUp();

    final CountDownLatch latch = new CountDownLatch(1);
    operationSucceed = false;
    LCUser.logIn(username, "FER$@$@#Ffwe").subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(@NotNull Disposable disposable) {

      }

      @Override
      public void onNext(@NotNull LCUser avUser) {
        System.out.println("succeed to login with username/password. currentUser: " + avUser.toJSONString());

        avUser.associateWithAuthData(weixinAuthData, "weixin").subscribe(new Observer<LCUser>() {
          @Override
          public void onSubscribe(@NotNull Disposable disposable) {

          }

          @Override
          public void onNext(@NotNull LCUser abUser) {
            System.out.println("succeed to associate with weixin data. currentUser: " + abUser.toJSONString());
            abUser.associateWithAuthData(qqAuthData, "qq").subscribe(new Observer<LCUser>() {
              @Override
              public void onSubscribe(@NotNull Disposable disposable) {

              }

              @Override
              public void onNext(@NotNull LCUser acUser) {
                System.out.println("succeed to associate with qq data. currentUser: " + acUser.toJSONString());
                LCUser.currentUser().logOut();
                operationSucceed = true;
                latch.countDown();
              }

              @Override
              public void onError(@NotNull Throwable throwable) {
                System.out.println("failed to associate qq data, cause: " + throwable.getMessage());
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });
          }

          @Override
          public void onError(@NotNull Throwable throwable) {
            System.out.println("failed to associate weixin data, cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(@NotNull Throwable throwable) {
        System.out.println("failed to login with username/passwd, cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testDissociateAuthData() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    operationSucceed = false;

    final Map<String, Object> qqAuthData = new HashMap<String, Object>();
    qqAuthData.put("expires_at", "2019-01-07T02:41:13.580Z");
    qqAuthData.put("openid", "6A83faefewfew158");
    qqAuthData.put("access_token", "DCfafewerEWDWIF");
    qqAuthData.put("platform", "qq");

    LCUser.loginWithAuthData(qqAuthData, "qq").subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(@NotNull Disposable disposable) {

      }

      @Override
      public void onNext(@NotNull LCUser avUser) {
        System.out.println("succeed to login with QQ data. currentUser: " + avUser.toJSONString());
        JSONObject auth = avUser.getJSONObject("authData");
        System.out.println("authData: " + auth.toJSONString());
        avUser.dissociateWithAuthData("qq").subscribe(new Observer<LCUser>() {
          @Override
          public void onSubscribe(@NotNull Disposable disposable) {

          }

          @Override
          public void onNext(@NotNull LCUser axUser) {
            System.out.println("succeed to dissociate with QQ data. currentUser: " + axUser.toJSONString());

            JSONObject auth = axUser.getJSONObject("authData");
            System.out.println("authData: " + auth.toJSONString());

            operationSucceed = true;
            axUser.associateWithAuthData(qqAuthData, "qq").subscribe(new Observer<LCUser>() {
              @Override
              public void onSubscribe(@NotNull Disposable disposable) {

              }

              @Override
              public void onNext(@NotNull LCUser avUser) {
                latch.countDown();
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

          @Override
          public void onError(@NotNull Throwable throwable) {
            System.out.println("failed to dissociate with qq data, cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(@NotNull Throwable throwable) {
        System.out.println("failed to login with qq data, cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testAssociateAuthDataTwice() throws Exception {
    LCUser user = new LCUser();
    user.setEmail("jfeng987@test.com");
    user.setUsername("jfeng987");
    user.setPassword("FER$@$@#Ffwe");

    String openId = "openid";
    String accessToken = "access_token";
    String expiresAt = "313830732382";
    final Map<String,Object> userAuth = new HashMap<>();
    userAuth.put("access_token",accessToken);
    userAuth.put("expires_in", expiresAt);
    userAuth.put("openid",openId);
    LCUser tmp = user.associateWithAuthData(userAuth, "qq").blockingSingle();
    assertTrue(tmp != null);

    final CountDownLatch latch = new CountDownLatch(1);
    operationSucceed = false;

    LCUser.logIn(USERNAME, PASSWORD).subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(final LCUser two) {
        two.associateWithAuthData(userAuth, "qq").subscribe(new Observer<LCUser>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCUser avUser) {
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            two.abortOperations();
            JSONObject authData = two.getJSONObject("authData");
            System.out.println("authData in target User: " + authData);
            operationSucceed = (authData == null || authData.size() < 1);
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
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
    assertTrue(operationSucceed);
  }

  public void testRequestSMSCodeForUpdatingPhoneNumberWithoutLogin() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.requestSMSCodeForUpdatingPhoneNumberInBackground("18600345188", null).subscribe(
            new Observer<LCNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCNull LCNull) {
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                System.out.println("failed to requestSMSCodeForUpdatingPhoneNumber.");
                throwable.printStackTrace();
                operationSucceed = true;
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            }
    );
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testRequestSMSCodeForUpdatingPhoneNumber() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logIn(USERNAME, PASSWORD).subscribe(new Observer<LCUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(LCUser avUser) {
        System.out.println("onNext. result=" + JSON.toJSONString(avUser));

        LCUser currentUser = LCUser.getCurrentUser();
        System.out.println("currentUser. result=" + JSON.toJSONString(currentUser));
        System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

        LCUser.requestSMSCodeForUpdatingPhoneNumberInBackground("18600345188", null).subscribe(
                new Observer<LCNull>() {
                  @Override
                  public void onSubscribe(Disposable disposable) {

                  }

                  @Override
                  public void onNext(LCNull LCNull) {
                    System.out.println("Succeed to requestSMSCodeForUpdatingPhoneNumber");
                    operationSucceed = true;
                    latch.countDown();
                  }

                  @Override
                  public void onError(Throwable throwable) {
                    System.out.println("Failed to requestSMSCodeForUpdatingPhoneNumber");
                    throwable.printStackTrace();
                    latch.countDown();
                  }

                  @Override
                  public void onComplete() {
                  }
                }
        );
      }

      public void onError(Throwable throwable) {
        System.out.println("Failed to login.");
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    latch.await();
    LCUser.logOut();
    assertTrue(operationSucceed);
  }

  public void testVerifySMSCodeForUpdatingPhoneNumber() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logIn(USERNAME, PASSWORD).subscribe(new Observer<LCUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(LCUser avUser) {
        System.out.println("onNext. result=" + JSON.toJSONString(avUser));

        LCUser currentUser = LCUser.getCurrentUser();
        System.out.println("currentUser. result=" + JSON.toJSONString(currentUser));
        System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

        LCUser.verifySMSCodeForUpdatingPhoneNumberInBackground("135966", "18600345188").subscribe(
                new Observer<LCNull>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCNull LCNull) {
            System.out.println("Succeed to verifySMSCodeForUpdatingPhoneNumber");
            operationSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("Failed to verifySMSCodeForUpdatingPhoneNumber");
            throwable.printStackTrace();
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      public void onError(Throwable throwable) {
        System.out.println("Failed to login.");
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    latch.await();
    LCUser.logOut();
    assertTrue(operationSucceed);
  }

}
