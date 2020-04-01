package cn.leancloud.sample.testcase;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import ar.com.daidalos.afiledialog.FileChooserDialog;
import cn.leancloud.AVException;
import cn.leancloud.AVFile;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.sample.DemoBaseActivity;
import cn.leancloud.sample.DemoUtils;
import cn.leancloud.sample.R;
import cn.leancloud.callback.FindCallback;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.types.AVNull;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by fengjunwen on 2018/3/22.
 */

public class FileDemoActivity extends DemoBaseActivity {
  private String fileUrl = null;
  private String objectId = null;

  interface SelectFileCallback {
    void onFileSelect(File file);
  }

  public void testUploaderContentWithObserver() throws Exception {
    String contents = StringUtil.getRandomString(64);
    AVFile file = new AVFile("test", contents.getBytes());
    Observable<AVFile> result = file.saveInBackground();
    result.subscribe(new Observer<AVFile>() {
      @Override
      public void onSubscribe(Disposable d) {
        ;
      }

      @Override
      public void onNext(AVFile avFile) {
        log("Thread:" + Thread.currentThread().getId());
        log("保存了一个File：" + avFile.getObjectId());
        Toast.makeText(FileDemoActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onError(Throwable e) {
        Toast.makeText(FileDemoActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onComplete() {

      }
    });
  }

  public void testUploaderExternelUrlWithObserver() throws Exception {
    AVFile file = new AVFile("test", "http://cms-bucket.ws.126.net/2020/0401/8666ec9dp00q83fid008oc000m801n8c.png");
    Observable<AVFile> result = file.saveInBackground();
    result.subscribe(new Observer<AVFile>() {
      @Override
      public void onSubscribe(Disposable d) {
        ;
      }

      @Override
      public void onNext(AVFile avFile) {
        log("Thread:" + Thread.currentThread().getId());
        log("保存了一个File：" + avFile.getObjectId());
        Toast.makeText(FileDemoActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onError(Throwable e) {
        Toast.makeText(FileDemoActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onComplete() {

      }
    });
  }

  public void testUploaderContentWithCallback() throws Exception {
    String contents = StringUtil.getRandomString(64);
    AVFile file = new AVFile("test", contents.getBytes());
    Observable<AVFile> result = file.saveInBackground();
    result.subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback<AVFile>() {
      @Override
      public void done(AVException e) {
        log("Thread:" + Thread.currentThread().getId());
        Toast.makeText(FileDemoActivity.this, "上传成功：" + (null == e), Toast.LENGTH_SHORT).show();
      }
    }));
  }

  public void testUploaderExternelUrlWithCallback() throws Exception {
    AVFile file = new AVFile("test", "http://cms-bucket.ws.126.net/2020/0401/8666ec9dp00q83fid008oc000m801n8c.png");
    Observable<AVFile> result = file.saveInBackground();
    result.subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback<AVFile>() {
      @Override
      public void done(AVException e) {
        log("Thread:" + Thread.currentThread().getId());
        Toast.makeText(FileDemoActivity.this, "上传成功：" + (null == e), Toast.LENGTH_SHORT).show();
      }
    }));
  }

  private void selectFile(final SelectFileCallback callback) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final FileChooserDialog dialog = new FileChooserDialog(getRunningContext());
        dialog.show();
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
          @Override
          public void onFileSelected(Dialog source, File file) {
            if (callback != null) {
              callback.onFileSelect(file);
            }
            source.dismiss();
          }

          @Override
          public void onFileSelected(Dialog source, File folder, String name) {

          }
        });
      }
    });
  }

  public void testFileUpload() throws AVException {
    selectFile(new SelectFileCallback() {
      @Override
      public void onFileSelect(File file) {
        byte[] data = DemoUtils.readFile(file);
        final AVFile avFile = new AVFile(file.getName(), data);
        avFile.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
          @Override
          public void done(AVException e) {
            if (e == null) {
              fileUrl = avFile.getUrl();
              objectId = avFile.getObjectId();
              log("文件上传成功 url:" + fileUrl);
            } else {
              log(e.getMessage());
            }
          }
        }));
      }
    });
  }

  // create an object and query it.
  public void testFileDownload() throws AVException {
    if (DemoUtils.isBlankString(fileUrl)) {
      log("Please upload file at first.");
      return;
    }
    AVFile avFile = new AVFile("my_download_file", fileUrl, null);
    byte[] bytes = avFile.getData();
    log("下载文件完毕，总字节数：" + bytes.length);
  }

  // 需要控制台开启权限
  public void testFileDelete() throws Exception {
    if (DemoUtils.isBlankString(objectId)) {
      log("Please upload file at first.");
      return;
    }
    AVFile avFile = AVFile.createWithoutData(AVFile.class, objectId);
    avFile.delete();
    log("删除成功，被删掉的文件的 objectId 为 " + objectId);
  }

  public void testCreateFileFromBytes() throws AVException {
    AVFile file = new AVFile("testCreateFileFromBytes", getAvatarBytes());
    file.save();
    log("从 bytes 中创建了文件 file:" + toString(file));
    logThreadTips();
  }

  private File createCacheFile(String name) throws IOException {
    File tmpFile = new File(getCacheDir(), name);
    byte[] bytes = "hello world".getBytes();
    FileOutputStream outputStream = new FileOutputStream(tmpFile);
    outputStream.write(bytes, 0, bytes.length);
    outputStream.close();
    return tmpFile;
  }

  public void testCreateFileFromPath() throws IOException, AVException {
    File tmpFile = createCacheFile("testCreateFileFromPath");

    AVFile file = AVFile.withAbsoluteLocalPath("testCreateFileFromPath", tmpFile.getAbsolutePath());
    file.save();
    log("从文件的路径中构造了 AVFile，并保存成功。file:" + toString(file));
  }

  public void testCreateAVFileFromFile() throws IOException, AVException {
    File tmpFile = createCacheFile("testCreateAVFileFromFile");

    AVFile file = AVFile.withFile("testCreateAVFileFromFile", tmpFile);
    file.save();
    log("用文件构造了 AVFile，并保存成功。file:" + toString(file));
  }

  String toString(AVFile file) {
    return "AVFile, url: " + file.getUrl() + " objectId:" + file.getObjectId() + " metaData" + file.getMetaData() +
        "name:" + file.getName();
  }

  public void testCreateFileFromAVObject() throws AVException {
    AVQuery<AVFile> q = new AVQuery<>(AVFile.CLASS_NAME);
    AVObject first = q.getFirst();
    log("获取了文件 AVObject：" + first);
  }

  public void testCreateFileWithObjectId() throws AVException, FileNotFoundException {
    AVQuery<AVFile> q = new AVQuery<>(AVFile.CLASS_NAME);
    AVObject first = q.getFirst();
    log("获取了文件 AVObject：" + first);
  }

  public void testFileMetaData() throws AVException {
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output);
    byte[] bytes = output.toByteArray();

    AVFile file = new AVFile("avatar", bytes);
    file.addMetaData("width", bitmap.getWidth());
    file.addMetaData("height", bitmap.getHeight());
    file.save();

    log("保存了文件及其 MetaData, file:" + toString(file));
  }

  AVFile saveAvatar() throws AVException {
    byte[] bytes = getAvatarBytes();
    AVFile file = new AVFile("avatar", bytes);
    file.save();
    return file;
  }

  public void testThumbnail() throws AVException {
    AVFile avatar = saveAvatar();
    String url = avatar.getThumbnailUrl(true, 200, 200);
    log("最大宽度为200 、最大高度为200的缩略图 url:" + url);
    // http://docs.qiniu.com/api/v6/image-process.html
    log("其它图片处理见七牛文档");
  }

  public void testGetDataInBackground() throws Exception {
    AVFile portrait = new AVFile("thumbnail", "http://file.everydaydiary.luyunxinchen.cn/437K25F9DpoWnJcJgbQECCV994ntJKpCGGudo6af.png");
    portrait.getDataInBackground().subscribe(new Observer<byte[]>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(byte[] bytes) {
        log("file data length: " + bytes.length);
      }

      @Override
      public void onError(Throwable e) {
        log("failed to get data. cause: " + e.getMessage());
      }

      @Override
      public void onComplete() {

      }
    });
  }

  public void testGetDataStreamInBackground() throws Exception {
    AVFile portrait = new AVFile("thumbnail", "http://file.everydaydiary.luyunxinchen.cn/437K25F9DpoWnJcJgbQECCV994ntJKpCGGudo6af.png");
    portrait.getDataStreamInBackground().subscribe(new Observer<InputStream>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(InputStream inputStream) {
        try {
          byte[] buffer = new byte[102400];
          int read = inputStream.read(buffer);
          log("file data length: " + read);
          inputStream.close();
        } catch (Exception ex) {
          ;
        }
      }

      @Override
      public void onError(Throwable e) {
        log("failed to get data. cause: " + e.getMessage());
      }

      @Override
      public void onComplete() {

      }
    });
  }

  public void testSample1() throws AVException {
    String contents = StringUtil.getRandomString(64);
    final AVFile file = new AVFile("test", contents.getBytes());
    file.saveInBackground().subscribe(new Observer<AVFile>() {
      public void onSubscribe(Disposable disposable) {
      }
      public void onNext(AVFile avFile) {
        System.out.println("succeed to upload file. objectId=" + avFile.getObjectId());
        file.deleteInBackground().subscribe(new Observer<AVNull>() {
          public void onSubscribe(Disposable disposable) {
          }
          public void onNext(AVNull avNull) {
            System.out.println("succeed to delete file");
          }
          public void onError(Throwable throwable) {
            throwable.printStackTrace();
          }
          public void onComplete() {
          }
        });
      }
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
      }
      public void onComplete() {
      }
    });
  }
  public void testSample2() throws AVException,FileNotFoundException {
    AVFile file = AVFile.withAbsoluteLocalPath("test.jpg", Environment.getExternalStorageDirectory() + "/xxx.jpg");
    file.addMetaData("width", 100);
    file.addMetaData("height", 100);
    file.addMetaData("author", "LeanCloud");
    file.saveInBackground().blockingSubscribe();

    file = new AVFile("Satomi_Ishihara.gif", "http://ww3.sinaimg.cn/bmiddle/596b0666gw1ed70eavm5tg20bq06m7wi.gif",
        new HashMap<String, Object>());
    AVObject todo = new AVObject("Todo");
    todo.put("girl", file);
    todo.put("topic", "明星");
    todo.save();
  }

  public void testSample3() throws AVException {
    AVQuery<AVObject> query = new AVQuery<>("Todo");
    query.whereEqualTo("topic", "明星");
    query.include("girl");
    query.findInBackground().subscribe(ObserverBuilder.buildSingleObserver(new FindCallback<AVObject>() {
      @Override
      public void done(List<AVObject> list, AVException e) {
        if(null != list && list.size() > 0) {
          list.get(0).getAVFile("girl").getUrl();
        }
      }
    }));
  }
}
