package cn.leancloud.testcase;

import cn.leancloud.AVObject;
import cn.leancloud.DemoBaseActivity;
import cn.leancloud.AVException;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by fengjunwen on 2018/3/22.
 */

public class ObjectDemoActivity extends DemoBaseActivity {
  public void testCreateObject() throws AVException {
    AVObject student = new AVObject("Student");
    student.put("age", 12);
    student.put("name", "Mike");
    student.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(AVObject avObject) {
        log("Thread:" + Thread.currentThread().getId());
        log("保存了一个学生：" + prettyJSON(avObject));
        logThreadTips();
      }

      @Override
      public void onError(Throwable e) {

      }

      @Override
      public void onComplete() {

      }
    });

  }

  public void testUpdateObject() throws AVException {
    AVObject student = new AVObject("Student");
    student.setObjectId("fparuew3rl4l233");

    student.put("age", 20);
    student.saveInBackground();
    log("更改后学生的年龄：" + student.getInt("age"));
  }
}
