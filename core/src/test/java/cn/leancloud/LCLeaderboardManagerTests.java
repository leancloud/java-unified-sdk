package cn.leancloud;

import cn.leancloud.json.JSONObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LCLeaderboardManagerTests extends TestCase {
    private boolean testSucceed = false;
    private CountDownLatch latch = null;
    public LCLeaderboardManagerTests(String name) {
        super(name);
        Configure.initializeWithMasterKey("ohqhxu3mgoj2eyj6ed02yliytmbes3mwhha8ylnc215h0bgk",
                "kjbda51ef9m5bn26fsttkz79d72qk0ruu543onnql8r3q0h8",
                "https://ohqhxu3m.lc-cn-n1-shared.com");
    }

    public static Test suite() {
        return new TestSuite(LCLeaderboardManagerTests.class);
    }

    @Override
    protected void setUp() throws Exception {
        testSucceed = false;
        latch = new CountDownLatch(1);
    }

    @Override
    protected void tearDown() throws Exception {
        ;
    }

    public void testEnumInstance() throws Exception {
        LCLeaderboard.LCLeaderboardOrder order = LCLeaderboard.lookup(LCLeaderboard.LCLeaderboardOrder.class, "Ascending");
        assertTrue(order == LCLeaderboard.LCLeaderboardOrder.Ascending);
        order = LCLeaderboard.lookup(LCLeaderboard.LCLeaderboardOrder.class, "ascending");
        assertTrue(order == LCLeaderboard.LCLeaderboardOrder.Ascending);
        order = LCLeaderboard.lookup(LCLeaderboard.LCLeaderboardOrder.class, "descending");
        assertTrue(order == LCLeaderboard.LCLeaderboardOrder.Descending);
        order = LCLeaderboard.lookup(LCLeaderboard.LCLeaderboardOrder.class, "cending");
        assertTrue(order == null);
    }

    public void testLeaderboardCRUD() throws Exception {
        final String statisticName = "justSoso";
        LCLeaderboard.create(statisticName, LCLeaderboard.LCLeaderboardOrder.Ascending,
                LCLeaderboard.LCLeaderboardUpdateStrategy.Last,
                LCLeaderboard.LCLeaderboardVersionChangeInterval.Day).subscribe(new Observer<LCLeaderboard>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull final LCLeaderboard lcLeaderboard) {
                System.out.println("step 1: create leaderboard");
                if (!statisticName.equals(lcLeaderboard.getStatisticName())
                    || lcLeaderboard.getVersion() != 0 || lcLeaderboard.getNextResetAt() == null
                    || lcLeaderboard.getCreatedAt() == null
                    || lcLeaderboard.getOrder() != LCLeaderboard.LCLeaderboardOrder.Ascending
                    || lcLeaderboard.getUpdateStrategy() != LCLeaderboard.LCLeaderboardUpdateStrategy.Last
                    || lcLeaderboard.getVersionChangeInterval() != LCLeaderboard.LCLeaderboardVersionChangeInterval.Day) {
                    System.out.println("failed, leaderboard attributes are not right");
                    latch.countDown();
                    return;
                }
                lcLeaderboard.updateUpdateStrategy(LCLeaderboard.LCLeaderboardUpdateStrategy.Better).subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable disposable) {

                    }

                    @Override
                    public void onNext(@NotNull Boolean aBoolean) {
                        System.out.println("step 2: update leaderboard update strategy.");
                        if (!aBoolean || lcLeaderboard.getUpdateStrategy() != LCLeaderboard.LCLeaderboardUpdateStrategy.Better) {
                            System.out.println("failed, leaderboard update strategy isnot right");
                            latch.countDown();
                            return;
                        }
                        lcLeaderboard.updateVersionChangeInterval(LCLeaderboard.LCLeaderboardVersionChangeInterval.Week)
                                .subscribe(new Observer<Boolean>() {
                                    @Override
                                    public void onSubscribe(@NotNull Disposable disposable) {

                                    }

                                    @Override
                                    public void onNext(@NotNull Boolean aBoolean) {
                                        System.out.println("step 3: update leaderboard version change interval.");
                                        if (!aBoolean || lcLeaderboard.getVersionChangeInterval() != LCLeaderboard.LCLeaderboardVersionChangeInterval.Week) {
                                            System.out.println("failed, leaderboard version change interval isnot right");
                                            latch.countDown();
                                            return;
                                        }
                                        int oldVersion = lcLeaderboard.getVersion();
                                        lcLeaderboard.reset().blockingFirst();
                                        System.out.println("step 4: reset current leaderboard.");
                                        if (lcLeaderboard.getVersion() <= oldVersion) {
                                            System.out.println("failed, leaderboard version isnot right");
                                            latch.countDown();
                                        } else {
                                            lcLeaderboard.destroy().blockingFirst();
                                            System.out.println("step 5: destroy current leaderboard.");
                                            testSucceed = true;
                                            latch.countDown();
                                        }
                                    }

                                    @Override
                                    public void onError(@NotNull Throwable throwable) {
                                        System.out.println("failed to change version update interval. cause: " + throwable);
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                    }

                    @Override
                    public void onError(@NotNull Throwable throwable) {
                        System.out.println("failed to change update strategy. cause: " + throwable);
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
                System.out.println("failed to create leaderboard. cause: " + throwable);
                latch.countDown();
            }

            @Override
            public void onComplete() {

            }
        });
        latch.await();
        assertTrue(testSucceed);
    }

    public void testLeaderboardCreateWithName() throws Exception {
        LCLeaderboard leaderboard = LCLeaderboard.createWithoutData("leancloudgogo");
        leaderboard.getResults(0, 0, null, null, null, true)
                .subscribe(new Observer<LCLeaderboardResult>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull LCLeaderboardResult lcLeaderboardResult) {
                System.out.println("succeed to get results.");
                testSucceed = lcLeaderboardResult.getCount() > 0;
                latch.countDown();
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
                System.out.println("failed to get results. cause: " + throwable);
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
        LCUser unauthUser = LCUser.createWithoutData(LCUser.class, "60e57e6eb8524555a2c85fd1");
        final Map<String, Double> scores = new HashMap<>();
        scores.put("leancloudgogo", (double)System.currentTimeMillis());
        LCLeaderboard.updateStatistic(unauthUser, scores).subscribe(new Observer<LCStatisticResult>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull LCStatisticResult jsonObject) {
                System.out.println("succeed to update user's statistic.");
                testSucceed = true;
                latch.countDown();
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
                System.out.println("failed to update user's statistic. cause: " + throwable);
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
