package cn.leancloud;

import cn.leancloud.types.AVNull;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

public class AVFileTest extends TestCase {
  private boolean testSucceed = false;
  private CountDownLatch latch = null;
  public AVFileTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AVFileTest.class);
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

  public void testCreateWithObjectId() throws Exception {
    String url = "http://i1.wp.com/blog.avoscloud.com/wp-content/uploads/2014/05/screen568x568-1.jpg?resize=202%2C360";
    AVFile file = new AVFile("screen.jpg", url);
    file.save();

    final String fileObjectId = file.getObjectId();
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
        testSucceed = url.length() > 0 && thumbnailUrl.length() > 0 && name.length() > 0 && key.length() > 0;
        testSucceed = testSucceed && objectId.equals(fileObjectId);
        testSucceed = testSucceed && (mimeType.length() > 0);
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(testSucceed);
    file.delete();
  }

  public void testCreateWithExtension() throws Exception {
    File localFile = new File("./20160704174809.jpeg");
    AVFile file = new AVFile("test.jpeg", localFile);
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
            testSucceed = true;
            latch.countDown();
          }

          public void onError(Throwable throwable) {
            System.out.println("[Thread:" + Thread.currentThread().getId() +"] failed to delete file.");
            throwable.printStackTrace();
            latch.countDown();
          }

          public void onComplete() {

          }
        });
      }

      public void onError(Throwable throwable) {
        System.out.println("[Thread:" + Thread.currentThread().getId() + "] failed to upload file.");
        throwable.printStackTrace();
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testBase64Data() throws Exception {
    String contents = StringUtil.getRandomString(640);
    AVFile file = new AVFile("testfilename", contents.getBytes());
//    Map<String, Object> metaData = new HashMap<>();
//    metaData.put("format", "dat file");
//    file.setMetaData(metaData);
    file.setACL(new AVACL());
    file.saveInBackground().subscribe(new Observer<AVFile>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVFile avFile) {
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
    assertTrue(file.getObjectId().length() > 0);
  }

  public void testSaveDataWithKeepFileName() throws Exception {
    String contents = StringUtil.getRandomString(640);
    AVFile file = new AVFile("testfilename", contents.getBytes());
//    Map<String, Object> metaData = new HashMap<>();
//    metaData.put("format", "dat file");
//    file.setMetaData(metaData);
    file.setACL(new AVACL());
    file.saveInBackground(true).subscribe(new Observer<AVFile>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVFile avFile) {
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
    assertTrue(file.getObjectId().length() > 0);
  }

  public void testLocalFileWithKeepFileName() throws Exception {
    File currentFile = new File("./20160704174809.jpeg");
    AVFile file = new AVFile("20160704174809.jpeg", currentFile);
    file.saveInBackground(true).subscribe(new Observer<AVFile>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVFile avFile) {
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

  public void testLocalFileWithoutKeepFileName() throws Exception {
    File currentFile = new File("./20160704174809.jpeg");
    AVFile file = new AVFile("20160704174809.jpeg", currentFile);
    file.saveInBackground().subscribe(new Observer<AVFile>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVFile avFile) {
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

  public void testExternalFile2() throws Exception {
    String url = "http://i1.wp.com/blog.avoscloud.com/wp-content/uploads/2014/05/screen568x568-1.jpg?resize=202%2C360";
    AVFile file = new AVFile("screen.jpg", url, null);
    file.saveInBackground().subscribe(new Observer<AVFile>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVFile avFile) {
        System.out.println(avFile.toJSONString());
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

  public void testExternalFile3() throws Exception {
    String url = "https://some.website.com/apple.acc";
    AVFile file = new AVFile("screen.jpg", url, null);
    file.saveInBackground(true)
            .subscribeOn(Schedulers.io())
            .subscribe(new Observer<AVFile>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVFile avFile) {
        System.out.println(avFile.toJSONString());
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

  public void testExternalFile() throws Exception {
    AVFile portrait = new AVFile("thumbnail", "https://tvax1.sinaimg.cn/crop.0.0.200.200.180/a8d43f7ely1fnxs86j4maj205k05k74f.jpg");
    portrait.saveInBackground().subscribe(new Observer<AVFile>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVFile avFile) {
        avFile.delete();
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

  public void testWxThumbnailFile() throws Exception {
    AVFile portrait = new AVFile("thumbnail", "http://thirdwx.qlogo.cn/mmopen/vi_32/zxVN0QqibgaibNf8ia7y4ugUJMbia0Zt6QPHh1ymUNBrIgsGfMd7WyvzMVPa9aeA6pbIB7ePEaQ7jO4BJr21howXDw/132");
    portrait.saveInBackground().subscribe(new Observer<AVFile>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVFile avFile) {
        avFile.delete();
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

  public void testDownloadExternalFile() throws Exception {
    AVFile portrait = new AVFile("thumbnail", "http://file.everydaydiary.luyunxinchen.cn/437K25F9DpoWnJcJgbQECCV994ntJKpCGGudo6af.png");
    byte[] contents = portrait.getData();
    System.out.println("data length:" + contents.length);
    assertTrue(contents.length == 80830);
  }

  public void testDownloadExternalFileUnderAsyncMode() throws Exception {
    AVFile portrait = new AVFile("thumbnail", "http://file.everydaydiary.luyunxinchen.cn/437K25F9DpoWnJcJgbQECCV994ntJKpCGGudo6af.png");
    portrait.getDataInBackground().subscribe(new Observer<byte[]>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(byte[] bytes) {
        System.out.println("data length:" + bytes.length);
        testSucceed = (bytes.length == 80830);
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

  public void testGetDataStream() throws Exception {
    AVFile portrait = new AVFile("thumbnail", "http://file.everydaydiary.luyunxinchen.cn/437K25F9DpoWnJcJgbQECCV994ntJKpCGGudo6af.png");
    InputStream is = portrait.getDataStream();
    byte[] buffer = new byte[102400];
    int length = is.read(buffer);
    System.out.println("data length:" + length);
    is.close();
    assertTrue(80830 == length);
  }

  public void testGetDataStreamUnderAsyncMode() throws Exception {
    AVFile portrait = new AVFile("thumbnail", "http://file.everydaydiary.luyunxinchen.cn/437K25F9DpoWnJcJgbQECCV994ntJKpCGGudo6af.png");
    portrait.getDataStreamInBackground().subscribe(new Observer<InputStream>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(InputStream inputStream) {
        byte[] buffer = new byte[102400];
        try {
          int length = inputStream.read(buffer);
          System.out.println("data length:" + length);
          testSucceed = length == 80830;
          inputStream.close();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
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

  public void testGetDataStreamWithNotexistFile() throws Exception {
    try {
      File currentFile = new File("./notexistedfile.jpeg");
      AVFile file = new AVFile("20160704174809.jpeg", currentFile);
      InputStream is = file.getDataStream();
      assertTrue(is == null);
    } catch (Exception ex) {
      ;
    }
  }

  public void testGetDataStreamWithLocalFile() throws Exception {
    File currentFile = new File("./20160704174809.jpeg");
    AVFile file = new AVFile("20160704174809.jpeg", currentFile);
    InputStream is = file.getDataStream();
    byte[] buffer = new byte[102400];
    int length = is.read(buffer);
    System.out.println("data length:" + length);
    is.close();
    assertTrue(length > 0);
  }

  public void testGetDataStreamWithLocalFileUnderAsyncMode() throws Exception {
    File currentFile = new File("./20160704174809.jpeg");
    AVFile file = new AVFile("20160704174809.jpeg", currentFile);
    file.getDataStreamInBackground().subscribe(new Observer<InputStream>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(InputStream is) {
        byte[] buffer = new byte[102400];
        try {
          int length = is.read(buffer);
          System.out.println("data length:" + length);
          is.close();
          testSucceed = length > 0;
        } catch (Exception ex) {
          ex.printStackTrace();
        }
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

  public void testBlockSave() throws Exception {
    AVFile leanFile = new AVFile("name.txt", "name".getBytes());
    leanFile.save();
  }

  public void testUploader() throws Exception {
    String contents = StringUtil.getRandomString(640);
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
            testSucceed = true;
            latch.countDown();
          }

          public void onError(Throwable throwable) {
            latch.countDown();
          }

          public void onComplete() {

          }
        });
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
}
