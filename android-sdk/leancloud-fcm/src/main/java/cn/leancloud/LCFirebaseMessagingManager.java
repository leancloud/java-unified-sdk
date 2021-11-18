package cn.leancloud;

import com.google.firebase.messaging.FirebaseMessaging;

public class LCFirebaseMessagingManager {
    /**
     * set flag indicating whether to initialize push token automatically or not.
     * @param isEnabled enable flag.
     */
    public static void setAutoInitEnabled(boolean isEnabled) {
        FirebaseMessaging.getInstance().setAutoInitEnabled(isEnabled);
    }

    /**
     * push token auto initialization flag
     * @return auto init flag.
     */
    public static boolean isAutoInitEnabled() {
        return FirebaseMessaging.getInstance().isAutoInitEnabled();
    }
}
