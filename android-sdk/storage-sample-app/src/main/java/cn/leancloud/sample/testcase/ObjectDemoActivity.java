package cn.leancloud.sample.testcase;

import android.content.Intent;

import junit.framework.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import cn.leancloud.LCFile;
import cn.leancloud.LCObject;
import cn.leancloud.LCParcelableObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCSaveOption;
import cn.leancloud.sample.DemoBaseActivity;
import cn.leancloud.LCException;
import cn.leancloud.sample.Student;
import cn.leancloud.callback.GetCallback;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by fengjunwen on 2018/3/22.
 */

public class ObjectDemoActivity extends DemoBaseActivity {
  public void testCreateObject() throws LCException {
    LCObject student = new LCObject("Student");
    student.put("age", 12);
    student.put("name", "Mike");
    student.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        log("[Thread:" + Thread.currentThread().getId() + "] 保存了一个学生：" + LCObject.toString());
      }

      @Override
      public void onError(Throwable e) {

      }

      @Override
      public void onComplete() {

      }
    });

  }

  public void testUpdateObject() throws LCException {
    LCObject student = new LCObject("Student");
    student.setObjectId("fparuew3rl4l233");

    student.put("age", 20);
    student.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        log("更改后学生的年龄：" + student.getInt("age"));
      }

      @Override
      public void onError(Throwable e) {
        log("Error:" + e.getMessage());
      }

      @Override
      public void onComplete() {

      }
    });
  }


  public void testSaveWithOption() throws LCException {
    final LCObject LCObject1 = LCObject.createWithoutData("Student", "5a7a4ac8128fe1003768d2b1");
    LCObject1.fetchInBackground().subscribe(ObserverBuilder.buildSingleObserver(new GetCallback<LCObject>() {
      @Override
      public void done(final LCObject LCObject, LCException e) {
        System.out.println(LCObject.getUpdatedAt());
        LCSaveOption LCSaveOption = new LCSaveOption();
        LCSaveOption.query(new LCQuery("Student").whereLessThanOrEqualTo("updatedAt", LCObject.getUpdatedAt()));
        LCObject.put("sss","xxx");
        LCObject.saveInBackground(LCSaveOption).subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
          @Override
          public void done(LCException e) {
            if (e == null) {
              LCObject.toString();
            } else {
              e.printStackTrace();
            }
          }
        }));
      }
    }));

  }


  public void testDeleteObject() throws LCException {
    Student student = getFirstStudent();
    //删掉了第一个学生
    student.delete();
    log("删掉了学生：" + prettyJSON(student));

    try {
      LCQuery<Student> query = LCQuery.getQuery(Student.class);
      query.get(student.getObjectId());
    } catch (Exception e) {
      log("再次去获取这个学生，抛出异常：" + e.getMessage());
    }
  }

  public void testGetObject() throws LCException {
    Student first = getFirstStudent();

    Student student = LCObject.createWithoutData(Student.class, first.getObjectId());
    LCObject fetched = student.fetch();
    log("用 objectId 创建了对象，并获取了数据：" + fetched);
  }

  public void testCreateCascadedObject() throws LCException {
    LCObject person = new LCObject("Student");
    person.put("name", "GTO");
    Student student = new Student();
    student.setName(getClassName());
    student.put("teacher", person);
    student.save();
    log("保存了 Student，并把 LCObject 作为一个字段保存到了对象。student: " + prettyJSON(student));
  }

  public void testCreateObjectWithFile() throws IOException, LCException {
    LCFile avatar = new LCFile("avatar", getAvatarBytes());

    Student student = new Student();
    student.setName(getClassName());
    student.setAvatar(avatar);
    student.save();
    log("保存了文件，并把其作为一个字段保存到了对象。student: " + prettyJSON(student));
  }

  public void testObjectParse() throws Exception {
    Student student = getFirstStudent();
    String s = student.toString();
    log("将对象序列化成字符串：" + s);
  }

  public void testObjectIntent() throws LCException {
    Student student = getFirstStudent();
    Intent intent = new Intent();
    intent.putExtra("student", new LCParcelableObject(student));

    LCParcelableObject intentStudent = intent.getParcelableExtra("student");
    log("通过 intent 传递了对象 " + intentStudent);
  }

  public void testOfflineSave() {
    log("请在网络关闭的时候运行本方法，然后开启网络，看是否保存上");
    Student student = new Student();
    student.setName("testOfflineSave");
    try {
      student.saveEventually();
      log("离线保存了对象：" + prettyJSON(student));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void testIncrement() throws LCException {
    Student student = getFirstStudent();
    log("生日前的年龄：%d", student.getAge());
    student.increment(Student.AGE, 1);
    student.save();
    log("生日了，年龄：%d", student.getAge());
  }

  public void testAnyType() throws LCException {
    Student student = getFirstStudent();
    student.setAny(1);
    student.save();
    log("Any 字段保存为了数字 " + student.getAny());

    student.setAny("hello");
    student.save();
    log("Any 字段保存为了字符串 " + student.getAny());

    HashMap<String, Object> map = new HashMap<>();
    map.put("like", "swimming");
    student.setAny(map);
    student.save();
    log("Any 字段保存为了Map " + student.getAny());
  }

  public void testRemoveKey() throws LCException {
    Student student = getFirstStudent();
    log("名字：" + student.getName());

    student.remove(Student.NAME);
    student.save();
    log("将名字字段置为空后：", student.getName());
  }

  public void testArrayAddObject() throws LCException {
    Student student = getFirstStudent();
    log("添加前的爱好：" + student.getHobbies());
    List<String> hobbies = new ArrayList<>();
    hobbies.add("running");
    hobbies.add("fly");
    student.addAll(Student.HOBBIES, hobbies);
    student.save();
    log("添加了两个爱好, hobbies : " + student.getHobbies());
  }

  public void testArrayAddMutipleObjects() throws LCException {
    Student student = getFirstStudent();
    student.add(Student.HOBBIES, "swimming");
    student.save();
    log("添加了游泳爱好, hobbies : " + student.getHobbies());
  }

  public void testArrayRemoveObject() throws LCException {
    Student student = getFirstStudent();
    log("移除爱好前，hobbies = " + student.getHobbies());
    List<String> removeHobbies = new ArrayList<>();
    removeHobbies.add("swimming");
    student.removeAll(Student.HOBBIES, removeHobbies);
    student.save();
    log("移除爱好后, hobbies = " + student.getHobbies());
  }

  public void testArrayAddUnique() throws LCException {
    Student student = getFirstStudent();
    student.addUnique(Student.HOBBIES, "swimming");
    student.save();
    log("添加了游泳的爱好之后, hobbies: " + student.getHobbies());

    student.addUnique(Student.HOBBIES, "swimming");
    student.save();
    log("再次 addUnique 游泳爱好, hobbies:" + student.getHobbies());
  }

  public void testSaveAll() throws LCException {
    List<Student> students = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      Student student = new Student();
      student.setName(i + "");
      student.setAge(i + 10);
      students.add(student);
    }
    LCObject.saveAll(students);

    log("保存了五个学生: " + prettyJSON(students));
  }

  public void testSaveAllWithFile() throws LCException {
    List<Student> students = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      Student student = new Student();
      student.setName(i + "");
      LCFile avatar = new LCFile("avatar" + i, getAvatarBytes());
      student.setAvatar(avatar);
      students.add(student);
    }
    LCObject.saveAll(students);
    log("批量保存了一批学生及其头像，students:" + prettyJSON(students));
  }

  public void testBatchUpdate() throws LCException {
    List<Student> students = findStudents();
    for (Student student : students) {
      student.setName("testBatchUpdate");
    }
    LCObject.saveAll(students);
    log("批量更改了一批学生的名字，students:" + prettyJSON(students));
  }

  public void testDeleteAll() throws LCException {
    List<Student> students = findStudents();
    LCObject.deleteAll(students);

    log("删除掉了一批学生 " + prettyJSON(students));
  }

  // create an object and query it.
  public void testObjectSaveAndQuery() throws LCException {
    final String key = "array";
    final String objectTable = "ObjectDemoTableRead";
    final LCObject myObject = new LCObject(objectTable);
    for (int i = 0; i < 5; ++i) {
      myObject.add(key, i);
    }
    myObject.save();

    LCQuery<LCObject> query = LCQuery.getQuery(objectTable);
    LCObject result = query.get(myObject.getObjectId());
    List<Number> array = result.getList(key);
    Assert.assertTrue(array.size() == 5);
    if (array.size() != 5) {
      showMessage("", new LCException(LCException.OTHER_CAUSE, "incorrect result"), false);
    } else {
      showMessage("", null, false);
    }
  }

  public void testObjectCreateAndQuery() throws LCException {
    final String objectTable = "ObjectDemoTableCreate";
    final String key = "score";
    LCObject gameScore = new LCObject(objectTable);
    final int targetValue = new Random().nextInt();
    gameScore.put(key, targetValue);
    int value = gameScore.getInt(key);
    Assert.assertTrue(value == targetValue);

    final String targetString = "Sean Plott";
    gameScore.put("playerName", targetString);
    String stringValue = gameScore.getString("playerName");
    Assert.assertTrue(stringValue == targetString);
    gameScore.save();
  }

  // update an object
  public void testObjectUpdateAndQuery() throws LCException {
    final String key = "update";
    final String objectTable = "ObjectDemoTableUpdate";
    final LCObject myObject = new LCObject(objectTable);
    final String value = "anotherValue";
    myObject.put(key, "myValue");
    myObject.save();

    myObject.put(key, value);
    myObject.save();
    LCQuery<LCObject> query = LCQuery.getQuery(objectTable);
    LCObject result = query.get(myObject.getObjectId());
    String stringValue = (String) result.get(key);
    Assert.assertEquals(stringValue, value);
    if (!value.equals(stringValue)) {
      showMessage("", new LCException(LCException.OTHER_CAUSE, "incorrect result"), false);
    } else {
      showMessage("", null, false);
    }
  }

  public void testObjectDeleteAndQuery() throws LCException {
    final String objectTable = "ObjectDemoTableDelete";
    final LCObject myObject = new LCObject(objectTable);
    myObject.save();
    myObject.delete();
    LCQuery<LCObject> query = LCQuery.getQuery(objectTable);
    LCObject result = query.get(myObject.getObjectId());
    Assert.assertTrue(result == null);
    if (result != null) {
      showMessage("", new LCException(LCException.OTHER_CAUSE, "delete failed"), false);
    } else {
      showMessage("", null, false);
    }
  }

  public void testSample1() throws LCException {
    LCObject todo = new LCObject("Todo");
    todo.put("title", "工程师周会");
    todo.put("content", "每周工程师会议，周一下午2点");
    todo.put("location", "会议室");// 只要添加这一行代码，服务端就会自动添加这个字段
    todo.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {}

      public void onNext(LCObject LCObject) {
        System.out.println("succeed to save Object.");
      }

      public void onError(Throwable throwable) {}

      public void onComplete() {}
    });
  }

  public void testDeserializeWithOperationQueue() throws Exception {
    String json = "{ \"@type\":\"com.avos.avoscloud.LCObject\",\"objectId\":\"5b4f1e0dfe88c200357fda80\",\"updatedAt\":\"2018-12-25T15:18:51.574Z\",\"createdAt\":\"2018-07-18T11:01:33.931Z\",\"className\":\"Caricature\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"update\":\"更新至201话\",\"read_url\":\"http://ac.qq.com/ComicView/index/id/624388/cid/3\",\"type\":\"\",\"des\":\"洪明轩-外号“雪豹”这个传说中的格斗高手竟然——遭遇校园冷暴力?!被逼无奈，只能转学的他决心隐藏自己的真正实力，结果转学第一天就惹到了学校老大？还被学校里的一群麻烦家伙缠上了，“雪豹”到底该如何抉择呢，要展示一下自己的真正实力吗？【授权/周四六更新】\",\"tag\":\"校园 爆笑\",\"views\":3.3E+8,\"source_name\":\"腾讯动漫\",\"order\":1891,\"author\":\"作者：KTOON / YUYU \",\"isValid\":\"1\",\"book_img_url\":\"https://manhua.qpic.cn/vertical/0/08_22_58_fb3925ac11acc5063f244ad2aab3aad7_1507474687604.jpg/420\",\"url\":\"http://ac.qq.com/Comic/comicInfo/id/624388\",\"name\":\"进击吧,雪豹\",\"category\":\"\"},\"operationQueue\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"lastUrl\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"lastUrl\",\"type\":\"Set\",\"values\":\"https://m.ac.qq.com/chapter/index/id/624388/cid/3\"}}}";
    //    String json = "{ \"@type\":\"com.avos.avoscloud.LCObject\",\"objectId\":\"5b4f1e0dfe88c200357fda80\",\"updatedAt\":\"2018-12-25T15:18:51.574Z\",\"createdAt\":\"2018-07-18T11:01:33.931Z\",\"className\":\"Caricature\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"update\":\"更新至201话\",\"read_url\":\"http://ac.qq.com/ComicView/index/id/624388/cid/3\",\"type\":\"\",\"des\":\"洪明轩-外号“雪豹”这个传说中的格斗高手竟然——遭遇校园冷暴力?!被逼无奈，只能转学的他决心隐藏自己的真正实力，结果转学第一天就惹到了学校老大？还被学校里的一群麻烦家伙缠上了，“雪豹”到底该如何抉择呢，要展示一下自己的真正实力吗？【授权/周四六更新】\",\"tag\":\"校园 爆笑\",\"views\":3.3E+8,\"source_name\":\"腾讯动漫\",\"order\":1891,\"author\":\"作者：KTOON / YUYU \",\"isValid\":\"1\",\"book_img_url\":\"https://manhua.qpic.cn/vertical/0/08_22_58_fb3925ac11acc5063f244ad2aab3aad7_1507474687604.jpg/420\",\"url\":\"http://ac.qq.com/Comic/comicInfo/id/624388\",\"name\":\"进击吧,雪豹\",\"category\":\"\"}}";
    LCObject obj = LCObject.parseLCObject(json);
    System.out.println(obj);
  }

  public void testDeserializeWithoutOperationQueue() throws Exception {
    String json = "{ \"@type\":\"com.avos.avoscloud.LCObject\",\"objectId\":\"5b04d56a9f545400880c1e00\",\"updatedAt\":\"2018-05-30T03:33:51.183Z\",\"createdAt\":\"2018-05-23T02:43:54.804Z\",\"className\":\"EnglishWebsite\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"Order\":599,\"ad_filte\":\"class:app-guiding bottom-fixed no-close#class:app-guiding#class:down-app-mask\",\"Title\":\"腾讯动漫\",\"category\":\"caricature\",\"ImgUrl\":\"http://www.mzxbkj.com/images/cartoon/cartoon_001.png\",\"Url\":\"http://m.ac.qq.com/\",\"IsValid\":\"1\"}}";
    LCObject obj = LCObject.parseLCObject(json);
    System.out.println(obj);
  }
  public void testSample2() throws LCException {
    LCQuery<LCObject> LCQuery = new LCQuery<>("Todo");
    LCQuery.getInBackground("558e20cbe4b060308e3eb36c").subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {
      }
      public void onNext(LCObject o) {
        System.out.println(o.toString());
      }
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
      }
      public void onComplete() {
      }
    });
    LCObject todo = LCObject.createWithoutData("Todo", "558e20cbe4b060308e3eb36c");
    todo.fetchInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {
      }
      public void onNext(LCObject o) {
        System.out.println(o.toString());
      }
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
      }
      public void onComplete() {
      }
    });
  }
}
