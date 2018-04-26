package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;

public class ObjectUnitTest extends TestCase {
  public ObjectUnitTest(String testName) {
    super(testName);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVObject.registerSubclass(Armor.class);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }
  public static Test suite() {
    return new TestSuite(ObjectUnitTest.class);
  }

  private void assertObject(AVObject avObject, Date date) {
    assertEquals(1, avObject.getInt("number"));
    assertEquals("testSaveGetDeleteGet", avObject.getString("name"));
    assertEquals(date, avObject.getDate("now"));
  }

  public void testSaveGetDeleteGet() throws Exception {
    final AVObject avObject = new AVObject("ObjectUnitTest");
    assertTrue(avObject.getObjectId().isEmpty());
    avObject.put("number", 1);
    avObject.put("name", "testSaveGetDeleteGet");
    Date date = new Date();
    avObject.put("now", date);
    avObject.save();
    assertFalse(avObject.getObjectId().isEmpty());

    assertObject(avObject, date);

    // Try to get it
    AVObject cloudObject = AVObject.createWithoutData("ObjectUnitTest", avObject.getObjectId());
    cloudObject = cloudObject.fetchIfNeeded();
    assertNotNull(cloudObject);
    assertObject(cloudObject, date);

    // Delete it
    cloudObject.delete();

    AVObject tmp = AVObject.createWithoutData("ObjectUnitTest", avObject.getObjectId()).fetchIfNeeded();
    assertNull(tmp.getDate("now"));
  }

