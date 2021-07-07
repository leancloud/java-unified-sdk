package cn.leancloud.utils;

import android.os.Build.VERSION;

/**
 * BuildCompat contains additional platform version checking methods for
 * testing compatibility with new features.
 *
 * copy from  android-25 / android / support / v4 / app /
 */
public class BuildCompat {
    private BuildCompat() {
    }
    /**
     * Check if the device is running on the Android N release or newer.
     *
     * @return {@code true} if N APIs are available for use
     */
    public static boolean isAtLeastN() {
        return VERSION.SDK_INT >= 24;
    }
    /**
     * Check if the device is running on the Android N MR1 release or newer.
     *
     * @return {@code true} if N MR1 APIs are available for use
     */
    public static boolean isAtLeastNMR1() {
        return VERSION.SDK_INT >= 25;
    }
}
