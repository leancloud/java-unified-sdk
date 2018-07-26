package cn.leancloud;

import cn.leancloud.callback.GetDataCallback;
import cn.leancloud.core.AVOSCloud;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.LinkedList;
import java.util.List;

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

  public void testUploadDownloadAssociateFile() throws Exception {
    AVFile avFile;

    AVObject avObject = new AVObject("FileUnitTest");
    avFile = new AVFile("FileUnitTestFiles", "hello world".getBytes());
    avFile.save();
    assertFalse(avFile.getObjectId().isEmpty());
    avObject.put("applicatName", "steve");
    avObject.put("applicatFile", avFile);
    avObject.save();
    assertFalse(avObject.getObjectId().isEmpty());

    // retrieve file
    AVFile file = AVFile.withObjectIdInBackground(avFile.getObjectId()).blockingFirst();

    assertEquals(avFile.getUrl(), file.getUrl());
    assertEquals(avFile.getMetaData(), file.getMetaData());

    GetDataCallback cb = new GetDataCallback() {
      @Override
      public void done(byte[] data, AVException e) {
        assertEquals("hello world", new String(data));
      }
    };

    file.getData();

    // get file from object
    AVFile appFile = avObject.getAVFile("applicatFile");
    assertEquals(avFile.getUrl(), appFile.getUrl());
    assertEquals(avFile.getMetaData(), appFile.getMetaData());

    appFile.getData();

    // query file from server
    AVObject cloudObj = AVObject.createWithoutData("FileUnitTest", avObject.getObjectId());
    cloudObj.fetchIfNeeded();
    appFile = avObject.getAVFile("applicatFile");
    assertEquals(avFile.getUrl(), appFile.getUrl());
    assertEquals(avFile.getMetaData(), appFile.getMetaData());

    appFile.getData();
  }

  public void testFileArray() throws Exception {
    System.out.println("begin testFileArray()...");
    AVFile avFile1 = new AVFile("FileUnitTestFiles", "hello world".getBytes());
    avFile1.save();
    AVFile avFile2 = new AVFile("FileUnitTestFiles", "hello world".getBytes());
    avFile2.save();

    AVObject avObject = new AVObject("FileUnitTest");
    avObject.add("file_array", avFile1);
    avObject.add("file_array", avFile2);
    avObject.save();

    AVObject cloudObj =
            AVQuery.getQuery("FileUnitTest").include("file_array").get(avObject.getObjectId());

    List<AVFile> files = cloudObj.getList("file_array");
    assertNotNull(files);
    assertEquals(2, files.size());
    for (AVFile file : files) {
      file.fetch();
      byte[] data = file.getData();
      assertEquals("hello world", new String(data));
    }
  }

  public void testFileArrayWithAddAll() throws Exception {
    AVFile avFile1 = new AVFile("FileUnitTestFiles", "hello world".getBytes());
    avFile1.save();
    AVFile avFile2 = new AVFile("FileUnitTestFiles", "hello world".getBytes());
    avFile2.save();

    List<AVFile> fileList = new LinkedList<AVFile>();
    fileList.add(avFile1);
    fileList.add(avFile2);

    AVObject avObject = new AVObject("FileUnitTest");
    avObject.addAll("file_array", fileList);
    avObject.save();

    AVObject cloudObj =
            AVQuery.getQuery("FileUnitTest").include("file_array").get(avObject.getObjectId());

    List<AVFile> files = cloudObj.getList("file_array");
    assertNotNull(files);
    assertEquals(2, files.size());
    for (AVFile file : files) {
      file.fetch();
      byte[] data = file.getData();
      assertEquals("hello world", new String(data));
    }
  }

  public void testFileArrayGetInSubClass() throws Exception {
    AVObject.registerSubclass(Operation.class);
    AVFile avFile1 = new AVFile("FileUnitTestFiles", "hello world".getBytes());
    avFile1.save();
    AVFile avFile2 = new AVFile("FileUnitTestFiles", "hello world".getBytes());
    avFile2.save();

    List<AVFile> fileList = new LinkedList<AVFile>();
    fileList.add(avFile1);
    fileList.add(avFile2);

    Operation operation = new Operation();
    operation.addAll("file_array", fileList);
    operation.save();

    AVObject cloudObj =
            Operation.getQuery(Operation.class).include("file_array").get(operation.getObjectId());

    List<AVFile> files = cloudObj.getList("file_array");
    assertNotNull(files);
    assertEquals(2, files.size());
    for (AVFile file : files) {
      file.fetch();
      byte[] data = file.getData();
      assertEquals("hello world", new String(data));
    }
  }

  public void testFileArrayWrongWithPut() throws Exception {
    AVFile avFile1 = new AVFile("FileUnitTestFiles", "hello world".getBytes());
    avFile1.save();
    AVFile avFile2 = new AVFile("FileUnitTestFiles", "hello world".getBytes());
    avFile2.save();

    List<AVFile> fileList = new LinkedList<AVFile>();
    fileList.add(avFile1);
    fileList.add(avFile2);

    AVObject avObject = new AVObject("WrongFileArrayTest");
    avObject.put("file_array", fileList);
    avObject.save();

    AVObject cloudObj =
            AVQuery.getQuery("WrongFileArrayTest").include("file_array").get(avObject.getObjectId());

    List<AVFile> files = cloudObj.getList("file_array");
    assertNotNull(files);
    assertEquals(2, files.size());
    for (AVFile file : files) {
      file.fetch();
      byte[] data = file.getData();
      assertEquals("hello world", new String(data));
    }
  }
}
