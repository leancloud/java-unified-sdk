package cn.leancloud.im;

import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.leancloud.im.v2.messages.AVIMFileMessage;
import cn.leancloud.im.v2.messages.AVIMImageMessage;

/**
 * Created by fengjunwen on 2018/7/4.
 */

public class AndroidFileMetaAccessor implements FileMetaAccessor {
  public Map<String, Object> mediaInfo(File file) {
    Map<String, Object> meta = new HashMap<String, Object>();

    try {
      MediaMetadataRetriever retriever = new MediaMetadataRetriever();
      retriever.setDataSource(file.getAbsolutePath());

      String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
      String format = getMimeType(file.getAbsolutePath());
      double duration = Long.parseLong(durationString) / 1000.0d;

      meta.put(AVIMFileMessage.FORMAT, format);
      meta.put(AVIMFileMessage.DURATION, duration);
    } catch (Exception e) {
      meta.put(AVIMFileMessage.DURATION, 0l);
      meta.put(AVIMFileMessage.FORMAT, "");
    }

    return meta;
  }

  public String getMimeType(String url)
  {
    String type = null;
    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
    if (extension != null) {
      MimeTypeMap mime = MimeTypeMap.getSingleton();
      type = mime.getMimeTypeFromExtension(extension);
    }
    return type;
  }

  public Map<String, Object> getImageMeta(File file) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;

    // Returns null, sizes are in the options variable
    BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    int width = options.outWidth;
    int height = options.outHeight;
    // If you want, the MIME type will also be decoded (if possible)
    String type = options.outMimeType;
    Map<String, Object> meta = new HashMap<String, Object>();
    meta.put(AVIMFileMessage.FORMAT, type);
    meta.put(AVIMImageMessage.IMAGE_WIDTH, width);
    meta.put(AVIMImageMessage.IMAGE_HEIGHT, height);
    return meta;
  }

}
