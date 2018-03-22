package cn.leancloud;

import android.os.Parcel;
import android.os.Parcelable;
import cn.leancloud.network.PaasClient;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import cn.leancloud.core.ObjectValueFilter;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class AVObject extends cn.leancloud.core.AVObject implements Parcelable {
  public AVObject(String className) {
    super(className);
  }
  private AVObject(cn.leancloud.core.AVObject original) {
    super(original);
  }

  public AVObject(Parcel in) {
    super("");
  }

  @Override
  public Observable<? extends AVObject> saveInBackground() {
    Observable<? extends cn.leancloud.core.AVObject> result = super.saveInBackground();
    return result.map(new Function<cn.leancloud.core.AVObject, AVObject>() {
      public AVObject apply(@NonNull cn.leancloud.core.AVObject var1) throws Exception {
        return new AVObject(var1);
      }
    });
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int i) {
    out.writeString(this.getClassName());
    out.writeString(this.getCreatedAt());
    out.writeString(this.getUpdatedAt());
    out.writeString(this.getObjectId());
    out.writeString(JSON.toJSONString(serverData, new ObjectValueFilter(),
            SerializerFeature.NotWriteRootClassName, SerializerFeature.WriteClassName));
    out.writeString(JSON.toJSONString(operations, SerializerFeature.WriteClassName,
            SerializerFeature.NotWriteRootClassName));
  }

  public static transient final Creator CREATOR = AVObjectCreator.instance;

  public static class AVObjectCreator implements Creator {
    public static AVObjectCreator instance = new AVObjectCreator();

    private AVObjectCreator() {

    }

    @Override
    public AVObject createFromParcel(Parcel parcel) {
      AVObject avobject = new AVObject(parcel);
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