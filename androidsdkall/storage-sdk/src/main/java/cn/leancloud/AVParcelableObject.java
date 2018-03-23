package cn.leancloud;

import android.os.Parcel;
import android.os.Parcelable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class AVParcelableObject implements Parcelable {
  private AVObject instance = null;

  public AVParcelableObject(AVObject object) {
    this.instance = object;
  }
  public AVParcelableObject(Parcel in) {
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int i) {
    out.writeString(this.instance.getClassName());
    out.writeString(this.instance.getCreatedAt());
    out.writeString(this.instance.getUpdatedAt());
    out.writeString(this.instance.getObjectId());
    out.writeString(JSON.toJSONString(instance.serverData, new ObjectValueFilter(),
            SerializerFeature.NotWriteRootClassName, SerializerFeature.WriteClassName));
    out.writeString(JSON.toJSONString(instance.operations, SerializerFeature.WriteClassName,
            SerializerFeature.NotWriteRootClassName));
  }

  public static transient final Creator CREATOR = AVObjectCreator.instance;

  public static class AVObjectCreator implements Creator {
    public static AVObjectCreator instance = new AVObjectCreator();

    private AVObjectCreator() {

    }

    @Override
    public AVObject createFromParcel(Parcel parcel) {
      AVObject avobject = new AVObject(parcel.readString());
//      Class<? extends AVObject> subClass = AVUtils.getAVObjectClassByClassName(avobject.getClassName());
//      if (subClass != null) {
//        try {
//          AVObject returnValue = AVObject.cast(avobject, subClass);
//          return returnValue;
//        } catch (Exception e) {
//        }
//      }
      return avobject;
    }

    @Override
    public AVObject[] newArray(int i) {
      return new AVObject[i];
    }
  }

}