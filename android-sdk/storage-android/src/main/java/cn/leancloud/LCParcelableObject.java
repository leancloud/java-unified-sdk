package cn.leancloud;

import android.os.Parcel;
import android.os.Parcelable;

import cn.leancloud.utils.LogUtil;


public class LCParcelableObject implements Parcelable {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCParcelableObject.class);
  private LCObject instance = null;

  public LCParcelableObject(LCObject object) {
    this.instance = object;
  }

  public LCParcelableObject() {
    super();
  }
//  public AVParcelableObject(Parcel in) {
//    instance = CREATOR.createFromParcel(in);
//  }

  public LCObject object() {
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

  public static transient final Creator<LCParcelableObject> CREATOR = LCObjectCreator.instance;

  public static class LCObjectCreator implements Creator<LCParcelableObject> {
    public static LCObjectCreator instance = new LCObjectCreator();

    private LCObjectCreator() {

    }

    @Override
    public LCParcelableObject createFromParcel(Parcel parcel) {
      String className = parcel.readString();
      String content = parcel.readString();
      LOGGER.d("createFromParcel with archivedContent: " + content + ", className: " + className);
      LCObject rawObject = ArchivedRequests.parseAVObject(content);
      return new LCParcelableObject(Transformer.transform(rawObject, className));
    }

    @Override
    public LCParcelableObject[] newArray(int i) {
      return new LCParcelableObject[i];
    }
  }

}