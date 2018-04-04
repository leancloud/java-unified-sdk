package cn.leancloud.upload;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.callback.ProgressCallback;
import cn.leancloud.AVFile;
import cn.leancloud.utils.LogUtil;

import java.io.InputStream;

import java.util.List;
import java.util.ArrayList;

/**
 * Use one thread to upload file to qiniu, slicing with 4MB chunk.
 *
 * Created by fengjunwen on 2017/8/14.
 */

class QiniuSlicingUploader extends HttpClientUploader {
  private static AVLogger LOGGER = LogUtil.getLogger(QiniuSlicingUploader.class);

  private final String token;
  private FileUploader.ProgressCalculator progressCalculator;
  private int uploadChunkSize = QiniuAccessor.WIFI_CHUNK_SIZE;
  private String fileKey = null;
  private QiniuAccessor qiniuAccessor;

  QiniuSlicingUploader(AVFile avFile, String token, ProgressCallback progressCallback) {
    super(avFile, progressCallback);
    this.token = token;
    this.fileKey = avFile.getKey();
    this.qiniuAccessor = new QiniuAccessor(getOKHttpClient(), this.token, this.fileKey);
    LOGGER.d("Constructor with token=" + token + ", key=" + fileKey + ", accessor=" + qiniuAccessor);
  }

  public AVException execute() {
    boolean isWifi = true;
    if (!isWifi) {
      // 从七牛的接口来看block size为4M不可变，但是chunkSize是可以调整的
      uploadChunkSize = QiniuAccessor.NONWIFI_CHUNK_SIZE;
    }
    InputStream is = null;
    byte buf[] = new byte[uploadChunkSize];
    int fileSize = this.avFile.getSize();
    int blockCount = (fileSize / QiniuAccessor.BLOCK_SIZE) + (fileSize % QiniuAccessor.BLOCK_SIZE > 0 ? 1 : 0);
    List<String> uploadFileCtxs = new ArrayList<String>(blockCount);

    progressCalculator = new FileUploader.ProgressCalculator(blockCount, new FileUploader.FileUploadProgressCallback() {
      public void onProgress(int progress) {
        publishProgress(progress);
      }
    });

    try {
      is = this.avFile.getDataStream();
      LOGGER.d("begin to upload qiniu. chunkSize=" + uploadChunkSize + ", blockCount=" + blockCount + ", is=" + is);
      // loop for read, upload block to qiniu.
      for (int i = 0; i< blockCount; i++) {
        int currentBlockSize = QiniuAccessor.BLOCK_SIZE;
        int currentBlockOffset = i * QiniuAccessor.BLOCK_SIZE;
        if (i == blockCount - 1) {
          // last block.
          currentBlockSize = fileSize - currentBlockOffset;
        }
        int chunkCount = currentBlockSize / uploadChunkSize + (currentBlockSize % uploadChunkSize > 0? 1 : 0);
        QiniuAccessor.QiniuBlockResponseData lastResponse = null;
        for (int j = 0; j < chunkCount; j++) {
          int currentChunkOffset = j * uploadChunkSize;
          int currentChunkSize = (j == chunkCount -1)? (currentBlockSize - currentChunkOffset): uploadChunkSize;

          // read BLOCK_SIZE content to buf until reach out block size or end-of-file.
          int totalReadCnt = 0;
          int curReadCnt = is.read(buf, totalReadCnt, currentChunkSize - totalReadCnt);
          totalReadCnt += curReadCnt;
          while (curReadCnt > 0 && totalReadCnt < currentChunkSize) {
            curReadCnt = is.read(buf, totalReadCnt, currentChunkSize - totalReadCnt);
            totalReadCnt += curReadCnt;
          }

          if (j == 0) {
            // 1.创建一个block,并且会上传第一个block的第一个chunk的数据
            lastResponse = this.qiniuAccessor.createBlockInQiniu(currentBlockSize, currentChunkSize, buf, DEFAULT_RETRY_TIMES);
            LOGGER.d("createBlockInQiniu(curBlockSize=" + currentBlockSize + ", curChunkSize=" + currentChunkSize + ") result=" + lastResponse);
          } else {
            // 2.分片上传
            QiniuAccessor.QiniuBlockResponseData tmpResponse = lastResponse;
            lastResponse = this.qiniuAccessor.putFileBlocksToQiniu(lastResponse, currentBlockOffset, buf, currentChunkSize, DEFAULT_RETRY_TIMES);
            LOGGER.d("putFileBlocksToQiniu(lastRes=" + tmpResponse + ", curBlockOffset=" + currentBlockOffset + ", curChunkSize=" + currentChunkSize
              + ") result=" + lastResponse);
          }
        }

        if (null != lastResponse){
          uploadFileCtxs.add(lastResponse.ctx);
          progressCalculator.publishProgress(i, 100);
          LOGGER.d("finished to upload block(" + i + "), ctx=" + lastResponse.ctx);
        } else {
          // error.
          // FIXME: 2017/8/14 603 is hardcode.
          return new AVException(603, "failed to upload file to qiniu.");
        }
      }
      QiniuAccessor.QiniuMKFileResponseData finalResponse = this.qiniuAccessor.makeFile(fileSize, uploadFileCtxs, DEFAULT_RETRY_TIMES);
      LOGGER.d("makeFile(fileSize=" + fileSize + ") result=" + finalResponse);
      if (finalResponse == null || !finalResponse.key.equals(fileKey)) {
        return new AVException(AVException.OTHER_CAUSE, "upload file failure");
      }
    } catch (Exception ex) {
      return new AVException(ex);
    } finally {
      try {
        if (null != is) {
          is.close();
        }
      } catch (Exception e) {
        ;
      }
    }

    return null;
  }
}
