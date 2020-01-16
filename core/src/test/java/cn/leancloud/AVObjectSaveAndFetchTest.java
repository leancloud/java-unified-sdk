package cn.leancloud;

import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVObjectSaveAndFetchTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public AVObjectSaveAndFetchTest(String testName) {
    super(testName);
    AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(AVObjectSaveAndFetchTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
    AVOSCloud.setAutoMergeOperationDataWhenSave(true);
  }

  @Override
  protected void tearDown() throws Exception {
    AVOSCloud.setAutoMergeOperationDataWhenSave(false);
  }

  public void testSimpleAttributes() throws Exception {
    final Date now = new Date();
    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("lastOcc", now);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println(avObject.toJSONString());
        testSucceed = (18 == object.getInt("age"))
                && "Automatic Tester".equals(object.getString("name"))
                && now.getTime() == object.getDate("lastOcc").getTime()
                && object.getObjectId().length() > 0
                && null != object.getCreatedAt();
        object.delete();
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

  public void testComplexAttributes() throws Exception {
    final Date now = new Date();
    List<String> courses = Arrays.asList("A", "B", "C");
    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("lastOcc", now);
    object.addAll("course", courses);
    object.addUnique("uniqueAttr", "uniqueAttr");
    object.remove("removedAttr");
    object.bitAnd("age", 4);
    object.bitAnd("otherIntAttr", 5);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println(avObject.toJSONString());
        testSucceed = (18 == object.getInt("age"))
                && "Automatic Tester".equals(object.getString("name"))
                && now.getTime() == object.getDate("lastOcc").getTime()
                && object.getList("course").size() == 3
                && object.getList("uniqueAttr").size() == 1
                && object.get("removedAttr") == null
                && object.getInt("otherIntAttr") == 0
                && object.getObjectId().length() > 0
                && null != object.getCreatedAt();
        object.delete();
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

  public void testObjectUpdate() throws Exception {
    final Date now = new Date();
    List<String> courses = Arrays.asList("A", "B", "C");
    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("lastOcc", now);
    object.addAll("course", courses);
    object.addUnique("uniqueAttr", "uniqueAttr");
    object.remove("removedAttr");
    object.bitAnd("age", 4);
    object.increment("otherIntAttr", 5);
    AVSaveOption option = new AVSaveOption();
    option.setFetchWhenSave(true);
    object.saveInBackground(option).subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println(avObject.toJSONString());
        avObject.increment("age", 3); // 21
        avObject.add("course", "D");  // A,B,C,D
        avObject.bitXor("otherIntAttr", 2); // 7
        avObject.put("newAttr", "newAttr");
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject tmp) {
            System.out.println(tmp.toJSONString());
            testSucceed = (21 == tmp.getInt("age"))
                    && "Automatic Tester".equals(tmp.getString("name"))
                    && now.getTime() == tmp.getDate("lastOcc").getTime()
                    && tmp.getList("course").size() == 4
                    && tmp.getList("uniqueAttr").size() == 1
                    && "newAttr".equals(tmp.get("newAttr"))
                    && tmp.getInt("otherIntAttr") == 7
                    && tmp.getObjectId().length() > 0
                    && null != tmp.getUpdatedAt();
            tmp.delete();
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            object.delete();
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

  public void testObjectUpdateWithoutAutoMergeFlag() throws Exception {
    AVOSCloud.setAutoMergeOperationDataWhenSave(false);
    final Date now = new Date();
    List<String> courses = Arrays.asList("A", "B", "C");
    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("lastOcc", now);
    object.addAll("course", courses);
    object.addUnique("uniqueAttr", "uniqueAttr");
    object.remove("removedAttr");
    object.bitAnd("age", 4);
    object.increment("otherIntAttr", 5);
    AVSaveOption option = new AVSaveOption();
    option.setFetchWhenSave(true);
    object.saveInBackground(option).subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println(avObject.toJSONString());
        avObject.increment("age", 3); // 21
        avObject.add("course", "D");  // A,B,C,D
        avObject.bitXor("otherIntAttr", 2); // 7
        avObject.put("newAttr", "newAttr");
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject tmp) {
            System.out.println(tmp.toJSONString());
            testSucceed = (18 == tmp.getInt("age"))
                    && "Automatic Tester".equals(tmp.getString("name"))
                    && now.getTime() == tmp.getDate("lastOcc").getTime()
                    && tmp.getList("course").size() == 3
                    && tmp.getList("uniqueAttr").size() == 1
                    && !"newAttr".equals(tmp.get("newAttr"))
                    && tmp.getInt("otherIntAttr") == 5
                    && tmp.getObjectId().length() > 0
                    && null != tmp.getUpdatedAt();
            tmp.delete();
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            object.delete();
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

  public void testComplexAttributesWithFetchFlag() throws Exception {
    final Date now = new Date();
    List<String> courses = Arrays.asList("A", "B", "C");
    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("lastOcc", now);
    object.addAll("course", courses);
    object.addUnique("uniqueAttr", "uniqueAttr");
    object.remove("removedAttr");
    object.bitAnd("age", 4);
    object.increment("otherIntAttr", 5);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println(avObject.toJSONString());
        testSucceed = (18 == object.getInt("age"))
                && "Automatic Tester".equals(object.getString("name"))
                && now.getTime() == object.getDate("lastOcc").getTime()
                && object.getList("course").size() == 3
                && object.getList("uniqueAttr").size() == 1
                && object.get("removedAttr") == null
                && object.getInt("otherIntAttr") == 5
                && object.getObjectId().length() > 0
                && null != object.getCreatedAt();
        object.delete();
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

  public void testExternalFileAttribute() throws Exception {
    final String url = "http://i1.wp.com/blog.avoscloud.com/wp-content/uploads/2014/05/screen568x568-1.jpg?resize=202%2C360";
    AVFile file = new AVFile("screen.jpg", url);
    file.save();
    System.out.println("file-------- " + file.toJSONString());

    final Date now = new Date();
    List<String> courses = Arrays.asList("A", "B", "C");
    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("avatar", file);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println(avObject.toJSONString());
        testSucceed = url.equals(avObject.getAVFile("avatar").getUrl());
        object.delete();
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
    file.delete();
    assertTrue(testSucceed);
  }

  public void testExternalFileAttributeWithFetchFlag() throws Exception {
    final String url = "http://i1.wp.com/blog.avoscloud.com/wp-content/uploads/2014/05/screen568x568-1.jpg?resize=202%2C360";
    AVFile file = new AVFile("screen.jpg", url);
    file.save();
    System.out.println("file-------- " + file.toJSONString());

    final Date now = new Date();
    List<String> courses = Arrays.asList("A", "B", "C");
    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("avatar", file);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println(avObject.toJSONString());
        testSucceed = url.equals(avObject.getAVFile("avatar").getUrl());
        object.delete();
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
    file.delete();
    assertTrue(testSucceed);
  }

  public void testLocalFileAttribute() throws Exception {
    File localFile = new File("./20160704174809.jpeg");
    final AVFile file = new AVFile("test.jpeg", localFile);
    file.save();
    System.out.println("file-------- " + file.toJSONString());

    final Date now = new Date();
    List<String> courses = Arrays.asList("A", "B", "C");
    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("avatar", file);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println(avObject.toJSONString());
        testSucceed = file.getUrl().equals(avObject.getAVFile("avatar").getUrl());
        object.delete();
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
    file.delete();
    assertTrue(testSucceed);
  }

  public void testLocalFileAttributeWithFetchFlag() throws Exception {
    File localFile = new File("./20160704174809.jpeg");
    final AVFile file = new AVFile("test.jpeg", localFile);
    file.save();
    System.out.println("file-------- " + file.toJSONString());

    final AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("avatar", file);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println(avObject.toJSONString());
        testSucceed = avObject.getAVFile("avatar") != null
                && file.getUrl().equals(avObject.getAVFile("avatar").getUrl());
        object.delete();
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
    file.delete();
    assertTrue(testSucceed);
  }


}
