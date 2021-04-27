package cn.leancloud.sample.testcase;

import java.util.HashMap;

import cn.leancloud.LCCloud;
import cn.leancloud.LCException;
import cn.leancloud.LCObject;
import cn.leancloud.sample.DemoBaseActivity;
import cn.leancloud.sample.Student;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by fengjunwen on 2018/5/10.
 */

public class EngineDemoActivity extends DemoBaseActivity {
  public void testCallCloudFunction() throws LCException {
    Object hello = LCCloud.callFunctionInBackground("hello", null).blockingFirst();
    log("云引擎返回的结果:" + hello);
    logThreadTips();
  }

  public void testErrorCode() throws LCException {
    LCCloud.callFunctionInBackground("errorCode", null).subscribe(new Observer<Object>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(Object o) {

      }

      @Override
      public void onError(Throwable e) {
        if (e instanceof LCException) {
          LCException ave = (LCException) e;
          if (ave.getCode() == 211) {
            log("云引擎返回的 Error, code：" + ave.getCode() + " message:" + ave.getMessage());
            return;
          }
        }
      }

      @Override
      public void onComplete() {

      }
    });
  }

  public void testCustomErrorCode() throws LCException {
//    try {
      LCCloud.callFunctionInBackground("customErrorCode", null).blockingSubscribe();
//    } catch (LCException e) {
//      if (e.getCode() == 123) {
//        log("云引擎返回的 Error, code：" + e.getCode() + " message:" + e.getMessage());
//      } else {
//        throw e;
//      }
//    }
  }

  public void testFetchObject() throws LCException {
    Student student = getFirstStudent();
    HashMap<String, Object> params = new HashMap<>();

    Student fetchStudent = Student.createWithoutData(Student.class, student.getObjectId());
    params.put("obj", student);
    Object fetchObject = LCCloud.callFunctionInBackground("fetchObject", params).blockingFirst();
    log("根据返回结果构造的对象:" + fetchObject);
  }

  public void testFullObject() throws LCException {
    Object object = LCCloud.callFunctionInBackground("fullObject", null).blockingFirst();
    log("从云引擎中获取整个对象:" + object);
  }

  public void testBeforeSave() throws LCException {
    LCObject object = new LCObject("LCCloudTest");
    object.put("string", "This is too much long, too much long, too long");
    object.setFetchWhenSave(true);
    object.save();
    log("通过 beforeSave Hook 截断至 10个字符:" + object.getString("string"));
  }
}
