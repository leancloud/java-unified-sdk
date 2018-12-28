package cn.leancloud.sample.testcase;

import android.content.Intent;

import cn.leancloud.AVException;
import cn.leancloud.AVObject;
import cn.leancloud.AVParcelableObject;
import cn.leancloud.AVQuery;
import cn.leancloud.sample.DemoBaseActivity;
import cn.leancloud.sample.Student;

/**
 * Created by fengjunwen on 2018/6/27.
 */

public class ObjectTransferDemoActivity extends DemoBaseActivity {
  public void testTransferObject() throws AVException {
    AVQuery q = new AVQuery("Student");
    q.addDescendingOrder("createdAt");
    AVObject student = q.getFirst();
    if (student == null) {
      student = new AVObject("Student");
      student.put("age", 12);
      student.put("name", "Mike");
    }

    System.out.println("sender: " + student.toJSONString());
    System.out.println("sender objectId:" + student.getObjectId());

    AVParcelableObject parcelableObject = new AVParcelableObject(student);
    Intent intent = new Intent(this, ObjectTransferTargetActivity.class);
    intent.putExtra("attached", parcelableObject);
    this.startActivity(intent);
  }
  public void testTransferSubObject() throws AVException {

  }
  public void testTransferUser() throws AVException {

  }
}
