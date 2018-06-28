package cn.leancloud.sample;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class SourceCodeActivity extends Activity {
  private WebView webView = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    webView = new WebView(this);
    setContentView(webView);
    loadContent();
  }

  private void loadContent() {
    String code = getIntent().getStringExtra(DemoBaseActivity.CONTENT_TAG);
    DemoUtils.loadCodeAtWebView(this, code, webView);
  }
}