  public void testSaveWithWrongColumnType() {
    final AVObject avObject = new AVObject("ObjectUnitTest");
    assertTrue(avObject.getObjectId().isEmpty());
    avObject.put("number", 1);
    avObject.put("name", "testSaveGetDeleteGet");
    avObject.put("now", 1);

    try {
      avObject.save();
      fail();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  public void testSaveWithWrongTypePointer() throws Exception {
    final AVObject avObject = new AVObject("ObjectUnitTest");
    assertTrue(avObject.getObjectId().isEmpty());
    avObject.put("number", 1);
    avObject.put("name", "testSaveGetDeleteGet");
    avObject.put("parent", AVObject.createWithoutData("ObjectUnitTest", "5ad6e79d9f545400457ff851"));
    avObject.save();

    // try to save with wrong pointer type
    AVObject avObject1 = new AVObject("ObjectUnitTest");
    assertTrue(avObject1.getObjectId().isEmpty());
    avObject1.put("number", 1);
    avObject1.put("name", "testSaveGetDeleteGet");
    avObject1.put("now", 1);
    // unknown type
    avObject1.put("parent", new AVObject("UnknowType"));

    try {
      avObject1.save();
      fail();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  public void testUpdateCounter() throws Exception {
    AVObject avObject = new AVQuery<AVObject>("ObjectUnitTest").getFirstInBackground().blockingFirst();
    assertNotNull(avObject);
    final int oldNumber = avObject.getInt("number");
    avObject.increment("number");
    avObject.saveInBackground().blockingSubscribe();

    new AVQuery("ObjectUnitTest").getInBackground(avObject.getObjectId()).subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject o) {
        int newValue = o.getInt("number");
        assertEquals(oldNumber +1, newValue);
      }

      @Override
      public void onError(Throwable throwable) {
        fail();
      }

      @Override
      public void onComplete() {

      }
    });
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
    AVObject obj = new AVObject("ObjectUnitTest");
    long now = System.currentTimeMillis();
    obj.put("number", now);
    final AVObject avObject = new AVObject("ObjectUnitTest");
    avObject.put("pointer", obj);
    avObject.save();

    AVObject theObject = AVObject.createWithoutData("ObjectUnitTest", avObject.getObjectId());
    theObject.refresh("pointer");
    AVObject pointer = theObject.getAVObject("pointer");
    assertEquals(now, pointer.getLong("number"));

    // test fetch
    theObject = AVObject.createWithoutData("ObjectUnitTest", avObject.getObjectId());
    theObject = theObject.fetch("pointer");
    pointer = theObject.getAVObject("pointer");
    assertEquals(now, pointer.getLong("number"));
  }

  public void testSaveWithPointerArrayAndQuryInclude() throws Exception {
    AVObject obj = new AVQuery("ObjectUnitTest").getFirst();
    final AVObject avObject = new AVObject("ObjectUnitTest");
    avObject
            .add("pointerArray", AVObject.createWithoutData("ObjectUnitTest", obj.getObjectId()));
    avObject
            .add("pointerArray", AVObject.createWithoutData("ObjectUnitTest", obj.getObjectId()));
    avObject.save();

    AVQuery query = new AVQuery("ObjectUnitTest");
    query.include("pointerArray");
    AVObject cloudObject = query.get(avObject.getObjectId());
    List<AVObject> pointerArray = cloudObject.getList("pointerArray");
    assertEquals(2, pointerArray.size());
  }

  public void testSaveWithPointerArrayUnique() throws Exception {
    AVObject obj = new AVQuery("ObjectUnitTest").getFirst();
    final AVObject avObject = new AVObject("ObjectUnitTest");
    avObject.addUnique("pointerArray",
            AVObject.createWithoutData("ObjectUnitTest", obj.getObjectId()));
    avObject.addUnique("pointerArray",
            AVObject.createWithoutData("ObjectUnitTest", obj.getObjectId()));
    avObject.save();

    AVQuery query = new AVQuery("ObjectUnitTest");
    AVObject cloudObject = query.get(avObject.getObjectId());
    List<AVObject> pointerArray = cloudObject.getList("pointerArray");
    assertEquals(1, pointerArray.size());
  }

  public void testIncrementUpdate() throws Exception {
    AVObject obj = new AVObject("ObjectUnitTest");
    obj.put("number", 100);
    obj.put("name", "testIncrementUpdate");
    obj.save();

    AVObject cloudObj1 = AVObject.createWithoutData("ObjectUnitTest", obj.getObjectId());
    AVObject cloudObj2 = AVObject.createWithoutData("ObjectUnitTest", obj.getObjectId());

    cloudObj1.fetchIfNeeded();
    cloudObj2.fetchIfNeeded();

    // update different properties
    cloudObj1.put("number", 50);
    cloudObj2.put("name", "another test");
    cloudObj1.save();
    cloudObj2.save();
    // check properties
    AVObject cloudObj3 = AVObject.createWithoutData("ObjectUnitTest", obj.getObjectId());
    cloudObj3.fetchIfNeeded();

    assertEquals(50, cloudObj3.getInt("number"));
    assertEquals("another test", cloudObj3.getString("name"));
  }

  public void testInvalidField() {
    final AVObject avObject = new AVObject("ObjectUnitTest");
    assertTrue(avObject.getObjectId().isEmpty());
    avObject.put("$test", "hello world".getBytes());
    try {
      avObject.save();
      fail();
    } catch (Exception ex) {
      ;
    }
  }

  public void testDeleteObjectField() throws Exception {
    final AVObject avObject = new AVObject("ObjectUnitTest");
    assertTrue(avObject.getObjectId().isEmpty());
    avObject.put("willBeDeleted", 1);
    avObject.save();

    AVObject cloudObj1 = AVObject.createWithoutData("ObjectUnitTest", avObject.getObjectId());
    cloudObj1.fetchIfNeeded();
    assertEquals(1, cloudObj1.getInt("willBeDeleted"));

    avObject.remove("willBeDeleted");
    avObject.save();
    AVObject cloudObj2 = AVObject.createWithoutData("ObjectUnitTest", avObject.getObjectId());
    cloudObj2.fetchIfNeeded();
    assertFalse(cloudObj2.has("willBeDeleted"));
    assertNull(cloudObj2.get("willBeDeleted"));
  }

  public void testBytesType() throws Exception {
    final AVObject avObject = new AVObject("ObjectUnitTest");
    assertTrue(avObject.getObjectId().isEmpty());
    avObject.put("bytes", "hello world".getBytes());
    avObject.save();

    String id = avObject.getObjectId();
    AVObject cloudObject = AVObject.createWithoutData("ObjectUnitTest", id);
    cloudObject.fetchIfNeeded();
    assertEquals("hello world", new String(cloudObject.getBytes("bytes")));

  }

  public void testAddRemoveRelation() throws Exception {
    AVObject avObject = new AVObject("ObjectUnitTest");
    AVRelation likes = avObject.getRelation("parents");

    AVQuery query = new AVQuery("ObjectUnitTest");
    query.setLimit(5);
    List<AVObject> list = query.find();
    likes.addAll(list);

    avObject.saveInBackground().blockingSubscribe();

    assertFalse(avObject.getObjectId().isEmpty());
    AVRelation anotherLikes = avObject.getRelation("parents");
    List<AVObject> parents = anotherLikes.getQuery().find();
    assertEquals(parents.size(), list.size());

    // Remove relation
    anotherLikes.remove(list.get(0));
    avObject.save();
    anotherLikes = avObject.getRelation("parents");
    parents = anotherLikes.getQuery().find();
    assertEquals(parents.size() + 1, list.size());
  }

  public void testArrayField() throws Exception {
    AVObject avObject = new AVObject("ObjectUnitTest");
    avObject.add("normal_array", new Date());

    avObject.save();

    AVObject cloudObj = AVObject.createWithoutData("ObjectUnitTest", avObject.getObjectId());
    cloudObj.fetchIfNeeded();

    List<Date> array = cloudObj.getList("normal_array");
    assertEquals(1, array.size());
    assertTrue(array.get(0) instanceof Date);

    cloudObj.addAll("normal_array", Arrays.asList(1, 2, 3, 4));
    cloudObj.save();

    cloudObj = AVObject.createWithoutData("ObjectUnitTest", avObject.getObjectId());
    cloudObj.fetchIfNeeded();
    array = cloudObj.getList("normal_array");
    assertEquals(5, array.size());
  }

  public void testUniqueArray() throws Exception {
    AVObject avObject = new AVObject("ObjectUnitTest");
    Date date = new Date();
    avObject.addUnique("unique_array", date);

    avObject.save();

    AVObject cloudObj = AVObject.createWithoutData("ObjectUnitTest", avObject.getObjectId());
    cloudObj.fetchIfNeeded();

    List<Date> array = cloudObj.getList("unique_array");
    assertEquals(1, array.size());
    assertTrue(array.get(0) instanceof Date);

    cloudObj.addUnique("unique_array", date);
    cloudObj.save();

    cloudObj = AVObject.createWithoutData("ObjectUnitTest", avObject.getObjectId());
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

  public void testSubClassAsRelation() throws Exception {
    testSaveSubClass();
    AVObject avObject = new AVObject("ObjectUnitTest");
    AVRelation<Armor> armors = avObject.getRelation("armors");
    AVQuery<Armor> query = AVObject.getQuery(Armor.class);
    Armor armor = query.getFirst();
    armors.add(armor);

    avObject.save();

    AVRelation<Armor> anotherArmors = avObject.getRelation("armors");
    List<Armor> results = anotherArmors.getQuery().find();
    assertEquals(1, results.size());
    Armor cloudArmor = results.get(0);
    cloudArmor.fetchIfNeeded();
    assertEquals(cloudArmor, armor);
  }

  public void testQueryAndUpdateSubClass() throws Exception {
    testSaveSubClass();
    AVQuery<Armor> query = AVObject.getQuery(Armor.class);
    Armor armor = query.getFirst();
    assertNotNull(armor);
    assertTrue(armor instanceof Armor);
    assertEquals("dennis zane", armor.getDisplayName());
    int oldDurability = armor.getDurability();
    armor.takeDamage(10);
    armor.save();

    Armor cloudArmor = AVObject.createWithoutData(Armor.class, armor.getObjectId());
    cloudArmor.fetchIfNeeded();
    assertEquals(oldDurability - 10, cloudArmor.getDurability());
  }
}
