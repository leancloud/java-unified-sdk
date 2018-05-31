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
    String archivedContent = ArchivedRequests.getArchiveContent(this.instance, false);
    out.writeString(this.instance.getClassName());
    out.writeString(archivedContent);
  }

  public static transient final Creator CREATOR = AVObjectCreator.instance;

  public static class AVObjectCreator implements Creator {
    public static AVObjectCreator instance = new AVObjectCreator();

    private AVObjectCreator() {

    }

    @Override
    public AVObject createFromParcel(Parcel parcel) {
      String className = parcel.readString();
      String content = parcel.readString();
      AVObject rawObject = ArchivedRequests.parseAVObject(content);
      return Transformer.transform(rawObject, className);
    }

    @Override
    public AVObject[] newArray(int i) {
      return new AVObject[i];
    }
  }

}