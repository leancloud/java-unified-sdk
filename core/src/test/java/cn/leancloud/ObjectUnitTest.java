package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ObjectUnitTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public ObjectUnitTest(String testName) {
    super(testName);

    LCObject.registerSubclass(Armor.class);
    Configure.initializeRuntime();
    LCUser.changeCurrentUser(null, true);
  }
  public static Test suite() {
    return new TestSuite(ObjectUnitTest.class);
  }

  private void assertObject(LCObject LCObject, Date date) {
    assertEquals(1, LCObject.getInt("number"));
    assertEquals("testSaveGetDeleteGet", LCObject.getString("name"));
    assertEquals(date, LCObject.getDate("now"));
  }

  public void testSaveGetDeleteGet() throws Exception {
    final LCObject LCObject = new LCObject("ObjectUnitTest");
    assertTrue(LCObject.getObjectId().isEmpty());
    LCObject.put("number", 1);
    LCObject.put("name", "testSaveGetDeleteGet");
    Date date = new Date();
    LCObject.put("now", date);
    LCObject.setFetchWhenSave(true);
    LCObject.save();
    assertFalse(LCObject.getObjectId().isEmpty());

    assertObject(LCObject, date);

    // Try to get it
    LCObject cloudObject = LCObject.createWithoutData("ObjectUnitTest", LCObject.getObjectId());
    cloudObject = cloudObject.fetchIfNeeded();
    assertNotNull(cloudObject);
    assertObject(cloudObject, date);

    // Delete it
    cloudObject.delete();

    LCObject tmp = LCObject.createWithoutData("ObjectUnitTest", LCObject.getObjectId()).fetchIfNeeded();
    assertNull(tmp.getDate("now"));
  }

  public void testSaveWithWrongColumnType() {
    final LCObject LCObject = new LCObject("ObjectUnitTest");
    assertTrue(LCObject.getObjectId().isEmpty());
    LCObject.put("number", 1);
    LCObject.put("name", "testSaveGetDeleteGet");
    LCObject.put("now", 1);

    try {
      LCObject.save();
      fail();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  public void testSaveWithWrongTypePointer() throws Exception {
    final LCObject LCObject = new LCObject("ObjectUnitTest");
    assertTrue(LCObject.getObjectId().isEmpty());
    LCObject.put("number", 1);
    LCObject.put("name", "testSaveGetDeleteGet");
    LCObject.put("parent", LCObject.createWithoutData("ObjectUnitTest", "5ad6e79d9f545400457ff851"));
    LCObject.save();

    // try to save with wrong pointer type
    LCObject LCObject1 = new LCObject("ObjectUnitTest");
    assertTrue(LCObject1.getObjectId().isEmpty());
    LCObject1.put("number", 1);
    LCObject1.put("name", "testSaveGetDeleteGet");
    LCObject1.put("now", 1);
    // unknown type
    LCObject1.put("parent", new LCObject("UnknowType"));

    try {
      LCObject1.save();
      fail();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  public void testUpdateCounter() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;

    LCObject LCObject = new LCQuery<LCObject>("ObjectUnitTest").getFirstInBackground().blockingFirst();
    assertNotNull(LCObject);
    final int oldNumber = LCObject.getInt("number");
    LCObject.increment("number");
    LCObject.saveInBackground().blockingSubscribe();

    new LCQuery("ObjectUnitTest").getInBackground(LCObject.getObjectId()).subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject o) {
        int newValue = o.getInt("number");
        testSucceed = (oldNumber +1 )== newValue;
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


//  public void testUpdateCounterFetchWhenSave() throws Exception {
//    try {
//      AVObject avObject = new AVQuery("ObjectUnitTest").getFirst();
//      avObject.put("number", 10);
//      avObject.save();
//      assertNotNull(avObject);
//      final String id = avObject.getObjectId();
//      final ConcurrentLinkedQueue<AVObject> set = new ConcurrentLinkedQueue<AVObject>();
//
//      final CyclicBarrier barrier = new CyclicBarrier(50);
//      for (int i = 50; i > 0; i--) {
//        new Thread() {
//          @Override
//          public void run() {
//            try {
//              AVQuery query = new AVQuery("ObjectUnitTest");
//              AVObject testObject = query.get(id);
//              testObject.setFetchWhenSave(true);
//              testObject.increment("number", -1);
//              testObject.save();
//              set.add(testObject);
//              barrier.await();
//            } catch (Exception e) {
//              throw new RuntimeException(e);
//            }
//          }
//        }.start();
//      }
//      if (barrier.await() == 0) {
//        assertEquals(50, set.size());
//        int gots = 0;
//        for (AVObject obj : set) {
//          if (obj.getInt("number") >= 0) {
//            gots++;
//          }
//        }
//        if (gots != 10) {
//          System.out.println("fuck");
//        }
//        assertEquals(10, gots);
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }


  public void testRefreshFetchWithPointer() throws Exception {
    LCObject obj = new LCObject("ObjectUnitTest");
    long now = System.currentTimeMillis();
    obj.put("number", now);
    final LCObject LCObject = new LCObject("ObjectUnitTest");
    LCObject.put("pointer", obj);
    LCObject.save();

    LCObject theObject = LCObject.createWithoutData("ObjectUnitTest", LCObject.getObjectId());
    theObject.refresh("pointer");
    LCObject pointer = theObject.getAVObject("pointer");
    assertEquals(now, pointer.getLong("number"));

    // test fetch
    theObject = LCObject.createWithoutData("ObjectUnitTest", LCObject.getObjectId());
    theObject = theObject.fetch("pointer");
    pointer = theObject.getAVObject("pointer");
    assertEquals(now, pointer.getLong("number"));
  }

  public void testSaveWithPointerArrayAndQuryInclude() throws Exception {
    LCObject obj = new LCQuery("ObjectUnitTest").getFirst();
    final LCObject LCObject = new LCObject("ObjectUnitTest");
    LCObject
            .add("pointerArray", LCObject.createWithoutData("ObjectUnitTest", obj.getObjectId()));
    LCObject
            .add("pointerArray", LCObject.createWithoutData("ObjectUnitTest", obj.getObjectId()));
    LCObject.save();

    LCQuery query = new LCQuery("ObjectUnitTest");
    query.include("pointerArray");
    LCObject cloudObject = query.get(LCObject.getObjectId());
    List<LCObject> pointerArray = cloudObject.getList("pointerArray");
    assertEquals(1, pointerArray.size());
  }

  public void testSaveWithPointerArrayUnique() throws Exception {
    LCObject obj = new LCQuery("ObjectUnitTest").getFirst();
    final LCObject LCObject = new LCObject("ObjectUnitTest");
    LCObject.addUnique("pointerArray",
            LCObject.createWithoutData("ObjectUnitTest", obj.getObjectId()));
    LCObject.addUnique("pointerArray",
            LCObject.createWithoutData("ObjectUnitTest", obj.getObjectId()));
    LCObject.save();

    LCQuery query = new LCQuery("ObjectUnitTest");
    LCObject cloudObject = query.get(LCObject.getObjectId());
    List<LCObject> pointerArray = cloudObject.getList("pointerArray");
    assertEquals(1, pointerArray.size());
  }

  public void testIncrementUpdate() throws Exception {
    LCObject obj = new LCObject("ObjectUnitTest");
    obj.put("number", 100);
    obj.put("name", "testIncrementUpdate");
    obj.save();

    LCObject cloudObj1 = LCObject.createWithoutData("ObjectUnitTest", obj.getObjectId());
    LCObject cloudObj2 = LCObject.createWithoutData("ObjectUnitTest", obj.getObjectId());

    cloudObj1.fetchIfNeeded();
    cloudObj2.fetchIfNeeded();

    // update different properties
    cloudObj1.put("number", 50);
    cloudObj2.put("name", "another test");
    cloudObj1.save();
    cloudObj2.save();
    // check properties
    LCObject cloudObj3 = LCObject.createWithoutData("ObjectUnitTest", obj.getObjectId());
    cloudObj3.fetchIfNeeded();

    assertEquals(50, cloudObj3.getInt("number"));
    assertEquals("another test", cloudObj3.getString("name"));
  }

  public void testInvalidField() {
    final LCObject LCObject = new LCObject("ObjectUnitTest");
    assertTrue(LCObject.getObjectId().isEmpty());
    LCObject.put("$test", "hello world".getBytes());
    try {
      LCObject.save();
      fail();
    } catch (Exception ex) {
      ;
    }
  }

  public void testDeleteObjectField() throws Exception {
    final LCObject LCObject = new LCObject("ObjectUnitTest");
    assertTrue(LCObject.getObjectId().isEmpty());
    LCObject.put("willBeDeleted", 1);
    LCObject.save();

    LCObject cloudObj1 = LCObject.createWithoutData("ObjectUnitTest", LCObject.getObjectId());
    cloudObj1.fetchIfNeeded();
    assertEquals(1, cloudObj1.getInt("willBeDeleted"));

    LCObject.remove("willBeDeleted");
    LCObject.save();
    LCObject cloudObj2 = LCObject.createWithoutData("ObjectUnitTest", LCObject.getObjectId());
    cloudObj2.fetchIfNeeded();
    assertFalse(cloudObj2.has("willBeDeleted"));
    assertNull(cloudObj2.get("willBeDeleted"));
  }

  public void testBytesType() throws Exception {
    final LCObject LCObject = new LCObject("ObjectUnitTest");
    assertTrue(LCObject.getObjectId().isEmpty());
    LCObject.put("bytes", "hello world".getBytes());
    LCObject.save();

    String id = LCObject.getObjectId();
    LCObject cloudObject = LCObject.createWithoutData("ObjectUnitTest", id);
    cloudObject.fetchIfNeeded();
    assertEquals("hello world", new String(cloudObject.getBytes("bytes")));

  }

//  public void testAddRemoveRelation() throws Exception {
//    AVObject avObject = new AVObject("ObjectUnitTest");
//    AVRelation likes = avObject.getRelation("parents");
//
//    AVQuery query = new AVQuery("ObjectUnitTest");
//    query.setLimit(5);
//    List<AVObject> list = query.find();
//    likes.addAll(list);
//
//    avObject.saveInBackground().blockingSubscribe();
//
//    assertFalse(avObject.getObjectId().isEmpty());
//    AVRelation anotherLikes = avObject.getRelation("parents");
//    List<AVObject> parents = anotherLikes.getQuery().find();
//    assertEquals(parents.size(), list.size());
//
//    // Remove relation
//    anotherLikes.remove(list.get(0));
//    avObject.save();
//    anotherLikes = avObject.getRelation("parents");
//    parents = anotherLikes.getQuery().find();
//    assertEquals(parents.size() + 1, list.size());
//  }

  public void testArrayField() throws Exception {
    LCObject LCObject = new LCObject("ObjectUnitTest");
    LCObject.add("normal_array", new Date());

    LCObject.save();

    LCObject cloudObj = LCObject.createWithoutData("ObjectUnitTest", LCObject.getObjectId());
    cloudObj.fetchIfNeeded();

    List<Date> array = cloudObj.getList("normal_array");
    assertEquals(1, array.size());
    assertTrue(array.get(0) instanceof Date);

    cloudObj.addAll("normal_array", Arrays.asList(1, 2, 3, 4));
    cloudObj.save();

    cloudObj = LCObject.createWithoutData("ObjectUnitTest", LCObject.getObjectId());
    cloudObj.fetchIfNeeded();
    array = cloudObj.getList("normal_array");
    assertEquals(5, array.size());
  }

  public void testUniqueArray() throws Exception {
    LCObject LCObject = new LCObject("ObjectUnitTest");
    Date date = new Date();
    LCObject.addUnique("unique_array", date);

    LCObject.save();

    LCObject cloudObj = LCObject.createWithoutData("ObjectUnitTest", LCObject.getObjectId());
    cloudObj.fetchIfNeeded();

    List<Date> array = cloudObj.getList("unique_array");
    assertEquals(1, array.size());
    assertTrue(array.get(0) instanceof Date);

    cloudObj.addUnique("unique_array", date);
    cloudObj.save();

    cloudObj = LCObject.createWithoutData("ObjectUnitTest", LCObject.getObjectId());
    cloudObj.fetchIfNeeded();
    array = cloudObj.getList("unique_array");
    assertEquals(1, array.size());
    assertTrue(array.get(0) instanceof Date);
  }

  public void testSaveSubClass() throws Exception {
    Armor armor = new Armor();
    armor.setDisplayName("dennis zane");
    armor.setBroken(false);
    armor.saveInBackground().blockingSubscribe();
    assertFalse(armor.getObjectId().isEmpty());
  }

//  public void testSubClassAsRelation() throws Exception {
//    testSaveSubClass();
//    AVObject avObject = new AVObject("ObjectUnitTest");
//    AVRelation<Armor> armors = avObject.getRelation("armors");
//    AVQuery<Armor> query = AVObject.getQuery(Armor.class);
//    Armor armor = query.getFirst();
//    armors.add(armor);
//
//    avObject.save();
//
//    AVRelation<Armor> anotherArmors = avObject.getRelation("armors");
//    List<Armor> results = anotherArmors.getQuery().find();
//    assertEquals(1, results.size());
//    Armor cloudArmor = results.get(0);
//    cloudArmor.fetchIfNeeded();
//    assertEquals(cloudArmor, armor);
//  }

  public void testQueryAndUpdateSubClass() throws Exception {
    testSaveSubClass();
    LCQuery<Armor> query = LCObject.getQuery(Armor.class);
    Armor armor = query.getFirst();
    assertNotNull(armor);
    assertTrue(armor instanceof Armor);
    assertEquals("dennis zane", armor.getDisplayName());
    int oldDurability = armor.getDurability();
    armor.takeDamage(10);
    armor.save();

    Armor cloudArmor = LCObject.createWithoutData(Armor.class, armor.getObjectId());
    cloudArmor.fetchIfNeeded();
    assertEquals(oldDurability - 10, cloudArmor.getDurability());
  }
}
