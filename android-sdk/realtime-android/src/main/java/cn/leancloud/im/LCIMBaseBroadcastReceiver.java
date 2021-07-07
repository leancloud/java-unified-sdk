package cn.leancloud.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import cn.leancloud.LCException;
import cn.leancloud.LeanCloud;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.utils.LocalBroadcastManager;

/**
 * Created by fengjunwen on 2018/8/7.
 */

public abstract class LCIMBaseBroadcastReceiver extends BroadcastReceiver {
  LCCallback callback;

  public LCIMBaseBroadcastReceiver(LCCallback callback) {
    this.callback = callback;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      Throwable error = null;
      if (null != intent && null != intent.getExtras() && intent.getExtras().containsKey(Conversation.callbackExceptionKey)) {
        error = (Throwable) intent.getExtras().getSerializable(Conversation.callbackExceptionKey);
      }
      HashMap<String, Object> result = (HashMap<String, Object>) intent.getSerializableExtra(IntentUtil.CALLBACK_RESULT_KEY);
      execute(result, error);
      if (LeanCloud.getContext() != null) {
        LocalBroadcastManager.getInstance(LeanCloud.getContext()).unregisterReceiver(this);
      }
    } catch (Exception e) {
      if (callback != null) {
        callback.internalDone(null, new LCException(e));
      }
    }
  }

  public abstract void execute(Map<String, Object> result, Throwable error);
}
