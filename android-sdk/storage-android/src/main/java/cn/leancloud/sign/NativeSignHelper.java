package cn.leancloud.sign;

import android.content.Context;

public class NativeSignHelper {

  // Used to load the 'native-lib' library on application startup.
  static {
    System.loadLibrary("leancloud-core");
  }

  /**
   * A native method that is implemented by the 'native-lib' native library,
   * which is packaged with this application.
   * @return current version.
   */
  public static native String version();

  public static native String generateRequestAuth();

  public static native void initialize(Context context);
}
