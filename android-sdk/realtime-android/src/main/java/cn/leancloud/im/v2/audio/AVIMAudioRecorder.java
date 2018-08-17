package cn.leancloud.im.v2.audio;

import android.hardware.Camera;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

import cn.leancloud.AVLogger;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by fengjunwen on 2017/8/25.
 */

public class AVIMAudioRecorder {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVIMAudioRecorder.class);
  public interface RecordEventListener {
    /**
     * invoke after recording finished.
     *
     * @param milliSeconds   audio recording time(milliseconds). 0 for error, otherwise always great then 0.
     * @param reason         error message, only valid while milliSeconds = 0.
     */
    void onFinishedRecord(long milliSeconds, String reason);

    /**
     * callback method after recording started.
     */
    void onStartRecord();
  }

  private static final long MIN_INTERVAL_TIME = 1000;
  private static final String REASON_TOO_SHORT_TIME = "time is too short(less than 1 second)";

  private MediaRecorder recorder = null;
  private String localPath = null;
  private long startRecordTime = 0l;
  private RecordEventListener listener = null;

  public AVIMAudioRecorder(String path, RecordEventListener listener) {
    if (StringUtil.isEmpty(path)) {
      throw new IllegalArgumentException("local path is empty.");
    }
    this.localPath = path;
    this.listener = listener;
  }

  /**
   * Returns the maximum absolute amplitude that was sampled since the last
   * call to this method. Call this only after the start().
   *
   * @return the maximum absolute amplitude measured since the last call, or
   * 0 when called for the first time
   * @throws IllegalStateException if it is called before
   * the audio source has been set.
   */
  public int getMaxAmplitude() {
    if (null == recorder) {
      return 0;
    }
    return recorder.getMaxAmplitude();
  }

  /**
   * Begins capturing and encoding data to the file specified.
   *
   */
  public void start() {
    try {
      if (null == recorder) {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(this.localPath);
        recorder.prepare();
      } else {
        recorder.reset();
        recorder.setOutputFile(this.localPath);
      }
      recorder.start();
      startRecordTime = System.currentTimeMillis();
      if (null != this.listener) {
        this.listener.onStartRecord();
      }
    } catch (IOException ex) {
      LOGGER.e("failed to start MediaRecorder. cause: ", ex);
    }
  }

  /**
   * Stops recording. Call this after start(). Once recording is stopped,
   * you will have to configure it again as if it has just been constructed.
   * Note that a RuntimeException is intentionally thrown to the
   * application, if no valid audio/video data has been received when stop()
   * is called. This happens if stop() is called immediately after
   * start(). The failure lets the application take action accordingly to
   * clean up the output file (delete the output file, for instance), since
   * the output file is not properly constructed when this happens.
   *
   * @throws IllegalStateException if it is called before start()
   */
  public void stop() {
    stopRecorder(true);
  }

  /**
   * Cancel recording. Call this after start(). Once recording is cancelled,
   * you will have to configure it again as if it has just been constructed.
   */
  public void cancel() {
    stopRecorder(false);
    removeRecordFile();
  }

  private void stopRecorder(boolean notify) {
    if (recorder != null) {
      try {
        recorder.stop();
        if (notify && null != this.listener) {
          long intervalTime = System.currentTimeMillis() - startRecordTime;
          if (intervalTime < MIN_INTERVAL_TIME) {
            removeRecordFile();
            this.listener.onFinishedRecord(0, REASON_TOO_SHORT_TIME);
          } else {
            this.listener.onFinishedRecord(intervalTime, null);
          }
        }
      } catch (Exception e) {
        LOGGER.e("failed to stop MediaRecorder. cause: ", e);
      } finally {
        recorder.release();
        recorder = null;
      }
    }
  }

  private void removeRecordFile() {
    File file = new File(this.localPath);
    if (file.exists()) {
      file.delete();
    }
  }
}