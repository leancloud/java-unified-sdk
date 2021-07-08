/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.leancloud.utils;

import android.os.Bundle;
import android.os.Parcelable;
import java.util.Arrays;


/*
 * copy from  android-25 / android / support / v4 / app /
 */

class BundleUtil {
    /**
     * Get an array of Bundle objects from a parcelable array field in a bundle.
     * Update the bundle to have a typed array so fetches in the future don't need
     * to do an array copy.
     */
    public static Bundle[] getBundleArrayFromBundle(Bundle bundle, String key) {
        Parcelable[] array = bundle.getParcelableArray(key);
        if (array instanceof Bundle[] || array == null) {
            return (Bundle[]) array;
        }
        Bundle[] typedArray = Arrays.copyOf(array, array.length,
                Bundle[].class);
        bundle.putParcelableArray(key, typedArray);
        return typedArray;
    }
}