package cn.leancloud;

import cn.leancloud.auth.UserBasedTestCase;
import cn.leancloud.json.JSONObject;
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

    public void testUpdateUserNotExistStatistic() throws Exception {
        LCLeaderboard leaderboard = LCLeaderboard.fetchByName(LEADERBOARD_NAME).blockingFirst();
        Map<String, Double> scores = new HashMap<>();
        scores.put("leancloudgogo", 432.34);
        scores.put("kills", 32.0);
        LCLeaderboard.updateStatistic(LCUser.currentUser(), scores).subscribe(new Observer<LCStatisticResult>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull LCStatisticResult jsonObject) {
                latch.countDown();
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
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
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull LCStatisticResult jsonObject) {
                testSucceed = true;
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
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull LCStatisticResult jsonObject) {
                latch.countDown();
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
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
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull LCStatisticResult lcStatisticResult) {
                latch.countDown();
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
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
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull LCStatisticResult lcStatisticResult) {
                if (lcStatisticResult.getResults().size() > 0) {
                    System.out.println("failed to get user all statistics. cause: result list is incorrect.");
                    latch.countDown();
                } else {
                    LCLeaderboard.getUserStatistics(targetUser, Arrays.asList("leancloudgogo"))
                            .subscribe(new Observer<LCStatisticResult>() {
                                @Override
                                public void onSubscribe(@NotNull Disposable disposable) {

                                }

                                @Override
                                public void onNext(@NotNull LCStatisticResult lcStatisticResult) {
                                    testSucceed = lcStatisticResult.getResults().size() < 1;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(@NotNull Throwable throwable) {
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
            public void onError(@NotNull Throwable throwable) {
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
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull LCStatisticResult lcStatisticResult) {
                if (lcStatisticResult.getResults().size() > 0) {
                    System.out.println("failed to get user all statistics. cause: result list is incorrect.");
                    latch.countDown();
                } else {
                    LCLeaderboard.getUserStatistics(targetUser, Arrays.asList("leancloudgogo"))
                            .subscribe(new Observer<LCStatisticResult>() {
                                @Override
                                public void onSubscribe(@NotNull Disposable disposable) {

                                }

                                @Override
                                public void onNext(@NotNull LCStatisticResult lcStatisticResult) {
                                    testSucceed = lcStatisticResult.getResults().size() < 1;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(@NotNull Throwable throwable) {
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
            public void onError(@NotNull Throwable throwable) {
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
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull LCStatisticResult lcStatisticResult) {
                if (lcStatisticResult.getResults().size() < 1) {
                    System.out.println("failed to get user all statistics. cause: result list is incorrect.");
                    latch.countDown();
                } else {
                    LCLeaderboard.getUserStatistics(LCUser.currentUser(), Arrays.asList("leancloudgogo"))
                            .subscribe(new Observer<LCStatisticResult>() {
                                @Override
                                public void onSubscribe(@NotNull Disposable disposable) {

                                }

                                @Override
                                public void onNext(@NotNull LCStatisticResult lcStatisticResult) {
                                    testSucceed = lcStatisticResult.getResults().size() > 0;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(@NotNull Throwable throwable) {
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
            public void onError(@NotNull Throwable throwable) {
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
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull LCStatisticResult jsonObject) {
                System.out.println("step 1: update statistic with scores: " + scores);
                final List<String> selectUserKeys = new ArrayList<>();
                selectUserKeys.add("sessionToken");
                leaderboard.getResults(0, 100, selectUserKeys, null).subscribe(new Observer<LCLeaderboardResult>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable disposable) {

                    }

                    @Override
                    public void onNext(@NotNull LCLeaderboardResult lcRankings) {
                        System.out.println("step 2: get global statistic. results: " + lcRankings);
                        leaderboard.getAroundResults(LCUser.currentUser().getObjectId(), 0, 10, selectUserKeys, null).subscribe(new Observer<LCLeaderboardResult>() {
                            @Override
                            public void onSubscribe(@NotNull Disposable disposable) {

                            }

                            @Override
                            public void onNext(@NotNull LCLeaderboardResult lcLeaderboardResult) {
                                System.out.println("step 3: get around statistic. results: " + lcLeaderboardResult);
                                testSucceed = lcLeaderboardResult.getResults().size() > 0;
                                latch.countDown();
                            }

                            @Override
                            public void onError(@NotNull Throwable throwable) {
                                System.out.println("failed to get around statistic. cause: " + throwable);
                                latch.countDown();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });

                    }

                    @Override
                    public void onError(@NotNull Throwable throwable) {
                        System.out.println("failed to get global statistic. cause: " + throwable);
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

            }

            @Override
            public void onError(@NotNull Throwable throwable) {
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
}