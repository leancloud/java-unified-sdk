package cn.leancloud;

import cn.leancloud.auth.UserBasedTestCase;
import cn.leancloud.json.JSONObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        LCLeaderboard.updateStatistic(LCUser.currentUser(), scores).subscribe(new Observer<JSONObject>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull JSONObject jsonObject) {
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
        LCLeaderboard.updateStatistic(LCUser.currentUser(), scores).subscribe(new Observer<JSONObject>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull JSONObject jsonObject) {
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

    public void testUpdateAndGetStatistic() throws Exception {
        final LCLeaderboard leaderboard = LCLeaderboard.fetchByName(LEADERBOARD_NAME).blockingFirst();
        Map<String, Double> scores = new HashMap<>();
        scores.put("leancloudgogo", (double)System.currentTimeMillis());
        LCLeaderboard.updateStatistic(LCUser.currentUser(), scores).subscribe(new Observer<JSONObject>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull JSONObject jsonObject) {
                leaderboard.getResults(0, 100, null, null).subscribe(new Observer<List<LCRanking>>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable disposable) {

                    }

                    @Override
                    public void onNext(@NotNull List<LCRanking> lcRankings) {
                        testSucceed = true;
                        latch.countDown();
                    }

                    @Override
                    public void onError(@NotNull Throwable throwable) {
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