package cn.leancloud.network;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.GetCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.types.AVDate;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.upload.FileUploadToken;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import io.reactivex.Observer;

public class PaasClientTest extends TestCase {
  public PaasClientTest(String testName) {
    super(testName);
  }
  public static Test suite()
  {
    return new TestSuite( PaasClientTest.class );
  }
  @Override
  protected void setUp() throws Exception {
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testCurrentTimeStamp() {
    try {
      StorageClient storageClient = PaasClient.getStorageClient();
      storageClient.getServerTime().subscribe(new Observer<AVDate>() {
        public void onSubscribe(Disposable disposable) {
        }

        public void onNext(AVDate avDate) {
          System.out.println(avDate);
        }

        public void onError(Throwable throwable) {
          System.out.println("failed! cause: " + throwable);
          fail();
        }

        public void onComplete() {

        }
      });

    } catch (Exception ex) {
      fail(ex.getMessage());
    }
  }

  public void testFetchOneObject() {
    try {
      StorageClient storageClient = PaasClient.getStorageClient();
      GetCallback callback = new GetCallback() {
        @Override
        public void done(AVObject object, AVException e) {
          if (null != e) {
            fail();
          } else {
            System.out.println("response is:" + object.toString());
            assertNotNull(object);
            System.out.println(JSON.toJSONString(object));
          }
        }
      };
      storageClient.fetchObject("Student", "5a7a4ac8128fe1003768d2b1")
              .subscribe(ObserverBuilder.buildSingleObserver(callback));
      Thread.sleep(2000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void testCreateUploadToken() {
    try {
      JSONObject fileObject = new JSONObject();
      StorageClient storageClient = PaasClient.getStorageClient();
      storageClient.newUploadToken(fileObject).subscribe(new Observer<FileUploadToken>() {
        public void onComplete() {

        }

        public void onError(Throwable throwable) {
          System.out.println("failed! cause: " + throwable);
        }

        public void onNext(FileUploadToken fileUploadToken) {
          System.out.println(fileUploadToken);
        }
        public void onSubscribe(Disposable disposable) {
          ;
        }
      });
      Thread.sleep(2000);
    } catch (Exception ex) {
      fail(ex.getMessage());
    }
  }
}
