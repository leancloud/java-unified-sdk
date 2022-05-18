package cn.leancloud;

import cn.leancloud.auth.UserBasedTestCase;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;


import java.util.*;
import java.util.concurrent.CountDownLatch;

public class LCLeaderboardUserTests extends UserBasedTestCase {
    private boolean testSucceed = false;
    private CountDownLatch latch = null;
    static final String LEADERBOARD_NAME = "leancloudgogo";

    public static final String JFENG_EMAIL = "jfeng@test.com";
    public static String DEFAULT_PASSWD = "FER$@$@#Ffwe";

    public LCLeaderboardUserTests(String name) {
        super(name);
        setAuthUser(JFENG_EMAIL, DEFAULT_PASSWD);
    }

    public static Test suite() {
        return new TestSuite(LCLeaderboardUserTests.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testSucceed = false;
        latch = new CountDownLatch(1);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testQueryUserGroupLeaderboard() throws Exception {
        LCLeaderboard leaderboard = LCLeaderboard.fetchByName(LEADERBOARD_NAME).blockingFirst();

        final String USER_PREFIX = "DeletedUserTest";
        List<String> testObjectIds = new ArrayList<>(10);
        String username = null;
        String userEmail = null;
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            username = USER_PREFIX + i;
            userEmail = username + "@abc.com";
            LCUser result = UserFollowshipTest.prepareUser(username, userEmail, true);
            testObjectIds.add(result.getObjectId());
            if (!result.isAuthenticated()) {
                result = LCUser.logIn(username, passwd).blockingFirst();
            }
            Map<String, Double> scores = new HashMap<>();
            scores.put("leancloudgogo", random.nextDouble());
            LCLeaderboard.updateStatistic(result, scores).blockingFirst();
        }
        leaderboard.getGroupResults(testObjectIds, 0, 5, null, null)
                .subscribe(new Observer<LCLeaderboardResult>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable disposable) {

                    }

                    @Override
                    public void onNext(@NotNull LCLeaderboardResult lcLeaderboardResult) {
                        for (LCRanking ranking: lcLeaderboardResult.getResults()) {
                            System.out.println(JSON.toJSONString(ranking));
                        }
                        testSucceed = true;
                        latch.countDown();
                    }

                    @Override
                    public void onError(@NotNull Throwable throwable) {
                        testSucceed = false;
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        latch.await();
        assertTrue(testSucceed);
    }

    public void testUserGroupAroundLeaderboard() throws Exception {
        LCLeaderboard leaderboard = LCLeaderboard.fetchByName(LEADERBOARD_NAME).blockingFirst();

        final String USER_PREFIX = "DeletedUserTest";
        List<String> testObjectIds = new ArrayList<>(10);
        String username = null;
        String userEmail = null;
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            username = USER_PREFIX + i;
            userEmail = username + "@abc.com";
            LCUser result = UserFollowshipTest.prepareUser(username, userEmail, true);
            testObjectIds.add(result.getObjectId());
            if (!result.isAuthenticated()) {
                result = LCUser.logIn(username, passwd).blockingFirst();
            }
            Map<String, Double> scores = new HashMap<>();
            scores.put("leancloudgogo", random.nextDouble());
            LCLeaderboard.updateStatistic(result, scores).blockingFirst();
        }
        leaderboard.getAroundInGroupResults(testObjectIds, testObjectIds.get(4), 3, null, null)
                .subscribe(new Observer<LCLeaderboardResult>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable disposable) {

                    }

                    @Override
                    public void onNext(@NotNull LCLeaderboardResult lcLeaderboardResult) {
                        for (LCRanking ranking: lcLeaderboardResult.getResults()) {
                            System.out.println(JSON.toJSONString(ranking));
                        }
                        testSucceed = true;
                        latch.countDown();
                    }

                    @Override
                    public void onError(@NotNull Throwable throwable) {
                        testSucceed = false;
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        latch.await();
        assertTrue(testSucceed);
    }

    public void testUpdateUserNotExistStatistic() throws Exception {
        LCLeaderboard leaderboard = LCLeaderboard.fetchByName(LEADERBOARD_NAME).blockingFirst();
        Map<String, Double> scores = new HashMap<>();
        scores.put("leancloudgogo", 432.34);
        scores.put("kills", 32.0);
        LCLeaderboard.updateStatistic(LCUser.currentUser(), scores).subscribe(new Observer<LCStatisticResult>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCStatisticResult jsonObject) {
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
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

    public void testUpdateUserExistStatistic() throws Exception {
        LCLeaderboard leaderboard = LCLeaderboard.fetchByName(LEADERBOARD_NAME).blockingFirst();
        Map<String, Double> scores = new HashMap<>();
        scores.put("leancloudgogo", (double)System.currentTimeMillis());
        LCLeaderboard.updateStatistic(LCUser.currentUser(), scores).subscribe(new Observer<LCStatisticResult>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCStatisticResult jsonObject) {
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

    public void testUpdateUnauthUserStatistic() throws Exception {
        clearCurrentAuthenticatedUser();
        LCUser unauthUser = LCUser.createWithoutData(LCUser.class, "60e57e6eb8524555a2c85fd1");
        final Map<String, Double> scores = new HashMap<>();
        scores.put(LEADERBOARD_NAME, (double)System.currentTimeMillis());
        LCLeaderboard.updateStatistic(unauthUser, scores).subscribe(new Observer<LCStatisticResult>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCStatisticResult jsonObject) {
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
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

    public void testAuthenticatedUserGetIllegalUserStatistics() throws Exception {
        final LCUser targetUser = LCUser.createWithoutData(LCUser.class, "thisisnotexistedUser");
        LCLeaderboard.getUserStatistics(targetUser).subscribe(new Observer<LCStatisticResult>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCStatisticResult lcStatisticResult) {
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("failed to get user all statistics. cause: " + throwable);
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

    public void testUnauthUserGetNotExistUserStatistics() throws Exception {
        clearCurrentAuthenticatedUser();
        final LCUser targetUser = LCUser.createWithoutData(LCUser.class, "5f51c1287628f2468aa696e6");
        LCLeaderboard.getUserStatistics(targetUser).subscribe(new Observer<LCStatisticResult>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCStatisticResult lcStatisticResult) {
                if (lcStatisticResult.getResults().size() > 0) {
                    System.out.println("failed to get user all statistics. cause: result list is incorrect.");
                    latch.countDown();
                } else {
                    LCLeaderboard.getUserStatistics(targetUser, Arrays.asList("leancloudgogo"))
                            .subscribe(new Observer<LCStatisticResult>() {
                                @Override
                                public void onSubscribe(Disposable disposable) {

                                }

                                @Override
                                public void onNext(LCStatisticResult lcStatisticResult) {
                                    testSucceed = lcStatisticResult.getResults().size() < 1;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    System.out.println("failed to get user specified statistics. cause: " + throwable);
                                    latch.countDown();
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("failed to get user all statistics. cause: " + throwable);
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

    public void testAuthenticatedUserGetNotExistUserStatistics() throws Exception {
        final LCUser targetUser = LCUser.createWithoutData(LCUser.class, "5f51c1287628f2468aa696e6");
        LCLeaderboard.getUserStatistics(targetUser).subscribe(new Observer<LCStatisticResult>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCStatisticResult lcStatisticResult) {
                if (lcStatisticResult.getResults().size() > 0) {
                    System.out.println("failed to get user all statistics. cause: result list is incorrect.");
                    latch.countDown();
                } else {
                    LCLeaderboard.getUserStatistics(targetUser, Arrays.asList("leancloudgogo"))
                            .subscribe(new Observer<LCStatisticResult>() {
                                @Override
                                public void onSubscribe(Disposable disposable) {

                                }

                                @Override
                                public void onNext(LCStatisticResult lcStatisticResult) {
                                    testSucceed = lcStatisticResult.getResults().size() < 1;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    System.out.println("failed to get user specified statistics. cause: " + throwable);
                                    latch.countDown();
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("failed to get user all statistics. cause: " + throwable);
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

    public void testGetAuthenticatedUserStatistics() throws Exception {
        final Map<String, Double> scores = new HashMap<>();
        scores.put(LEADERBOARD_NAME, (double)System.currentTimeMillis());
        LCLeaderboard.updateStatistic(LCUser.currentUser(), scores).blockingFirst();
        LCLeaderboard.getUserStatistics(LCUser.currentUser()).subscribe(new Observer<LCStatisticResult>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCStatisticResult lcStatisticResult) {
                if (lcStatisticResult.getResults().size() < 1) {
                    System.out.println("failed to get user all statistics. cause: result list is incorrect.");
                    latch.countDown();
                } else {
                    LCLeaderboard.getUserStatistics(LCUser.currentUser(), Arrays.asList("leancloudgogo"))
                            .subscribe(new Observer<LCStatisticResult>() {
                                @Override
                                public void onSubscribe(Disposable disposable) {

                                }

                                @Override
                                public void onNext(LCStatisticResult lcStatisticResult) {
                                    testSucceed = lcStatisticResult.getResults().size() > 0;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    System.out.println("failed to get user specified statistics. cause: " + throwable);
                                    latch.countDown();
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("failed to get user all statistics. cause: " + throwable);
                latch.countDown();
            }

            @Override
            public void onComplete() {

            }
        });
        latch.await();
        assertTrue(testSucceed);
    }
    public void testUpdateAndGetStatistic() throws Exception {
        final LCLeaderboard leaderboard = LCLeaderboard.fetchByName(LEADERBOARD_NAME).blockingFirst();
        final Map<String, Double> scores = new HashMap<>();
        scores.put("leancloudgogo", (double)System.currentTimeMillis());
        LCLeaderboard.updateStatistic(LCUser.currentUser(), scores).subscribe(new Observer<LCStatisticResult>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCStatisticResult jsonObject) {
                System.out.println("step 1: update statistic with scores: " + scores);
                final List<String> selectUserKeys = new ArrayList<>();
                selectUserKeys.add("sessionToken");
                leaderboard.getResults(0, 100, selectUserKeys, null).subscribe(new Observer<LCLeaderboardResult>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(LCLeaderboardResult lcRankings) {
                        System.out.println("step 2: get global statistic. results: " + lcRankings);
                        leaderboard.getAroundResults(LCUser.currentUser().getObjectId(), 0, 10, selectUserKeys, null).subscribe(new Observer<LCLeaderboardResult>() {
                            @Override
                            public void onSubscribe(Disposable disposable) {

                            }

                            @Override
                            public void onNext(LCLeaderboardResult lcLeaderboardResult) {
                                System.out.println("step 3: get around statistic. results: " + lcLeaderboardResult);
                                testSucceed = lcLeaderboardResult.getResults().size() > 0;
                                latch.countDown();
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                System.out.println("failed to get around statistic. cause: " + throwable);
                                latch.countDown();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("failed to get global statistic. cause: " + throwable);
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("failed to update statistic. cause: " + throwable);
                latch.countDown();
            }

            @Override
            public void onComplete() {

            }
        });
        latch.await();
        assertTrue(testSucceed);
    }

    public void testGetOlderVersionLeaderboard() throws Exception {
        final LCLeaderboard leaderboard = LCLeaderboard.fetchByName(LEADERBOARD_NAME).blockingFirst();
        leaderboard.setVersion(leaderboard.getVersion() - 1);
        leaderboard.getResults(0, 100, null, new ArrayList<String>()).subscribe(new Observer<LCLeaderboardResult>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCLeaderboardResult lcLeaderboardResult) {
                leaderboard.setVersion(leaderboard.getVersion() - 1);
                leaderboard.getResults(0, 0, new ArrayList<String>(), new ArrayList<String>())
                        .subscribe(new Observer<LCLeaderboardResult>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(LCLeaderboardResult lcLeaderboardResult) {
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        testSucceed = true;
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("failed to get older version leaderboard, cause: " + throwable);
                latch.countDown();
            }

            @Override
            public void onComplete() {

            }
        });
        latch.await();
        assertTrue(testSucceed);
    }

    public void testGetIllegalVersionLeaderboard() throws Exception {
        final LCLeaderboard leaderboard = LCLeaderboard.createWithoutData(LEADERBOARD_NAME, "_User");
        leaderboard.setVersion(-2);
        leaderboard.getResults(0, 100, null, new ArrayList<String>()).subscribe(new Observer<LCLeaderboardResult>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCLeaderboardResult lcLeaderboardResult) {
                leaderboard.setVersion(200000);
                leaderboard.getAroundResults(LCUser.currentUser().getObjectId(), 0, 100, null,
                        null)
                        .subscribe(new Observer<LCLeaderboardResult>() {
                            @Override
                            public void onSubscribe(Disposable disposable) {

                            }

                            @Override
                            public void onNext(LCLeaderboardResult lcLeaderboardResult) {
                                latch.countDown();
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                testSucceed = true;
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
        assertTrue(testSucceed);
    }
}