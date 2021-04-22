package cn.leancloud;

import cn.leancloud.ops.AddOperation;
import cn.leancloud.ops.BaseOperation;
import cn.leancloud.ops.BitAndOperation;
import cn.leancloud.ops.SetOperation;
import cn.leancloud.json.JSON;
import cn.leancloud.json.TypeReference;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ArchivedRequestsTest extends TestCase {
  private boolean testSucceed = false;
  private CountDownLatch latch = null;

  public ArchivedRequestsTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(ArchivedRequestsTest.class);
  }

  public void testFileVerify() {
    String[] files = {".DS_Store", "5b028f2f17d009726f2ac391", "e87e8586-fe2a-4e24-a632-ff8ff39585eb", "-a632-ff8ff39585eb"};
    boolean[] results = {false, true, true, false};
    for (int i = 0; i < files.length; i++) {
      System.out.println("verify " + files[i]);
      boolean ret = LCObject.verifyInternalId(files[i]);
      assertEquals(results[i], ret);
    }
  }

  public void testOperationSerialize() {
    List<BaseOperation> ops = new ArrayList<>();
    SetOperation setOp = new SetOperation("age", 3);
    ops.add(setOp);
    AddOperation addOp = new AddOperation("course", "Computer Science");
    ops.add(addOp);
    BitAndOperation bitAndOp = new BitAndOperation("score", 0x002);
    ops.add(bitAndOp);
    String opString = JSON.toJSONString(ops);

    System.out.println(opString);

    List<BaseOperation> parsedOps = JSON.parseObject(opString,
            new TypeReference<List<BaseOperation>>() {});
    assertEquals(ops.size(), parsedOps.size());
  }

  public void testOperationSerialize2() {
    String opString = "[{\"field\":\"name\",\"final\":true,\"operation\":\"Set\", \"value\":\"Mike\"},{\"field\":\"age\",\"final\":true,\"operation\":\"Set\",\"value\":12}]";
    List<BaseOperation> parsedOps = JSON.parseObject(opString,
            new TypeReference<List<BaseOperation>>() {});
    assertEquals(2, parsedOps.size());
  }

  public void testRequestSerialize() throws Exception {
    testSucceed = false;
    latch = new CountDownLatch(1);

    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");

    String archivedJSON = ArchivedRequests.getArchiveContent(object, false);

    LCObject tmp = ArchivedRequests.parseAVObject(archivedJSON);
    assertEquals(object.internalId(), tmp.internalId());

    tmp.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        LCObject.delete();
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

  public void testCompoundRequestSerialize() {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.add("course", "Art");
    object.increment("age", 23);
    object.decrement("age");
    object.addUnique("course", "Math");
    String archivedJSON = ArchivedRequests.getArchiveContent(object, false);

    LCObject tmp = ArchivedRequests.parseAVObject(archivedJSON);
    assertEquals(object.internalId(), tmp.internalId());
  }

  public void testRequestSerializeWithSingleObjectValue() {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.add("birthday", new Date());
    object.put("friend", LCObject.createWithoutData("Student", "fakeObjectId"));

    String archivedJSON = ArchivedRequests.getArchiveContent(object, false);

    LCObject tmp = ArchivedRequests.parseAVObject(archivedJSON);
    assertEquals(object.internalId(), tmp.internalId());
    assertEquals(object.operations.size(), tmp.operations.size());
  }

  public void testRequestSerializeWithObjectValueArray() {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.add("birthday", new Date());
    object.add("friend", LCObject.createWithoutData("Student", "fakeObjectId"));

    String archivedJSON = ArchivedRequests.getArchiveContent(object, false);
    System.out.println("archived jsonString: " + archivedJSON);

    LCObject tmp = ArchivedRequests.parseAVObject(archivedJSON);
    assertEquals(object.internalId(), tmp.internalId());
    assertEquals(object.operations.size(), tmp.operations.size());
  }
}
