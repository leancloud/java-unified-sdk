package cn.leancloud;

import cn.leancloud.callback.LogInCallback;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.callback.SignUpCallback;
import cn.leancloud.convertor.ObserverBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.CountDownLatch;

public class UserUnitTest extends TestCase {
  public static final String username = "steve" + System.currentTimeMillis();
  public static final String password = "f32@ds*@&dsa";
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public UserUnitTest(String name) {
    super(name);

    AVObject.registerSubclass(SubUser.class);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(UserUnitTest.class);
  }


  @Override
  protected void setUp() throws Exception {
    super.setUp();
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testSignupLogin() throws Exception {
    AVUser user = new AVUser();
    assertTrue(user.getSessionToken() == null);
    user.setUsername(username);
    user.setPassword(password);
    user.setEmail("steve" + System.currentTimeMillis() + "@company.com");

    user.put("phone", "213-253-0000");

    SignUpCallback cb = new SignUpCallback() {
      @Override
      public void done(AVException e) {
        if (e != null) {
          e.printStackTrace();
        } else {
          testSucceed = true;
        }
        latch.countDown();
      }
    };
    user.signUpInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));
    latch.await();
    assertTrue(testSucceed);

    assertFalse(user.getSessionToken().isEmpty());

    latch = new CountDownLatch(1);
    testSucceed = false;

    // signup twice should fail
    AVUser newUser = new AVUser();
    assertTrue(newUser.getSessionToken() == null);
    newUser.setUsername(username);
    newUser.setPassword(password);
    cb = new SignUpCallback() {
      @Override
      public void done(AVException e) {
        if (null != e) {
          testSucceed = true;
        }
        latch.countDown();
      }
    };
    newUser.signUpInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));
    latch.await();
    assertTrue(testSucceed);

    login(user);
    currentUser(user);
//    currentUserWithRelation(user);
    logout(user);
  }

  public void login(AVUser user) throws Exception {

    AVUser cloudUser = AVUser.logIn(username, password).blockingFirst();
    assertTrue(cloudUser.getSessionToken() != null);
    assertEquals(cloudUser.getObjectId(), user.getObjectId());
    assertEquals(cloudUser.getSessionToken(), user.getSessionToken());
    assertEquals(username, cloudUser.getUsername());

    latch = new CountDownLatch(1);
    testSucceed = false;

    LogInCallback cb = new LogInCallback<AVUser>() {

      @Override
      public void done(AVUser user, AVException e) {
        if (null != e) {
          e.printStackTrace();
          testSucceed = true;
        }
        latch.countDown();
      }
    };
    AVUser.logIn(username, password+1).subscribe(ObserverBuilder.buildSingleObserver(cb));
    latch.await();
    assertTrue(testSucceed);
  }

  public void currentUser(AVUser user) {
    AVUser currentUser = AVUser.getCurrentUser();
    assertEquals(currentUser.getObjectId(), user.getObjectId());
    assertEquals(currentUser.getSessionToken(), user.getSessionToken());
    assertEquals(username, currentUser.getUsername());
  }

  public void logout(AVUser user) throws Exception{
    AVUser.logOut();
    assertNull(AVUser.getCurrentUser());
    AVUser cloudUser = AVUser.logIn(username, password).blockingFirst();
    AVUser currentUser = AVUser.getCurrentUser();
    assertNotNull(currentUser);
    assertEquals(currentUser, cloudUser);
    assertEquals(currentUser.getObjectId(), user.getObjectId());
    assertEquals(currentUser.getSessionToken(), user.getSessionToken());
    assertEquals(username, currentUser.getUsername());
  }

  public void currentUserWithRelation(AVUser user) throws Exception {

    // User has a pointer,and that pointer has a relation field
    AVObject object = new AVObject("UserUnitTest");
    object.put("name", "UserUnitTest");
    // save itself.
    object.save();
    // add relation
    object.getRelation("me").add(user);
    object.save();

    // added pointer to user
    user.put("UserUnitTest", object);
    user.save();

    // clear current user in memory
    AVUser.changeCurrentUser(null,false);
    // get current user
    AVUser currentUser = AVUser.getCurrentUser();

    AVObject unitTest = currentUser.getAVObject("UserUnitTest");
    assertNotNull(unitTest);
    AVRelation<AVObject> relation = unitTest.getRelation("me");
    assertNotNull(relation);
    assertEquals(1,relation.getQuery().find().size());
  }

  public void testSignupSubUser() throws Exception {
    SubUser subUser = new SubUser();
    String username = "dennis" + System.currentTimeMillis();
    String nickName = "testSignupSubUser";
    subUser.setUsername(username);
    subUser.setPassword(password);
    subUser.setNickName(nickName);
    subUser.setArmor(AVQuery.getQuery("Armor").getFirst());
    subUser.signUp();
    System.out.println("signup success");

    try {
      assertFalse(subUser.getObjectId().isEmpty());
      assertFalse(subUser.getSessionToken().isEmpty());

      SubUser cloudUser = AVUser.logIn(username, password,
              SubUser.class).blockingFirst();
      assertTrue(cloudUser.getSessionToken() != null);
      assertEquals(cloudUser.getObjectId(), subUser.getObjectId());
      assertEquals(cloudUser.getSessionToken(), subUser.getSessionToken());
      assertEquals(username, cloudUser.getUsername());
      assertEquals(nickName, cloudUser.getNickName());
      assertNotNull(cloudUser.getArmor());
    } catch (Exception e) {
      e.printStackTrace();
    }

    // test currentUser
    AVUser currentUser = AVUser.getCurrentUser();
    assertTrue(currentUser instanceof SubUser);

    AVUser.changeCurrentUser(null,false);
    // Then we will get the user from local file storage.
    SubUser theUser = AVUser.getCurrentUser(SubUser.class);
    assertNotNull(theUser);
    assertEquals(username, theUser.getUsername());
    assertEquals(nickName, theUser.getNickName());
  }

  public void withFile(AVUser user) throws Exception {
    final AVUser currentUser = AVUser.getCurrentUser();
    final AVFile file = new AVFile("test", "hello".getBytes());
    SaveCallback cb = new SaveCallback() {

      @Override
      public void done(AVException e) {
        assertNull(e);
        SaveCallback cb2 = new SaveCallback() {

          @Override
          public void done(AVException e) {
            assertNull(e);
            AVFile theFile = currentUser
                    .getAVFile("test_file");
            assertNotNull(theFile);
            assertEquals("test", theFile.getName());
            assertNotNull(theFile.getUrl());

          }
        };
        currentUser.put("test_file", file);
        currentUser.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb2));
      }
    };
    file.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));
  }
}
