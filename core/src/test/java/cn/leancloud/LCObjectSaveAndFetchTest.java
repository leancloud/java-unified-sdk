package cn.leancloud;

import cn.leancloud.core.LeanCloud;
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

public class LCObjectSaveAndFetchTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public LCObjectSaveAndFetchTest(String testName) {
    super(testName);
    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(LCObjectSaveAndFetchTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
    LeanCloud.setAutoMergeOperationDataWhenSave(true);
  }

  @Override
  protected void tearDown() throws Exception {
    LeanCloud.setAutoMergeOperationDataWhenSave(false);
  }

  public void testSimpleAttributes() throws Exception {
    final Date now = new Date();
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("lastOcc", now);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println(LCObject.toJSONString());
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
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("lastOcc", now);
    object.addAll("course", courses);
    object.addUnique("uniqueAttr", "uniqueAttr");
    object.remove("removedAttr");
    object.bitAnd("age", 4);
    object.bitAnd("otherIntAttr", 5);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println(LCObject.toJSONString());
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
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("lastOcc", now);
    object.addAll("course", courses);
    object.addUnique("uniqueAttr", "uniqueAttr");
    object.remove("removedAttr");
    object.bitAnd("age", 4);
    object.increment("otherIntAttr", 5);
    LCSaveOption option = new LCSaveOption();
    option.setFetchWhenSave(true);
    object.saveInBackground(option).subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println(LCObject.toJSONString());
        LCObject.increment("age", 3); // 21
        LCObject.add("course", "D");  // A,B,C,D
        LCObject.bitXor("otherIntAttr", 2); // 7
        LCObject.put("newAttr", "newAttr");
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject tmp) {
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
    LeanCloud.setAutoMergeOperationDataWhenSave(false);
    final Date now = new Date();
    List<String> courses = Arrays.asList("A", "B", "C");
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("lastOcc", now);
    object.addAll("course", courses);
    object.addUnique("uniqueAttr", "uniqueAttr");
    object.remove("removedAttr");
    object.bitAnd("age", 4);
    object.increment("otherIntAttr", 5);
    LCSaveOption option = new LCSaveOption();
    option.setFetchWhenSave(true);
    object.saveInBackground(option).subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println(LCObject.toJSONString());
        LCObject.increment("age", 3); // 21
        LCObject.add("course", "D");  // A,B,C,D
        LCObject.bitXor("otherIntAttr", 2); // 7
        LCObject.put("newAttr", "newAttr");
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject tmp) {
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
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("lastOcc", now);
    object.addAll("course", courses);
    object.addUnique("uniqueAttr", "uniqueAttr");
    object.remove("removedAttr");
    object.bitAnd("age", 4);
    object.increment("otherIntAttr", 5);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println(LCObject.toJSONString());
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
    LCFile file = new LCFile("screen.jpg", url);
    file.save();
    System.out.println("file-------- " + file.toJSONString());

    final Date now = new Date();
    List<String> courses = Arrays.asList("A", "B", "C");
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("avatar", file);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println(LCObject.toJSONString());
        testSucceed = url.equals(LCObject.getAVFile("avatar").getUrl());
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
    LCFile file = new LCFile("screen.jpg", url);
    file.save();
    System.out.println("file-------- " + file.toJSONString());

    final Date now = new Date();
    List<String> courses = Arrays.asList("A", "B", "C");
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("avatar", file);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println(LCObject.toJSONString());
        testSucceed = url.equals(LCObject.getAVFile("avatar").getUrl());
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
    final LCFile file = new LCFile("test.jpeg", localFile);
    file.save();
    System.out.println("file-------- " + file.toJSONString());

    final Date now = new Date();
    List<String> courses = Arrays.asList("A", "B", "C");
    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("avatar", file);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println(LCObject.toJSONString());
        testSucceed = file.getUrl().equals(LCObject.getAVFile("avatar").getUrl());
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
    final LCFile file = new LCFile("test.jpeg", localFile);
    file.save();
    System.out.println("file-------- " + file.toJSONString());

    final LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("avatar", file);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println(LCObject.toJSONString());
        testSucceed = LCObject.getAVFile("avatar") != null
                && file.getUrl().equals(LCObject.getAVFile("avatar").getUrl());
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
