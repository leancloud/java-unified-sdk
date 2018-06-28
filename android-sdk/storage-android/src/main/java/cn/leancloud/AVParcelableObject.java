package cn.leancloud;

import android.os.Parcel;
import android.os.Parcelable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.leancloud.utils.LogUtil;


public class AVParcelableObject implements Parcelable {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVParcelableObject.class);
  private AVObject instance = null;

  public AVParcelableObject(AVObject object) {
    this.instance = object;
  }

  public AVParcelableObject() {
    super();
  }
//  public AVParcelableObject(Parcel in) {
//    instance = CREATOR.createFromParcel(in);
//  }

  public AVObject object() {
    return this.instance;
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
    LOGGER.d("writeToParcel with archivedContent: " + archivedContent);
  }

  public static transient final Creator<AVParcelableObject> CREATOR = AVObjectCreator.instance;

  public static class AVObjectCreator implements Creator<AVParcelableObject> {
    public static AVObjectCreator instance = new AVObjectCreator();

    private AVObjectCreator() {

    }

    @Override
    public AVParcelableObject createFromParcel(Parcel parcel) {
      String className = parcel.readString();
      String content = parcel.readString();
      LOGGER.d("createFromParcel with archivedContent: " + content + ", className: " + className);
      AVObject rawObject = ArchivedRequests.parseAVObject(content);
      return new AVParcelableObject(Transformer.transform(rawObject, className));
    }

    @Override
    public AVParcelableObject[] newArray(int i) {
      return new AVParcelableObject[i];
    }
  }

}