package cn.leancloud.sample.testcase;

import android.content.Intent;

import cn.leancloud.json.JSON;

import cn.leancloud.AVException;
import cn.leancloud.AVObject;
import cn.leancloud.AVParcelableObject;
import cn.leancloud.AVQuery;
import cn.leancloud.sample.DemoBaseActivity;

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

  public void testJsonDeserialization() throws AVException {
    double a = 2.65D;
    System.out.println(a);

    String dataString = "{\"audio\":{\"@type\":\"cn.leancloud.AVFile\",\"bucket\":\"xtuccgoj\",\"dataAvailable\":true," +
        "\"dirty\":false,\"metaData\":{\"@type\":\"java.util.HashMap\",\"owner\":\"5c83c5b9303f390065666111\",\"_checksum\":\"92b0d717e56b77e28976c85433830ad8\",\"size\":8986,\"_name\":\"kuo_audio_1575980353771.mp3\"}," +
        "\"name\":\"5def8d41fc36ed0068874955\",\"objectId\":\"5def8d41fc36ed0068874955\",\"originalName\":\"kuo_audio_1575980353771.mp3\"," +
        "\"ownerObjectId\":\"5c83c5b9303f390065666111\",\"size\":8986,\"url\":\"http://file2.i7play.com/vCiVRj7RBmHSSwyQji815TM8KxY4Umx2NMA6Cg6W.mp3\"},\"value\":0.5D}";
    JSON.parse(dataString);
  }
}
