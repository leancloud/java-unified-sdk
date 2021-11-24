package cn.leancloud;

import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import cn.leancloud.query.QueryConditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LCUserStrictTest extends TestCase {
  private boolean operationSucceed = false;
 
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
                            throwable.printStackTrace();
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
                                throwable.printStackTrace();
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
                                throwable.printStackTrace();
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
}
