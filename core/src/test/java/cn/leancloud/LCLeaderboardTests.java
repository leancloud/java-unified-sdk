package cn.leancloud;

import cn.leancloud.core.LeanCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LCLeaderboardTests extends TestCase {
    private boolean testSucceed = false;
    private CountDownLatch latch = null;
    public LCLeaderboardTests(String name) {
        super(name);
        Configure.initializeWithApp("dY107uv8CYXDIve1V9",
                "hOx6DFyU9pXmZDDbNL4piUFto2LJGYp9UcfJFcaQ",
                "dy107uv8.cloud.tds1.tapapis.cn");
        LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    }

    public static Test suite() {
        return new TestSuite(LCLeaderboardTests.class);
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

    public void testResultParse() throws Exception {
        LCLeaderboard leaderboard = LCLeaderboard.createWithoutData("LearderBoard_Test");
        int skip = 0;
        int limit = 10;

        List<String> selectMemberKeys=new ArrayList<>();
        selectMemberKeys.add("username");
        List<String> includeStatistics=new ArrayList<>();
        includeStatistics.add("LeaderBoard_Test_Also");

        leaderboard.getResults(skip, limit, selectMemberKeys, includeStatistics)
                .subscribe(new Observer<LCLeaderboardResult>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                    }
                    @Override
                    public void onNext(LCLeaderboardResult leaderboardResult) {
                        // rankings是类的成员变量，用于其他方法访问
                        // 获取的到的数值请见文档末尾
                        List<LCRanking> rankings = leaderboardResult.getResults();
                        // X获取的结果为Null
                        List<LCStatistic> X = rankings.get(0).getStatistics();
                        // process rankings
                        // 调用获取成功处理...
                        if (null != X) {
                            for (LCStatistic statistic: X) {
                                System.out.println(statistic.toString());
                            }
                        }

                        // ...
                        testSucceed = true;
                        latch.countDown();
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        // handle error
                        // 调用获取失败处理...
                        // ...
                        latch.countDown();
                    }
                    @Override
                    public void onComplete() {
                    }
                });
        latch.await();
        assertTrue(testSucceed);
    }

    public void testUserGroupResultParse() throws Exception {
        LCLeaderboard leaderboard = LCLeaderboard.createWithoutData("LearderBoard_Test");
        List<String> userIds=new ArrayList<>();
        userIds.add("LeaderBoard_Test_1");
        userIds.add("LeaderBoard_Test_Also");

        leaderboard.queryGroupStatistics(userIds)
                .subscribe(new Observer<LCStatisticResult>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                    }
                    @Override
                    public void onNext(LCStatisticResult leaderboardResult) {
                        // X获取的结果为Null
                        List<LCStatistic> X = leaderboardResult.getResults();
                        // process rankings
                        // 调用获取成功处理...
                        if (null != X) {
                            for (LCStatistic statistic: X) {
                                System.out.println(statistic.toString());
                            }
                        }

                        // ...
                        testSucceed = true;
                        latch.countDown();
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        // handle error
                        // 调用获取失败处理...
                        // ...
                        latch.countDown();
                    }
                    @Override
                    public void onComplete() {
                    }
                });
        latch.await();
        assertTrue(testSucceed);
    }

    public void testObjectGroupResultParse() throws Exception {
        LCLeaderboard leaderboard = LCLeaderboard.createWithoutData("LearderBoard_Test", LCLeaderboard.MEMBER_TYPE_OBJECT);
        List<String> targetIds=new ArrayList<>();
        targetIds.add("LeaderBoard_Test_1");
        targetIds.add("LeaderBoard_Test_Also");

        leaderboard.queryGroupStatistics(targetIds)
                .subscribe(new Observer<LCStatisticResult>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                    }
                    @Override
                    public void onNext(LCStatisticResult leaderboardResult) {
                        // X获取的结果为Null
                        List<LCStatistic> X = leaderboardResult.getResults();
                        // process rankings
                        // 调用获取成功处理...
                        if (null != X) {
                            for (LCStatistic statistic: X) {
                                System.out.println(statistic.toString());
                            }
                        }

                        // ...
                        testSucceed = true;
                        latch.countDown();
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        // handle error
                        // 调用获取失败处理...
                        // ...
                        latch.countDown();
                    }
                    @Override
                    public void onComplete() {
                    }
                });
        latch.await();
        assertTrue(testSucceed);
    }

    public void testEntityGroupResultParse() throws Exception {
        LCLeaderboard leaderboard = LCLeaderboard.createWithoutData("LearderBoard_Test", LCLeaderboard.MEMBER_TYPE_ENTITY);
        List<String> targetIds=new ArrayList<>();
        targetIds.add("LeaderBoard_Test_1");
        targetIds.add("LeaderBoard_Test_Also");

        leaderboard.queryGroupStatistics(targetIds)
                .subscribe(new Observer<LCStatisticResult>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                    }
                    @Override
                    public void onNext(LCStatisticResult leaderboardResult) {
                        // X获取的结果为Null
                        List<LCStatistic> X = leaderboardResult.getResults();
                        // process rankings
                        // 调用获取成功处理...
                        if (null != X) {
                            for (LCStatistic statistic: X) {
                                System.out.println(statistic.toString());
                            }
                        }

                        // ...
                        testSucceed = true;
                        latch.countDown();
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        // handle error
                        // 调用获取失败处理...
                        // ...
                        System.out.println(throwable);
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
