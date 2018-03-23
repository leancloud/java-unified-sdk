package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.types.AVNull;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AVFileTest extends TestCase {
  public AVFileTest(String name) {
    super(name);
//    PaasClient.config(true, new PaasClient.SchedulerCreator() {
//      public Scheduler create() {
//        return Schedulers.newThread();
//      }
//    });
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public static Test suite() {
    return new TestSuite(AVFileTest.class);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
    ;
  }

  public void testCreateWithObjectId() {
    String fileObjectId = "5aa634357565710044bde4df";
    AVFile.withObjectIdInBackground(fileObjectId).subscribe(new Observer<AVFile>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVFile avFile) {
        System.out.println(avFile);
        String url = avFile.getUrl();
        String name = avFile.getName();
        String key = avFile.getKey();
        int size = avFile.getSize();
        String objectId = avFile.getObjectId();
        String thumbnailUrl = avFile.getThumbnailUrl(true, 200, 200);
        String mimeType = avFile.getMimeType();
        System.out.println("url=" + url + ", name=" + name + ", key=" + key + ", size=" + size);
        System.out.println("objId=" + objectId + ", thumbnail=" + thumbnailUrl + ", mime=" + mimeType);
        assertTrue(url.length() > 0 && thumbnailUrl.length() > 0 && name.length() > 0 && key.length() > 0);
        assertTrue(size > 0 && objectId.equals("5aa634357565710044bde4df"));
        assertTrue(mimeType.length() > 0);
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {
      }
    });
  }

  public void testUploader() {
    String contents = StringUtil.getRandomString(64);
    AVFile file = new AVFile("test", contents.getBytes());
    Observable<AVFile> result = file.saveInBackground();
    result.subscribe(new Observer<AVFile>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVFile avFile) {
        System.out.println("[Thread:" + Thread.currentThread().getId() +
                "]succeed to upload file. objectId=" + avFile.getObjectId());
        avFile.deleteInBackground().subscribe(new Observer<AVNull>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(AVNull aVoid) {
            System.out.println("[Thread:" + Thread.currentThread().getId() + "]succeed to delete file.");
          }

          public void onError(Throwable throwable) {
            fail();
          }

          public void onComplete() {

          }
        });
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }
}
