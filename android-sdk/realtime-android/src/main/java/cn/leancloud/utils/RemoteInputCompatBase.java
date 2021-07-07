package cn.leancloud.utils;

import android.os.Bundle;


/*
 * copy from  android-25 / android / support / v4 / app /
 */

class RemoteInputCompatBase {
    public static abstract class RemoteInput {
        protected abstract String getResultKey();
        protected abstract CharSequence getLabel();
        protected abstract CharSequence[] getChoices();
        protected abstract boolean getAllowFreeFormInput();
        protected abstract Bundle getExtras();
        public interface Factory {
            public RemoteInput build(String resultKey, CharSequence label,
                                     CharSequence[] choices, boolean allowFreeFormInput, Bundle extras);
            public RemoteInput[] newArray(int length);
        }
    }
}
