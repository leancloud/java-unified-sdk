package cn.leancloud;

import cn.leancloud.auth.UserBasedTestCase;
import cn.leancloud.callback.LogInCallback;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.callback.SignUpCallback;
import cn.leancloud.convertor.ObserverBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.CountDownLatch;

public class UserUnitTest extends UserBasedTestCase {
  public static final String username = "steve" + System.currentTimeMillis();
  public static final String password = "f32@ds*@&dsa";
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public UserUnitTest(String name) {
    super(name);
    LCObject.registerSubclass(SubUser.class);
    LCObject.registerSubclass(Armor.class);
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
    LCUser user = new LCUser();
    assertTrue(user.getSessionToken() == null);
    user.setUsername(username);
    user.setPassword(password);
    user.setEmail("steve" + System.currentTimeMillis() + "@company.com");

    user.put("phone", "213-253-0000");

    SignUpCallback cb = new SignUpCallback() {
      @Override
      public void done(LCException e) {
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
    LCUser newUser = new LCUser();
    assertTrue(newUser.getSessionToken() == null);
    newUser.setUsername(username);
    newUser.setPassword(password);
    cb = new SignUpCallback() {
      @Override
      public void done(LCException e) {
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

  public void login(LCUser user) throws Exception {

    LCUser cloudUser = LCUser.logIn(username, password).blockingFirst();
    assertTrue(cloudUser.getSessionToken() != null);
    assertEquals(cloudUser.getObjectId(), user.getObjectId());
    assertEquals(cloudUser.getSessionToken(), user.getSessionToken());
    assertEquals(username, cloudUser.getUsername());

    latch = new CountDownLatch(1);
    testSucceed = false;

    LogInCallback cb = new LogInCallback<LCUser>() {

      @Override
      public void done(LCUser user, LCException e) {
        if (null != e) {
          e.printStackTrace();
          testSucceed = true;
        }
        latch.countDown();
      }
    };
    LCUser.logIn(username, password+1).subscribe(ObserverBuilder.buildSingleObserver(cb));
    latch.await();
    assertTrue(testSucceed);
  }

  public void currentUser(LCUser user) {
    LCUser currentUser = LCUser.getCurrentUser();
    assertEquals(currentUser.getObjectId(), user.getObjectId());
    assertEquals(currentUser.getSessionToken(), user.getSessionToken());
    assertEquals(username, currentUser.getUsername());
  }

  public void logout(LCUser user) throws Exception{
    LCUser.logOut();
    assertNull(LCUser.getCurrentUser());
    LCUser cloudUser = LCUser.logIn(username, password).blockingFirst();
    LCUser currentUser = LCUser.getCurrentUser();
    assertNotNull(currentUser);
    assertEquals(currentUser, cloudUser);
    assertEquals(currentUser.getObjectId(), user.getObjectId());
    assertEquals(currentUser.getSessionToken(), user.getSessionToken());
    assertEquals(username, currentUser.getUsername());
  }

  public void currentUserWithRelation(LCUser user) throws Exception {

    // User has a pointer,and that pointer has a relation field
    LCObject object = new LCObject("UserUnitTest");
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
    LCUser.changeCurrentUser(null,false);
    // get current user
    LCUser currentUser = LCUser.getCurrentUser();

    LCObject unitTest = currentUser.getLCObject("UserUnitTest");
    assertNotNull(unitTest);
    LCRelation<LCObject> relation = unitTest.getRelation("me");
    assertNotNull(relation);
    assertEquals(1,relation.getQuery().find().size());
  }

  public void testSignupSubUser() throws Exception {
    LCObject armor = LCQuery.getQuery("Armor").getFirst();
    if (null == armor) {
      Armor newObj = new Armor();
      newObj.setBroken(false);
      newObj.setDisplayName("test at " + System.currentTimeMillis());
      newObj.save();
      armor = newObj;
    }
    SubUser subUser = new SubUser();
    String username = "dennis" + System.currentTimeMillis();
    String nickName = "testSignupSubUser";
    subUser.setUsername(username);
    subUser.setPassword(password);
    subUser.setNickName(nickName);
    subUser.setArmor(armor);
    subUser.signUp();
    System.out.println("signup success");

    try {
      assertFalse(subUser.getObjectId().isEmpty());
      assertFalse(subUser.getSessionToken().isEmpty());

      SubUser cloudUser = LCUser.logIn(username, password,
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
    LCUser currentUser = LCUser.getCurrentUser();
    assertTrue(currentUser instanceof SubUser);

    LCUser.changeCurrentUser(null,false);
    // Then we will get the user from local file storage.
    SubUser theUser = LCUser.getCurrentUser(SubUser.class);
    assertNotNull(theUser);
    assertEquals(username, theUser.getUsername());
    assertEquals(nickName, theUser.getNickName());
  }

  public void withFile(LCUser user) throws Exception {
    final LCUser currentUser = LCUser.getCurrentUser();
    final LCFile file = new LCFile("test", "hello".getBytes());
    SaveCallback cb = new SaveCallback() {

      @Override
      public void done(LCException e) {
        assertNull(e);
        SaveCallback cb2 = new SaveCallback() {

          @Override
          public void done(LCException e) {
            assertNull(e);
            LCFile theFile = currentUser
                    .getLCFile("test_file");
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
