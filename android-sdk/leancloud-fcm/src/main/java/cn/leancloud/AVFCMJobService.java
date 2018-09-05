package cn.leancloud;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by fengjunwen on 2018/8/28.
 */

public class AVFCMJobService extends JobService {
  private static final String TAG = "AVFCMJobService";

  public boolean onStartJob(JobParameters job) {
    Log.d(TAG, "Performing long running task in scheduled job");
    return false;
  }

  public boolean onStopJob(JobParameters job) {
    Log.d(TAG, "end long running task in scheduled job");
    return false;
  }
}