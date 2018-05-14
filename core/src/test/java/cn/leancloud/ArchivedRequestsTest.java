package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import com.alibaba.fastjson.serializer.SerializeConfig;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Date;

public class ArchivedRequestsTest extends TestCase {
  public ArchivedRequestsTest(String name) {
    super(name);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
//    SerializeConfig.getGlobalInstance().put(AVObject.class, new ObjectTypeAdapter());
  }

  public static Test suite() {
    return new TestSuite(ArchivedRequestsTest.class);
  }

  public void testRequestSerialize() {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");

    String archivedJSON = ArchivedRequests.getArchiveContent(object, false);
    System.out.println("archivedJSON: " + archivedJSON);

    AVObject tmp = ArchivedRequests.parse2Object(archivedJSON);
    assertEquals(object.internalId(), tmp.internalId());

    tmp.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        avObject.delete();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
      }

      @Override
      public void onComplete() {

      }
    });
  }

  public void testCompoundRequestSerialize() {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.add("course", "Art");
    object.increment("age", 23);
    object.decrement("age");
    object.addUnique("course", "Math");
    String archivedJSON = ArchivedRequests.getArchiveContent(object, false);
    System.out.println("archivedJSON: " + archivedJSON);

    AVObject tmp = ArchivedRequests.parse2Object(archivedJSON);
    assertEquals(object.internalId(), tmp.internalId());
  }

  public void testRequestSerializeWithSingleObjectValue() {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.add("birthday", new Date());
    object.put("friend", AVObject.createWithoutData("Student", "fakeObjectId"));

    String archivedJSON = ArchivedRequests.getArchiveContent(object, false);
    System.out.println("archivedJSON: " + archivedJSON);

    AVObject tmp = ArchivedRequests.parse2Object(archivedJSON);
    assertEquals(object.internalId(), tmp.internalId());
    assertEquals(object.operations.size(), tmp.operations.size());
  }

  public void testRequestSerializeWithObjectValueArray() {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.add("birthday", new Date());
    object.add("friend", AVObject.createWithoutData("Student", "fakeObjectId"));

    String archivedJSON = ArchivedRequests.getArchiveContent(object, false);
    System.out.println("archivedJSON: " + archivedJSON);

    AVObject tmp = ArchivedRequests.parse2Object(archivedJSON);
    assertEquals(object.internalId(), tmp.internalId());
    assertEquals(object.operations.size(), tmp.operations.size());
  }
}
