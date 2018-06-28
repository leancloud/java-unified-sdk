package cn.leancloud.testcase;

import android.content.Intent;

import cn.leancloud.AVException;
import cn.leancloud.AVObject;
import cn.leancloud.AVParcelableObject;
import cn.leancloud.DemoBaseActivity;

/**
 * Created by fengjunwen on 2018/6/27.
 */

public class ObjectTransferDemoActivity extends DemoBaseActivity {
  public void testTransferObject() throws AVException {
    AVObject student = new AVObject("Student");
    student.put("age", 12);
    student.put("name", "Mike");
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
