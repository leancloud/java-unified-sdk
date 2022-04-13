package cn.leancloud;

import cn.leancloud.json.JSONObject;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class UserFriendTest extends TestCase {
    private boolean testSucceed = false;
    private CountDownLatch latch = null;

    private String tomObjectId = "";
    private String jerryObjectId = "";
    private String anonymousObjectId = "";
    private String tuffyObjectId = "";

    public UserFriendTest(String name) {
        super(name);
        Configure.initializeRuntime();
        tomObjectId = tomLogin().getObjectId();
        jerryObjectId = jerryLogin().getObjectId();
        tuffyObjectId = tuffyLogin().getObjectId();
        anonymousObjectId = anonymousLogin().getObjectId();
    }

    public static Test suite() {
        return new TestSuite(UserFriendTest.class);
    }

    private LCUser tomLogin() {
        Map<String, Object> thirdPartyData = new HashMap<String, Object>();
        thirdPartyData.put("expires_in", 7200);
        thirdPartyData.put("openid", "284279WEOPENID");
        thirdPartyData.put("access_token", "WEIXIN=ACCESS_TOKEN");
        thirdPartyData.put("refresh_token", "WEIXIN_REFRESH_TOKEN");
        thirdPartyData.put("scope", "public_profile");
        LCUser result = LCUser.loginWithAuthData(thirdPartyData, "weixin").blockingFirst();
        System.out.println("succeed to login as tom user.");
        return result;
    }

    private LCUser jerryLogin() {
        Map<String, Object> thirdPartyData = new HashMap<String, Object>();
        thirdPartyData.put("expires_in", 115057);
        thirdPartyData.put("uid", "271XFEFEW273");
        thirdPartyData.put("access_token", "2.00xxxfafheiwfr3urh23");
        thirdPartyData.put("refresh_token", "2.00xxxREFRESH_TOKEN");
        LCUser result = LCUser.loginWithAuthData(thirdPartyData, "weibo").blockingFirst();
        System.out.println("succeed to login as jerry user.");
        return result;
    }

    private LCUser tuffyLogin() {
        Map<String, Object> thirdPartyData = new HashMap<String, Object>();
        thirdPartyData.put("expires_in", 115057);
        thirdPartyData.put("uid", "2722dqe2EW273");
        thirdPartyData.put("access_token", "2.00xxxfafheiwfr3urh23");
        thirdPartyData.put("refresh_token", "2.00xxxREFRESH_TOKEN");
        LCUser result = LCUser.loginWithAuthData(thirdPartyData, "weibo").blockingFirst();
        System.out.println("succeed to login as tuffy user.");
        return result;
    }

    private LCUser anonymousLogin() {
        LCUser result = LCUser.logInAnonymously().blockingFirst();
        System.out.println("succeed to login as anonymous user.");
        return result;
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("setUp: tom-" + tomObjectId + ", jerry-" + jerryObjectId
                + ", tuffy-" + tuffyObjectId + ", anonymous-" + anonymousObjectId);
        testSucceed = false;
        latch = new CountDownLatch(1);
        LCUser.logOut();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            tomLogin();
            LCObject.getQuery(LCFriendship.class)
                    .whereEqualTo(LCFriendship.ATTR_USER, LCObject.createWithoutData(LCUser.class, tomObjectId))
                    .deleteAll();

            jerryLogin();
            LCObject.getQuery(LCFriendship.class)
                    .whereEqualTo(LCFriendship.ATTR_USER, LCObject.createWithoutData(LCUser.class, jerryObjectId))
                    .deleteAll();

            tuffyLogin();
            LCObject.getQuery(LCFriendship.class)
                    .whereEqualTo(LCFriendship.ATTR_USER, LCObject.createWithoutData(LCUser.class, tuffyObjectId))
                    .deleteAll();

            anonymousLogin();
            LCObject.getQuery(LCFriendship.class)
                    .whereEqualTo(LCFriendship.ATTR_USER, LCObject.createWithoutData(LCUser.class, anonymousObjectId))
                    .deleteAll();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("tearDown");
    }

    public void testAcceptThenQuery() throws Exception {
        LCUser tom = tomLogin();
        Map<String, Object> attr1 = new HashMap<String, Object>();
        attr1.put("group", "cat");
        final LCFriendshipRequest request1 = tom.applyFriendshipInBackground(
                LCObject.createWithoutData(LCUser.class, jerryObjectId), attr1).blockingFirst();
        System.out.println("tom succeed to apply friend request to jerry. objectId=" + request1.getObjectId());

        final LCUser jerry = jerryLogin();
        LCFriendshipRequest requestTmp = jerry.declineFriendshipRequest(request1).blockingFirst();
        System.out.println("jerry declined request from tom. status="
                + requestTmp.get(LCFriendshipRequest.ATTR_STATUS)
                + ", updatedAt=" + requestTmp.getUpdatedAtString());

        requestTmp = jerry.acceptFriendshipRequest(request1, null).blockingFirst();
        System.out.println("jerry accepted request from tom. status="
                + requestTmp.get(LCFriendshipRequest.ATTR_STATUS)
                + ", updatedAt=" + requestTmp.getUpdatedAtString());

        jerry.friendshipRequestQuery(LCFriendshipRequest.STATUS_ANY, false, true)
                .findInBackground()
                .subscribe(new Observer<List<LCFriendshipRequest>>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(List<LCFriendshipRequest> lcFriendshipRequests) {
                        for (LCFriendshipRequest req: lcFriendshipRequests) {
                            req.delete();
                        }
                        jerry.queryFriendship(0, 0, null).subscribe(new Observer<List<LCFriendship>>() {
                            @Override
                            public void onSubscribe(Disposable disposable) {

                            }

                            @Override
                            public void onNext(List<LCFriendship> lcFriendships) {
                                System.out.println("succeed to query friendship of jerry. result: " + lcFriendships.size());
                                testSucceed = lcFriendships.size() == 1;
                                lcFriendships.get(0).delete();
                                latch.countDown();
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                System.out.println("failed to query friendship of jerry. cause: " + throwable.getMessage());
                                latch.countDown();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("failed to query friendship request. cause: " + throwable.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        latch.await();
        assertTrue(testSucceed);
    }

    public void testRequestAndAcceptFriends() throws Exception {
        LCUser tom = tomLogin();
        Map<String, Object> attr1 = new HashMap<String, Object>();
        attr1.put("group", "cat");
        final LCFriendshipRequest request1 = tom.applyFriendshipInBackground(
                LCObject.createWithoutData(LCUser.class, jerryObjectId), attr1).blockingFirst();
        System.out.println("tom succeed to apply friend request to jerry. objectId=" + request1.getObjectId());

        List<LCFriendshipRequest> requests = tom.friendshipRequestQuery(LCFriendshipRequest.STATUS_PENDING,
                false, false)
                .findInBackground()
                .blockingFirst();
        System.out.println("request number sent from tom is: " + requests.size());

        LCUser tuffy = tuffyLogin();
        Map<String, Object> attr2 = new HashMap<String, Object>();
        attr2.put("group", "league");
        final LCFriendshipRequest request2 = tuffy.applyFriendshipInBackground(
                LCObject.createWithoutData(LCUser.class, jerryObjectId), attr2).blockingFirst();
        System.out.println("tuffy succeed to apply friend request to jerry. objectId=" + request2.getObjectId());

        requests = tuffy.friendshipRequestQuery(LCFriendshipRequest.STATUS_PENDING,
                false, false)
                .findInBackground()
                .blockingFirst();
        System.out.println("request number sent from tuffy is: " + requests.size());

        final LCUser jerry = jerryLogin();
        LCFriendshipRequest requestTmp = jerry.declineFriendshipRequest(request1).blockingFirst();
        System.out.println("jerry declined request from tom. status="
                + requestTmp.get(LCFriendshipRequest.ATTR_STATUS)
                + ", updatedAt=" + requestTmp.getUpdatedAtString());

        requestTmp = jerry.acceptFriendshipRequest(request2, null).blockingFirst();
        System.out.println("jerry accepted request from tuffy. status="
                + requestTmp.get(LCFriendshipRequest.ATTR_STATUS)
                + ", updatedAt=" + requestTmp.getUpdatedAtString());

        jerry.friendshipRequestQuery(LCFriendshipRequest.STATUS_ANY, false, true)
                .findInBackground()
                .subscribe(new Observer<List<LCFriendshipRequest>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<LCFriendshipRequest> lcFriendshipRequests) {
                        System.out.println("friendship requests number to jerry is: " + lcFriendshipRequests.size());
                        for(LCFriendshipRequest request: lcFriendshipRequests) {
                            System.out.println("try to delete friendshipRquest: " + request);
                            request.delete();
                        }
                        jerry.queryFriendship(0, 0, null).subscribe(new Observer<List<LCFriendship>>() {
                            @Override
                            public void onSubscribe(Disposable disposable) {

                            }

                            @Override
                            public void onNext(List<LCFriendship> lcFriendships) {
                                if (null == lcFriendships || lcFriendships.size() != 1) {
                                    System.out.println("friendship query of jerry is error!!!!!");
                                } else {
                                    System.out.println("jerry's friend: " + lcFriendships.get(0));
                                    lcFriendships.get(0).delete();
                                    testSucceed = true;
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
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println("failed to query friendship of jerry. cause: " + e.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        latch.await();
        assertTrue(testSucceed);
    }

    public void testBlockFriend() throws Exception{
        jerryLogin();
        LCUser.getCurrentUser().blockFriendInBackground(tomObjectId)
        .subscribe(new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                
            }

            @Override
            public void onNext(JSONObject t) {
                System.out.println(" block tom success");
                LCUser.getCurrentUser().blockFriendInBackground(tuffyObjectId)
                .subscribe(new Consumer<JSONObject>() {
                @Override
                public void accept(JSONObject t) throws Exception {
                    System.out.println(" block tuffy success");
                   LCUser.getCurrentUser().unblockFriendInBackground(tomObjectId)
                   .subscribe(new Consumer<JSONObject>() {

                    @Override
                    public void accept(JSONObject t) throws Exception {
                        System.out.println(" unblock tom success");

                        LCUser.getCurrentUser().friendshipBlockQuery(LCBlockRelation.class)
                        .skip(0)
                        .limit(10)
                        .findInBackground()
                        .subscribe(new Consumer<List<LCBlockRelation>>() {

                            @Override
                            public void accept(List<LCBlockRelation> t) throws Exception {
                                System.out.println(" get blick list size = " + t.size());
                                System.out.println(" data = " + t);
                                if (t.size() != 1) {
                                    latch.countDown();
                                } else{
                                    System.out.println(" first user id = " + t.get(0).getUser().getObjectId());
                                LCUser.getCurrentUser().unblockFriendInBackground(tuffyObjectId)
                                        .subscribe(new Consumer<JSONObject>() {

                                            @Override
                                            public void accept(JSONObject t) throws Exception {
                                                System.out.println(" unblock tuffy success");
                                                testSucceed = true;
                                                latch.countDown();
                                            }
                                        }, new Consumer<Throwable>() {

                                            @Override
                                            public void accept(Throwable t) throws Exception {
                                                t.printStackTrace();
                                                latch.countDown();
                                            }
                                        });
                                }
                            }
            
                        }, new Consumer<Throwable>(){

                            @Override
                            public void accept(Throwable t) throws Exception {
                                t.printStackTrace();
                                latch.countDown();
                            }

                        });
                        
                    }
                       
                   }, new Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable t) throws Exception {
                        t.printStackTrace();
                        latch.countDown();
                        
                    }
                       
                   });
                    
                }
                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable t) throws Exception {
                       t.printStackTrace();
                       latch.countDown();
                        
                    }
                    
                });
                
            }

            @Override
            public void onComplete() {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                latch.countDown();
                
            }
        });
        latch.await();
        assertTrue(testSucceed);
    }




    public void testQueryFriendship() throws Exception {
        LCUser tom = tomLogin();
        Map<String, Object> attr1 = new HashMap<String, Object>();
        attr1.put("group", "cat");
        final LCFriendshipRequest request1 = tom.applyFriendshipInBackground(
                LCObject.createWithoutData(LCUser.class, jerryObjectId), attr1).blockingFirst();
        System.out.println("tom succeed to apply friend request to jerry. objectId=" + request1.getObjectId());

        List<LCFriendshipRequest> requests = tom.friendshipRequestQuery(LCFriendshipRequest.STATUS_PENDING,
                false, false)
                .findInBackground()
                .blockingFirst();
        System.out.println("request number sent from tom is: " + requests.size());

        LCUser tuffy = tuffyLogin();
        Map<String, Object> attr2 = new HashMap<String, Object>();
        attr2.put("group", "league");
        final LCFriendshipRequest request2 = tuffy.applyFriendshipInBackground(
                LCObject.createWithoutData(LCUser.class, jerryObjectId), attr2).blockingFirst();
        System.out.println("tuffy succeed to apply friend request to jerry. objectId=" + request2.getObjectId());

        requests = tuffy.friendshipRequestQuery(LCFriendshipRequest.STATUS_PENDING,
                false, false)
                .findInBackground()
                .blockingFirst();
        System.out.println("request number sent from tuffy is: " + requests.size());

        final LCUser jerry = jerryLogin();
        LCFriendshipRequest requestTmp = jerry.acceptFriendshipRequest(request1, null).blockingFirst();
        System.out.println("jerry accept request from tom. status="
                + requestTmp.get(LCFriendshipRequest.ATTR_STATUS)
                + ", updatedAt=" + requestTmp.getUpdatedAtString());

        requestTmp = jerry.acceptFriendshipRequest(request2, null).blockingFirst();
        System.out.println("jerry accepted request from tuffy. status="
                + requestTmp.get(LCFriendshipRequest.ATTR_STATUS)
                + ", updatedAt=" + requestTmp.getUpdatedAtString());

        jerry.friendshipRequestQuery(LCFriendshipRequest.STATUS_ANY, false, true)
                .findInBackground()
                .subscribe(new Observer<List<LCFriendshipRequest>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<LCFriendshipRequest> lcFriendshipRequests) {
                        System.out.println("friendship requests number to jerry is: " + lcFriendshipRequests.size());
                        for(LCFriendshipRequest request: lcFriendshipRequests) {
                            System.out.println("try to delete friendshipRquest: " + request);
                            request.delete();
                        }
                        jerry.friendshipQuery(false).limit(10).findInBackground().subscribe(new Observer<List<LCFriendship>>() {
                            @Override
                            public void onSubscribe(Disposable disposable) {

                            }

                            @Override
                            public void onNext(List<LCFriendship> lcFriendships) {
                                if (null == lcFriendships || lcFriendships.size() == 0) {
                                    System.out.println("friendship query of jerry is error!!!!!");
                                    latch.countDown();
                                } else {
                                    System.out.println("jerry's friend: size = " + lcFriendships.size() + " data = " + lcFriendships);
                                    jerry.blockFriendInBackground(tomObjectId).subscribe(new Consumer<JSONObject>() {
                                        @Override
                                        public void accept(JSONObject jsonObject) throws Exception {
                                            System.out.println(" block tom success");
                                            jerry.queryFriendship(0,10,null)
                                                    .subscribe(new Consumer<List<LCFriendship>>() {
                                                        @Override
                                                        public void accept(List<LCFriendship> lcFriendships) throws Exception {
                                                            System.out.println("jerry's friend: size = " + lcFriendships.size() + " data = " + lcFriendships);
                                                            if(lcFriendships.size() == 1) {
                                                                jerry.unblockFriendInBackground(tomObjectId).subscribe(new Consumer<JSONObject>() {
                                                                    @Override
                                                                    public void accept(JSONObject jsonObject) throws Exception {
                                                                        System.out.println(" unblock tom success");
                                                                        jerry.queryFriendship(0,10,null)
                                                                                .subscribe(new Consumer<List<LCFriendship>>() {
                                                                                    @Override
                                                                                    public void accept(List<LCFriendship> lcFriendships) throws Exception {
                                                                                        System.out.println("jerry's friend: size = " + lcFriendships.size() + " data = " + lcFriendships);
                                                                                        if (lcFriendships.size() == 2) {
                                                                                            lcFriendships.get(0).delete();
                                                                                            lcFriendships.get(1).delete();
                                                                                            testSucceed = true;
                                                                                        }
                                                                                        latch.countDown();
                                                                                    }
                                                                                }, new Consumer<Throwable>() {
                                                                                    @Override
                                                                                    public void accept(Throwable throwable) throws Exception {
                                                                                        throwable.printStackTrace();
                                                                                        latch.countDown();
                                                                                    }
                                                                                });
                                                                    }
                                                                }, new Consumer<Throwable>() {
                                                                    @Override
                                                                    public void accept(Throwable throwable) throws Exception {
                                                                        throwable.printStackTrace();
                                                                        latch.countDown();
                                                                    }
                                                                });
                                                            }else{
                                                                latch.countDown();
                                                            }
                                                        }
                                                    }, new Consumer<Throwable>() {
                                                        @Override
                                                        public void accept(Throwable throwable) throws Exception {
                                                            throwable.printStackTrace();
                                                            latch.countDown();
                                                        }
                                                    });
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            throwable.printStackTrace();
                                            latch.countDown();
                                        }
                                    });
                                }
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
                    public void onError(@NonNull Throwable e) {
                        System.out.println("failed to query friendship of jerry. cause: " + e.getMessage());
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
