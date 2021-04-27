package cn.leancloud.demo.leancloud_search_sample;

import androidx.appcompat.app.AppCompatActivity;
import cn.leancloud.search.LCSearchQuery;
import cn.leancloud.search.SearchActivity;
import cn.leancloud.utils.StringUtil;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.leancloud.json.JSON;


public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    SearchActivity.setHighLightStyle("<font color='#E68A00'>");

    final EditText queryInput = findViewById(R.id.search_input);
    Button searchButton = findViewById(R.id.search_button);
    searchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String queryString = queryInput.getText().toString();
        Log.d(TAG, "query: " + queryString);

        if (StringUtil.isEmpty(queryString)) {
          return;
        }

        LCSearchQuery searchQuery = new LCSearchQuery(queryString);
        searchQuery.orderByDescending("score");
        searchQuery.setTitleAttribute("content");
        SearchActivity activity = new SearchActivity();
        activity.setSearchQuery(searchQuery);
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        intent.putExtra(LCSearchQuery.DATA_EXTRA_SEARCH_KEY, JSON.toJSONString(searchQuery));
        startActivity(intent);
      }
    });
  }
}
