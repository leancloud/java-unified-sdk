package cn.leancloud;

import cn.leancloud.core.LeanCloud;
import cn.leancloud.types.LCDate;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import java.util.concurrent.CountDownLatch;

public class SmokingTest extends TestCase {
    private CountDownLatch latch = null;
    private boolean testSucceed = false;

    public SmokingTest(String name) {
        super(name);
        Configure.initializeRuntime();
    }

    public static Test suite() {
        return new TestSuite(SmokingTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        latch = new CountDownLatch(1);
        testSucceed = false;
    }

    public void testQueryServerDate() throws Exception {
        LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
        LeanCloud.getServerDateInBackground().subscribe(new Observer<LCDate>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(LCDate lcDate) {
                System.out.println("current server time: " + lcDate.toJSONString());
                testSucceed = true;
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
        latch.await();
        assertTrue(testSucceed);
    }
}
