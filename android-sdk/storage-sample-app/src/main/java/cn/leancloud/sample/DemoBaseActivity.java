package cn.leancloud.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
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

import cn.leancloud.*;

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
    setContentView(cn.leancloud.sample.R.layout.demo_base);
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

  protected void log(String msg) {
    Log.d(TAG_DEMO, msg);

    if (outputTextView != null) {
      final String displayText = outputTextView.getText() + "\n-------- RUN --------\n" + msg;
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          outputTextView.setText(displayText);
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

  protected void logThreadTips() {
    log("请注意这里的例子是运行在后台线程的，所以可以去请求网络阻塞线程，若在主线程调用请用 xxxInBackground 方法");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(cn.leancloud.sample.R.menu.menu_demo_group, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == cn.leancloud.sample.R.id.action_source) {
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


  protected void showInputDialog(final String title, final InputDialogListener listener) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(demoRunActivity);
        LayoutInflater inflater = LayoutInflater.from(demoRunActivity);
        final LinearLayout layout = (LinearLayout) inflater.inflate(cn.leancloud.sample.R.layout.login_dialog, null);

        final EditText userNameET = (EditText) layout.findViewById(cn.leancloud.sample.R.id.usernameInput);
        final EditText passwordET = (EditText) layout.findViewById(cn.leancloud.sample.R.id.passwordInput);
        builder.setView(layout);
        builder.setTitle(title).setPositiveButton(title, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String username = userNameET.getText().toString();
            String password = passwordET.getText().toString();
            if (listener != null && username.length() > 0 && password.length() > 0) {
              listener.onAction(username, password);
            }
          }
        });
        AlertDialog ad = builder.create();
        ad.show();
      }
    });
  }

  protected void showSimpleInputDialog(final String title, final SimpleInputDialogListner listner) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getRunningContext());
        final EditText editText = new EditText(getRunningContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(editText);
        builder.setTitle(title).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String text = editText.getText().toString();
            if (text.length() > 0 && listner != null) {
              listner.onConfirm(text);
            }
          }
        });
        AlertDialog ad = builder.create();
        ad.show();
      }
    });
  }

  protected boolean filterException(Exception e) {
    if (e == null) {
      return true;
    } else {
      log(e.getMessage());
      return false;
    }
  }

  protected <T extends AVObject> void logObjects(List<T> objects, String key) {
    StringBuilder sb = new StringBuilder();
    sb.append("一组对象 ");
    sb.append(key);
    sb.append(" 字段的值：\n");
    for (AVObject obj : objects) {
      sb.append(obj.get(key));
      sb.append("\n");
    }
    log(sb.toString());
  }

  public byte[] getAvatarBytes() {
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), cn.leancloud.sample.R.drawable.avatar);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output);
    byte[] bytes = output.toByteArray();
    return bytes;
  }

  public List<Student> findStudents() throws AVException {
    AVQuery<Student> q = AVObject.getQuery(Student.class);
    q.limit(5);
    return q.find();
  }

  protected <T extends AVObject> String prettyJSON(List<T> objects) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (AVObject object : objects) {
      sb.append(prettyJSON(object));
      sb.append(",");
    }
    sb.append("]");
    return sb.toString();
  }

  protected String prettyJSON(AVObject object) {
    com.alibaba.fastjson.JSONObject jsonObject = object.toJSONObject();
    try {
      return jsonObject.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return object.toString();
    }
  }

  public interface InputDialogListener {
    void onAction(final String username, final String password);
  }

  public interface SimpleInputDialogListner {
    void onConfirm(String text);
  }

  protected String getClassName() {
    return this.getClass().getSimpleName();
  }

  protected Student getFirstStudent() throws AVException {
    AVQuery<Student> q = AVObject.getQuery(Student.class);
    q.addDescendingOrder("createdAt");
    Student student = q.getFirst();
    if (student == null) {
      log("请先运行创建对象的例子，以便有对象可演示");
      throw new IllegalStateException("can not find any object");
    }
    return student;
  }

  protected Context getRunningContext() {
    return demoRunActivity;
  }

}
