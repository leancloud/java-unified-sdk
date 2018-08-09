package cn.leancloud.im;

import android.content.Context;

import cn.leancloud.im.v2.AndroidDatabaseDelegate;

/**
 * Created by fengjunwen on 2018/8/9.
 */

public class AndroidDatabaseDelegateFactory implements DatabaseDelegateFactory {
  private Context context;
  public AndroidDatabaseDelegateFactory(Context context) {
    this.context = context;
  }

  public DatabaseDelegate createInstance(String clientId) {
    return new AndroidDatabaseDelegate(this.context, clientId);
  }
}
