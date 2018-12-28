package cn.leancloud.sample.testcase;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import cn.leancloud.AVObject;
import cn.leancloud.AVParcelableObject;
import cn.leancloud.sample.R;

public class ObjectTransferTargetActivity extends AppCompatActivity {
  private AVObject attached = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    AVParcelableObject parcelableObject = getIntent().getParcelableExtra("attached");
    if (null != parcelableObject) {
      attached = parcelableObject.object();
      System.out.println("receiver: " + attached.toJSONString());
      System.out.println("receiver objectId:" + attached.getObjectId());
    } else {
      System.out.println("parcelableObject is null.");
    }
    setContentView(R.layout.activity_object_transfer_target);
  }
}
