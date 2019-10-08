package cn.leancloud.demo.leancloud_search_sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

public class ResultItemActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_result_item);

    TextView objectView = findViewById(R.id.textObjectId);

    Intent intent = getIntent();
    if (intent.getAction() == Intent.ACTION_VIEW) {
      Uri uri = intent.getData();
      String path = uri.getPath();
      int index = path.lastIndexOf("/");
      if (index > 0) {
        // 获取objectId
        String objectId = path.substring(index + 1);
        objectView.setText("result objectId: " + objectId);
      }
    }
  }
}
