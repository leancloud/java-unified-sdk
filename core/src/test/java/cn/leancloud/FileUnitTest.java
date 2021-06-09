package cn.leancloud;

import cn.leancloud.callback.GetDataCallback;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.LinkedList;
import java.util.List;

import static cn.leancloud.LCUserTest.PASSWORD;
import static cn.leancloud.LCUserTest.USERNAME;

public class FileUnitTest extends TestCase {
  public FileUnitTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(FileUnitTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.out.println("exit setUp()");
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    System.out.println("exit tearDown()");
  }

  public void testSaveWithSpecifiedUser() throws Exception {
    LCUser user = LCUser.logIn(USERNAME, PASSWORD).blockingFirst();
    System.out.println("current sessionToken: " + user.getSessionToken());
    LCFile avFile = new LCFile("FileUnitTestFiles", "hello world".getBytes());
    avFile.save(user);
    assertFalse(avFile.getObjectId().isEmpty());
    avFile = new LCFile("FileUnitTestFiles", "hello world".getBytes());
    avFile.saveInBackground(user, false).blockingFirst();
    assertFalse(avFile.getObjectId().isEmpty());
  }

  public void testUploadDownloadAssociateFile() throws Exception {
    LCFile avFile = new LCFile("FileUnitTestFiles", "hello world".getBytes());
    avFile.save();
    assertFalse(avFile.getObjectId().isEmpty());

    LCObject LCObject = new LCObject("FileUnitTest");
    LCObject.put("applicatName", "steve");
    LCObject.put("applicatFile", avFile);
    LCObject.setFetchWhenSave(true);
    LCObject.save();
    assertFalse(LCObject.getObjectId().isEmpty());

    // retrieve file
    LCFile file = LCFile.withObjectIdInBackground(avFile.getObjectId()).blockingFirst();
    assertEquals(avFile.getUrl(), file.getUrl());
    assertEquals(avFile.getMetaData().size(), file.getMetaData().size());
    assertEquals(avFile.getMetaData().get("_checksum"), file.getMetaData().get("_checksum"));

    GetDataCallback cb = new GetDataCallback() {
      @Override
      public void done(byte[] data, LCException e) {
        assertEquals("hello world", new String(data));
      }
    };

    file.getData();

    // get file from object
    LCFile appFile = LCObject.getLCFile("applicatFile");
    assertEquals(avFile.getUrl(), appFile.getUrl());
    assertEquals(avFile.getMetaData().size(), appFile.getMetaData().size());
    assertEquals(avFile.getMetaData().get("_checksum"), appFile.getMetaData().get("_checksum"));

    appFile.getData();

    // query file from server
    LCObject cloudObj = LCObject.createWithoutData("FileUnitTest", LCObject.getObjectId());
    cloudObj.fetchIfNeeded();
    appFile = LCObject.getLCFile("applicatFile");
    assertEquals(avFile.getUrl(), appFile.getUrl());
    assertEquals(avFile.getMetaData(), appFile.getMetaData());

    appFile.getData();
  }

  public void testFileArray() throws Exception {
    System.out.println("begin testFileArray()...");
    LCFile avFile1 = new LCFile("FileUnitTestFiles", "hello world".getBytes());
    avFile1.save();
    LCFile avFile2 = new LCFile("FileUnitTestFiles", "hello world".getBytes());
    avFile2.save();

    LCObject LCObject = new LCObject("FileUnitTest");
    LCObject.add("file_array", avFile1);
    LCObject.add("file_array", avFile2);
    LCObject.save();

    LCObject cloudObj =
            LCQuery.getQuery("FileUnitTest").include("file_array").get(LCObject.getObjectId());

    List<LCFile> files = cloudObj.getList("file_array");
    assertNotNull(files);
    assertEquals(2, files.size());
    for (LCFile file : files) {
      file.fetch();
      byte[] data = file.getData();
      assertEquals("hello world", new String(data));
    }
  }

  public void testFileArrayWithAddAll() throws Exception {
    LCFile avFile1 = new LCFile("FileUnitTestFiles", "hello world".getBytes());
    avFile1.save();
    LCFile avFile2 = new LCFile("FileUnitTestFiles", "hello world".getBytes());
    avFile2.save();

    List<LCFile> fileList = new LinkedList<LCFile>();
    fileList.add(avFile1);
    fileList.add(avFile2);

    LCObject LCObject = new LCObject("FileUnitTest");
    LCObject.addAll("file_array", fileList);
    LCObject.save();

    LCObject cloudObj =
            LCQuery.getQuery("FileUnitTest").include("file_array").get(LCObject.getObjectId());

    List<LCFile> files = cloudObj.getList("file_array");
    assertNotNull(files);
    assertEquals(2, files.size());
    for (LCFile file : files) {
      file.fetch();
      byte[] data = file.getData();
      assertEquals("hello world", new String(data));
    }
  }

  public void testFileArrayGetInSubClass() throws Exception {
    LCObject.registerSubclass(Operation.class);
    LCFile avFile1 = new LCFile("FileUnitTestFiles", "hello world".getBytes());
    avFile1.save();
    LCFile avFile2 = new LCFile("FileUnitTestFiles", "hello world".getBytes());
    avFile2.save();

    List<LCFile> fileList = new LinkedList<LCFile>();
    fileList.add(avFile1);
    fileList.add(avFile2);

    Operation operation = new Operation();
    operation.addAll("file_array", fileList);
    operation.save();

    LCObject cloudObj =
            Operation.getQuery(Operation.class).include("file_array").get(operation.getObjectId());

    List<LCFile> files = cloudObj.getList("file_array");
    assertNotNull(files);
    assertEquals(2, files.size());
    for (LCFile file : files) {
      file.fetch();
      byte[] data = file.getData();
      assertEquals("hello world", new String(data));
    }
  }

  public void testFileArrayWrongWithPut() throws Exception {
    LCFile avFile1 = new LCFile("FileUnitTestFiles", "hello world".getBytes());
    avFile1.save();
    LCFile avFile2 = new LCFile("FileUnitTestFiles", "hello world".getBytes());
    avFile2.save();

    List<LCFile> fileList = new LinkedList<LCFile>();
    fileList.add(avFile1);
    fileList.add(avFile2);

    LCObject LCObject = new LCObject("WrongFileArrayTest");
    LCObject.put("file_array", fileList);
    LCObject.save();

    LCObject cloudObj =
            LCQuery.getQuery("WrongFileArrayTest").include("file_array").get(LCObject.getObjectId());

    List<LCFile> files = cloudObj.getList("file_array");
    assertNotNull(files);
    assertEquals(2, files.size());
    for (LCFile file : files) {
      file.fetch();
      byte[] data = file.getData();
      assertEquals("hello world", new String(data));
    }
  }
}
