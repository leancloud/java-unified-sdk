package cn.leancloud.push.lite;

import android.content.Context;

public interface AVConnectivityListener {
  void onMobile(Context context);
  void onWifi(Context context);
  void onOtherConnected(Context context);
  void onNotConnected(Context context);
}
