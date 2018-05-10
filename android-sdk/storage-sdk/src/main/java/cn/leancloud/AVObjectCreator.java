package cn.leancloud;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fengjunwen on 2018/5/10.
 */

public class AVObjectCreator implements Parcelable.Creator{
  public static AVObjectCreator instance = new AVObjectCreator();
  public AVObject createFromParcel(Parcel parcel) {
    AVObject avobject = new AVObject();
//    Class<? extends AVObject> subClass = AVUtils.getAVObjectClassByClassName(avobject.className);
//    if(subClass != null) {
//      try {
//        AVObject returnValue = AVObject.cast(avobject, subClass);
//        return returnValue;
//      } catch (Exception var5) {
//        ;
//      }
//    }

    return avobject;
  }

  public AVObject[] newArray(int i) {
    return new AVObject[i];
  }
}
