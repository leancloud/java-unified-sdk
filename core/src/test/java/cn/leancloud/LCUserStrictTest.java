package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import cn.leancloud.json.JSONObject;
import cn.leancloud.query.QueryConditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LCUserStrictTest extends TestCase {
  private boolean operationSucceed = false;
  private String userId;
 
  public LCUserStrictTest(String name) {
    super(name);
    Configure.initializeWithApp("0RiAlMny7jiz086FaU","8V8wemqkpkxmAN7qKhvlh6v0pXc8JJzEZe3JFUnU","https://0rialmny.cloud.tds1.tapapis.cn");
  }

  public static Test suite() {
    return new TestSuite(LCUserStrictTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    operationSucceed = false;
  }

  public LCUser tomLogin() {
      Map<String, Object> authData = new HashMap<>();
      authData.put("access_token", "1/qUNqv_-kC-pgpqGVUi7RRXmNSzmdY6KI9WxCKs_2UU0ItLQWQdXte0h346iHzCGCEdVpeTWw5fUAv_90oYcrPGBs7J57GpK-gnV8XrwK9dkUXBAWyMYGA_HiDIaueNYnwPy3SbMpoKMtAGNuy728lL0Y46HVDXpucOy_bvGWZY7MB9eOmc2xNsWlt7cLGWInKmaUZYzxLxRjKhjt6d3z8_WA8UgFlhxNLbE04JViMLoVV20Lq_cgn0at0fZpioYYRJnCAIaod3smv9lShUuKqzrhOFl4TpxKnIoolg8OfDqmVSBFXAeWy4UIwH3ne48NWfaowipVmOz0Si8r7iYgqw");
      authData.put("kid", "1/qUNqv_-kC-pgpqGVUi7RRXmNSzmdY6KI9WxCKs_2UU0ItLQWQdXte0h346iHzCGCEdVpeTWw5fUAv_90oYcrPGBs7J57GpK-gnV8XrwK9dkUXBAWyMYGA_HiDIaueNYnwPy3SbMpoKMtAGNuy728lL0Y46HVDXpucOy_bvGWZY7MB9eOmc2xNsWlt7cLGWInKmaUZYzxLxRjKhjt6d3z8_WA8UgFlhxNLbE04JViMLoVV20Lq_cgn0at0fZpioYYRJnCAIaod3smv9lShUuKqzrhOFl4TpxKnIoolg8OfDqmVSBFXAeWy4UIwH3ne48NWfaowipVmOz0Si8r7iYgqw");
      authData.put("mac_key", "TN7goEtwnG9uyM4cyiClZpQ7mS8VUOXUxgDQfX79");
      authData.put("token_type", "mac");
      authData.put("mac_algorithm", "hmac-sha-1");
      authData.put("openid", "e6nTJG482t1wplqqvdjE1Q==");
      authData.put("unionid", "4s3nMd+8TbwaQ0+bMU2E+g==");
      authData.put("name", "SeraphLi");
      authData.put("avatar", "https://img3.tapimg.com/default_avatars/ff3f51dbef23d4c99a40b774b312d652.jpg?imageMogr2/auto-orient/strip/thumbnail/!300x300r/gravity/Center/crop/300x300/format/jpg/interlace/1/quality/80");
      LCUser user = LCUser.loginWithAuthData(LCUser.class, authData, "taptap").blockingFirst();
      userId = user.getObjectId();
      //tdsid = 61285b3246b6dc0d3f807c09
      return user;
  }

  public LCUser jerryLogin() {
      Map<String, Object> authData = new HashMap<>();
      authData.put("access_token", "1/IENOJL2gf25vVNCkAMRQAwaCcTOL6zdR2PQaK5NSQcjJs4XNgVPk1qKnktDfLgzq_XGSofdvt6Jy37Q3n82egVFzYxIbIgDZ9loxbJqWMtE29jey6beiYkdPRHkkaBsCm6ks7X0afE8CXLRI58p6GCzSdFhVbh7YzmqlvIlm202h90Gtiln3J0_sMzg23TOdgYbYDbSnmwzXkQmWzRsKS2b4yIytAxXLCD0ECLz_fDKWT8qw02x2r3VTq-SKo9sUyNK1PFujN9PlueSkf_wBjdYJV5Bqgxuo9aAq8Nsy3hPzZckmfnNtk1kEO-T5dKEQ82MD_foCkmVzVr9YwE_D6w");
      authData.put("kid", "1/IENOJL2gf25vVNCkAMRQAwaCcTOL6zdR2PQaK5NSQcjJs4XNgVPk1qKnktDfLgzq_XGSofdvt6Jy37Q3n82egVFzYxIbIgDZ9loxbJqWMtE29jey6beiYkdPRHkkaBsCm6ks7X0afE8CXLRI58p6GCzSdFhVbh7YzmqlvIlm202h90Gtiln3J0_sMzg23TOdgYbYDbSnmwzXkQmWzRsKS2b4yIytAxXLCD0ECLz_fDKWT8qw02x2r3VTq-SKo9sUyNK1PFujN9PlueSkf_wBjdYJV5Bqgxuo9aAq8Nsy3hPzZckmfnNtk1kEO-T5dKEQ82MD_foCkmVzVr9YwE_D6w");
      authData.put("mac_key", "z7a5pES9Ek6Ey9tZSG9dvr10iJF6PrUhqhwERuO6");
      authData.put("token_type", "mac");
      authData.put("mac_algorithm", "hmac-sha-1");
      authData.put("openid", "qN3BqueVyVWHH1V2ct1FCA==");
      authData.put("unionid", "dhcO3X62+BT8yKwZnePUxw==");
      authData.put("name", "测试用");
      authData.put("avatar", "https://img3.tapimg.com/default_avatars/4d6191ea1d18dc676f5ecfcef914f382.jpg?imageMogr2/auto-orient/strip/thumbnail/!300x300r/gravity/Center/crop/300x300/format/jpg/interlace/1/quality/80");
      LCUser user = LCUser.loginWithAuthData(LCUser.class, authData, "taptap").blockingFirst();
      userId = user.getObjectId();
      // tdsId = bda2074b7ac9418ca5fe772bfb949f38
      return user;
  }
  @Override
  protected void tearDown() throws Exception {
    ;
  }

  public void testStrictlyQueryUsers() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logInAnonymously().subscribe(new Observer<LCUser>() {
        @Override
        public void onSubscribe(Disposable disposable) {

        }

        @Override
        public void onNext(LCUser avUser) {
            System.out.println("onNext. result=" + avUser.toString());
            avUser.put("nickname", "seraph");
            avUser.saveInBackground().subscribe(new Observer<LCObject>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(LCObject lcObject) {
                    System.out.println(" nickname = " + lcObject.get("nickname"));
                    QueryConditions queryConditions = new QueryConditions();
                    queryConditions.whereEqualTo("nickname", "seraph");
                    LCUser.strictlyFind(queryConditions).subscribe(new Observer<List<LCUser>>() {
                        @Override
                        public void onSubscribe(Disposable disposable) {

                        }

                        @Override
                        public void onNext(List<LCUser> users) {
                            System.out.println("Succeed to strictlyQuery users size = " + users.size());
                            operationSucceed = true;
                            latch.countDown();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            System.out.println("Failed to strictlyQuery users");
                            latch.countDown();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
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

    public void testStrictlyQueryWithSkipUsers() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        LCUser.logInAnonymously().subscribe(new Observer<LCUser>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCUser avUser) {
                System.out.println("onNext. result=" + avUser.toString());
                avUser.put("nickname", "seraph");
                avUser.saveInBackground().subscribe(new Observer<LCObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(LCObject lcObject) {
                        System.out.println(" nickname = " + lcObject.get("nickname"));
                        QueryConditions queryConditions = new QueryConditions();
                        queryConditions.whereStartsWith("nickname", "seraph");
                        queryConditions.setSkip(100);
                        LCUser.strictlyFind(queryConditions).subscribe(new Observer<List<LCUser>>() {
                            @Override
                            public void onSubscribe(Disposable disposable) {

                            }

                            @Override
                            public void onNext(List<LCUser> users) {
                                System.out.println("Succeed to strictlyQuery users size = " + users.size());
                                latch.countDown();
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                System.out.println("Failed to strictlyQuery users");
                                operationSucceed = true;
                                latch.countDown();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
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

    public void testStrictlyQueryWithSessionTokenUsers() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        LCUser.logInAnonymously().subscribe(new Observer<LCUser>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCUser avUser) {
                System.out.println("onNext. result=" + avUser.toString());
                avUser.put("nickname", "seraph");
                avUser.saveInBackground().subscribe(new Observer<LCObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(LCObject lcObject) {
                        System.out.println(" nickname = " + lcObject.get("nickname"));
                        QueryConditions queryConditions = new QueryConditions();
                        queryConditions.whereExists("nickname");
                        queryConditions.whereExists("sessionToken");
                        LCUser.strictlyFind(queryConditions).subscribe(new Observer<List<LCUser>>() {
                            @Override
                            public void onSubscribe(Disposable disposable) {

                            }

                            @Override
                            public void onNext(List<LCUser> users) {
                                System.out.println("Succeed to strictlyQuery users size = " + users.size());
                                latch.countDown();
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                System.out.println("Failed to strictlyQuery users");
                                operationSucceed = true;
                                latch.countDown();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
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
    
    public void testBlock() {
    	LCUser tom = tomLogin();
    	final String tomObjectId = tom.getObjectId();
    	 final CountDownLatch latch = new CountDownLatch(1);
    	jerryLogin();
    	 LCUser.getCurrentUser().blockFriendInBackground(tomObjectId)
    	 .subscribe(new Consumer<JSONObject>() {
             @Override
             public void accept(JSONObject t) throws Exception {
            	 System.out.println(" block tom succeed");
            	 LCUser.getCurrentUser().friendshipBlockQuery(LCBlockRelation.class)
                 .skip(0)
                 .limit(10)
                 .findInBackground()
                 .subscribe(new Consumer<List<LCBlockRelation>>() {

                     @Override
                     public void accept(List<LCBlockRelation> t) throws Exception {
                         System.out.println(" get blick list size = " + t.size());
                         System.out.println(" data = " + t);
                         System.out.println(" first user id = " + t.get(0).getUser().getObjectId());
                        LCUser.getCurrentUser().unblockFriendInBackground(tomObjectId)
                        .subscribe(new Consumer<JSONObject>(){
                     	   
                     	   @Override
                            public void accept(JSONObject t) throws Exception {
                                System.out.println(" unblock tom success");
                                operationSucceed = true;
                                latch.countDown();
                     	   }
                          }, new Consumer<Throwable>(){

                            @Override
                            public void accept(Throwable t) throws Exception {
                                t.printStackTrace();
                                latch.countDown();
                            }
                        });
                     
             }
    	 },new Consumer<Throwable>(){

             @Override
             public void accept(Throwable t) throws Exception {
                 t.printStackTrace();
                 latch.countDown();
             }

         });
   
}
             },new Consumer<Throwable>(){

    @Override
    public void accept(Throwable t) throws Exception {
        t.printStackTrace();
        latch.countDown();
    }

});
    }
}
