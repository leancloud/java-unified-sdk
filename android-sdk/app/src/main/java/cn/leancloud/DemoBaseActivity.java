package cn.leancloud;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fengjunwen on 2018/3/22.
 */

public class DemoBaseActivity extends ListActivity {
  static public final String CONTENT_TAG = "content";
  public static final String METHOD_TAG = "method";
  public static final String TAG_DEMO = "Demo";
  private TextView outputTextView;
  protected DemoRunActivity demoRunActivity;

  private List<String> codeSnippetList = new ArrayList<String>();
  private List<String> displayNames = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setContentView(R.layout.demo_base);
    setupAdapter();
  }

  public void setupAdapter() {
    findAllMethods();
    ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this,
        android.R.layout.simple_list_item_1,
        displayNames);
    setListAdapter(adapter);
  }


  public void findAllMethods() {
    List<String> methods = methodsWithPrefix("test");
    sortMethods(methods);
    codeSnippetList.clear();
    codeSnippetList.addAll(methods);
    displayNames.clear();
    for (String method : methods) {
      displayNames.add(method.substring(4));
    }
  }
  public List<String> methodsWithPrefix(final String prefix) {
    List<String> methods = new ArrayList<String>();
    try {
      Class c = this.getClass();
      Method m[] = c.getDeclaredMethods();
      for (int i = 0; i < m.length; i++) {
        if (m[i].getName().startsWith(prefix)) {
          methods.add(m[i].getName());
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return methods;
  }

  private void sortMethods(final List<String> methods) {
    final Map<String, Integer> positions = new HashMap<>();
    String sourceCode = getFileSourceCode();
    for (String method : methods) {
      int pos = sourceCode.indexOf(method);
      positions.put(method, pos);
    }
    Collections.sort(methods, new Comparator<String>() {
      @Override
      public int compare(String lhs, String rhs) {
        return positions.get(lhs) - positions.get(rhs);
      }
    });
  }
  private String getFileSourceCode() {
    try {
      // 如果是 .java，会出现同名 java，方法跳转等IDE功能有问题
      String name = this.getClass().getSimpleName() + ".file";
      InputStream inputStream = getAssets().open(name);
      String content = DemoUtils.readTextFile(inputStream);
      return content;
    } catch (Exception e) {
      showMessage(e.getMessage());
    }
    return null;
  }

  public void showMessage(final String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  public void showMessage(final String text, Exception e, boolean busy) {
    if (e == null) {
      if (outputTextView == null) {
        showMessage(text + " finished.");
      } else {
        log(text + " finished");
      }
    } else {
      if (outputTextView == null) {
        showMessage(e.toString());
      } else {
        log(e.toString());
      }
    }
    if (!busy) {
      setProgressBarIndeterminateVisibility(false);
    }
  }

  protected void log(String format, @Nullable Object... objects) {
    final String msg = String.format(format, objects);
    Log.d(TAG_DEMO, msg);
    if (outputTextView != null) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          outputTextView.setText(outputTextView.getText() + "\n-------- RUN --------\n" + msg);
        }
      });
    }
  }

  protected void fastLog(Object... objects) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < objects.length; i++) {
      sb.append(objects[i]);
      sb.append(" ");
    }
    log(sb.toString());
  }

  protected String prettyJSON(AVObject object) {
    JSONObject jsonObject = object.toJSONObject();
    if (null == jsonObject) {
      return object.toString();
    }
    try {
      return jsonObject.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return object.toString();
    }
  }

  protected void logThreadTips() {
    log("请注意这里的例子是运行在后台线程的，所以可以去请求网络阻塞线程，若在主线程调用请用 xxxInBackground 方法");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_demo_group, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_source) {
      showSourceCode();
    }
    return super.onOptionsItemSelected(item);
  }

  private void showSourceCode() {
    try {
      startSourceCodeActivity(getFileSourceCode());
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void startSourceCodeActivity(final String content) {
    try {
      Intent intent = new Intent(this, SourceCodeActivity.class);
      intent.putExtra(CONTENT_TAG, content);
      startActivity(intent);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public void setOutputTextView(TextView outputTextView) {
    this.outputTextView = outputTextView;
  }

  public void setDemoRunActivity(DemoRunActivity demoRunActivity) {
    this.demoRunActivity = demoRunActivity;
  }

  public void runMethod(final Activity demoRunActivity, final String methodName) {
    demoRunActivity.setProgressBarIndeterminateVisibility(true);
    new BackgroundTask() {
      @Override
      protected void doInBack() throws Exception {
        Method method = DemoUtils.getMethodSafely(DemoBaseActivity.this.getClass(), methodName);
        DemoUtils.invokeMethod(DemoBaseActivity.this, method);
      }

      @Override
      protected void onPost(Exception e) {
        demoRunActivity.setProgressBarIndeterminateVisibility(false);
        if (e != null) {
          if (e instanceof InvocationTargetException) {
            // 打印原方法抛出的异常
            log("Error : %s", e.getCause().getMessage());
          } else {
            log("Error : %s", e.getMessage());
          }
        } else {
          log("%s Finished.", methodName);
        }
      }
    }.execute();
  }

  private String formatToTwoSpacesIndent(String method) {
    return method.replaceAll("\n  ", "\n");
  }

  private String getMethodSourceCode(String name) {
    String code = getFileSourceCode();
    String method = null;
    String patternString = String.format
        ("void\\s%s.*?\\{(.*?)\\n\\s\\s\\}\\n", name);
    Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);
    Matcher matcher = pattern.matcher(code);
    if (matcher.find()) {
      method = matcher.group(1);
    }
    return formatToTwoSpacesIndent(method);
  }

  @Override
  protected void onListItemClick(android.widget.ListView l, android.view.View v, int position, long id) {
    Intent intent = new Intent(this, DemoRunActivity.class);
    DemoRunActivity.demoActivity = this;
    String name = codeSnippetList.get(position);
    intent.putExtra(CONTENT_TAG, getMethodSourceCode(name));
    intent.putExtra(METHOD_TAG, name);
    startActivity(intent);
  }


}
