package cn.leancloud;

import cn.leancloud.core.LeanCloud;
import cn.leancloud.core.PaasClient;
import cn.leancloud.core.StorageClient;
import cn.leancloud.types.LCDate;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

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
        StorageClient storageClient = PaasClient.getStorageClient();
        storageClient.getServerTime().subscribe(new Observer<LCDate>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull LCDate lcDate) {
                System.out.println("current server time: " + lcDate.toJSONString());
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
        latch.await();
        assertTrue(testSucceed);
    }
}
