package cn.leancloud.sample;

import android.os.AsyncTask;

/**
 * Created by fengjunwen on 2018/3/22.
 */

public abstract class BackgroundTask extends AsyncTask<Void, Void, Void> {
  volatile private Exception exception = null;

  @Override
  protected Void doInBackground(Void... params) {
    try {
      doInBack();
    } catch (Exception e) {
      exception = e;
      exception.printStackTrace();
    }
    return null;
  }

  @Override
  protected void onPostExecute(Void result) {
    onPost(exception);
  }

  protected abstract void doInBack() throws Exception;

  protected abstract void onPost(Exception e);
}
