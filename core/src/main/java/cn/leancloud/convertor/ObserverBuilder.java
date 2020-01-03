package cn.leancloud.convertor;

import cn.leancloud.AVStatus;
import cn.leancloud.AVUser;
import cn.leancloud.callback.*;
import cn.leancloud.AVException;
import cn.leancloud.AVObject;
import cn.leancloud.query.AVCloudQueryResult;
import cn.leancloud.types.AVNull;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

import java.io.InputStream;
import java.util.List;

public class ObserverBuilder {

  static class SingleObjectObserver<T> implements Observer<T> {
    private AVCallback callback;
    SingleObjectObserver(AVCallback<T> callback) {
      this.callback = callback;
    }

    public void onNext(T object) {
      this.callback.internalDone(object, null);
    }

    public void onComplete() {
    }

    public void onError(Throwable error) {
      AVException exception = new AVException(error);
      this.callback.internalDone(exception);
    }
    public void onSubscribe(@NonNull Disposable var1) {
      ;
    }
  }

  static class CollectionObserver<T extends AVObject>  implements Observer<List<T>> {
    private AVCallback<List<T>> callback;
    CollectionObserver(AVCallback callback) {
      this.callback = callback;
    }

    public void onNext(List<T> object) {
      this.callback.internalDone(object, null);
    }

    public void onComplete() {
    }

    public void onError(Throwable error) {
      AVException exception = new AVException(error);
      this.callback.internalDone(exception);
    }

    public void onSubscribe(@NonNull Disposable var1) {
      ;
    }
  }

  /************************************************
   * Single Object Observer.
   ************************************************/

  /**
   * build observer for GetCallback
   * @param callback get callback
   * @param <T> template type
   * @return observer
   */
  public static <T extends AVObject> SingleObjectObserver<T> buildSingleObserver(GetCallback<T> callback) {
    return new SingleObjectObserver<T>(callback);
  }

  /**
   * build observer for SaveCallback
   * @param callback save callback
   * @param <T> template type
   * @return observer
   */
  public static <T extends AVObject> SingleObjectObserver<T> buildSingleObserver(SaveCallback<T> callback) {
    return new SingleObjectObserver<T>(callback);
  }

  /**
   * build observer for DeleteCallback
   * @param callback delete callback
   * @return observer
   */
  public static SingleObjectObserver<AVNull> buildSingleObserver(DeleteCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }

  /**
   * build observer for RefreshCallback
   * @param callback refresh callback
   * @param <T> template type
   * @return observer
   */
  public static <T extends AVObject> SingleObjectObserver<T> buildSingleObserver(RefreshCallback<T> callback) {
    return new SingleObjectObserver<T>(callback);
  }

  /**
   * build observer for LogInCallback
   * @param callback login calblack
   * @param <T> template type
   * @return observer
   */
  public static <T extends AVUser> SingleObjectObserver<T> buildSingleObserver(LogInCallback<T> callback) {
    return new SingleObjectObserver<T>(callback);
  }

  /**
   * build observer for SignupCallback
   * @param callback signup callback
   * @return observer
   */
  public static SingleObjectObserver<AVUser> buildSingleObserver(SignUpCallback callback) {
    return new SingleObjectObserver<AVUser>(callback);
  }

  /**
   * build observer for RequestEmailVerifyCallback
   * @param callback request email verify callback
   * @return observer
   */
  public static SingleObjectObserver<AVNull> buildSingleObserver(RequestEmailVerifyCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }

  /**
   * build observer for RequestMobileCodeCallback
   * @param callback request mobile code callback
   * @return observer
   */
  public static SingleObjectObserver<AVNull> buildSingleObserver(RequestMobileCodeCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }

  /**
   * build observer for FollowersAndFolloweesCallback
   * @param callback follower and followees callback
   * @return observer
   */
  public static SingleObjectObserver<JSONObject> buildSingleObserver(FollowersAndFolloweesCallback callback) {
    return new SingleObjectObserver<>(callback);
  }

  /**
   * build observer for RequestPasswordResetCallback
   * @param callback request password reset callback
   * @return observer
   */
  public static SingleObjectObserver<AVNull> buildSingleObserver(RequestPasswordResetCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }

  /**
   * build observer for CountCallback
   * @param callback count callback
   * @return observer
   */
  public static SingleObjectObserver<Integer> buildSingleObserver(CountCallback callback) {
    return new SingleObjectObserver<Integer>(callback);
  }

  /**
   * build observer for UpdatePasswordCallback
   * @param callback update password callback
   * @return observer
   */
  public static SingleObjectObserver<AVNull> buildSingleObserver(UpdatePasswordCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }

  /**
   * build observer for MobilePhoneVerifyCallback
   * @param callback mobile phone verify callback
   * @return observer
   */
  public static SingleObjectObserver<AVNull> buildSingleObserver(MobilePhoneVerifyCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }

  /**
   * build observer for FollowCallback
   * @param callback follow callback
   * @param <T> template type
   * @return observer
   */
  public static <T extends AVObject> SingleObjectObserver<T> buildSingleObserver(FollowCallback<T> callback) {
    return new SingleObjectObserver<>(callback);
  }

  /**
   * build observer for FunctionCallback
   * @param callback function callback
   * @param <T> template type
   * @return observer
   */
  public static <T> SingleObjectObserver<T> buildSingleObserver(FunctionCallback<T> callback) {
    return new SingleObjectObserver<>(callback);
  }

  /**
   * build observer for CloudQueryCallback
   * @param callback cloud query callback
   * @return observer
   */
  public static SingleObjectObserver<AVCloudQueryResult> buildSingleObserver(CloudQueryCallback callback) {
    return new SingleObjectObserver<>(callback);
  }

  /**
   * build observer for StatusCallback
   * @param callback status callback
   * @return observer
   */
  public static SingleObjectObserver<AVStatus> buildSingleObserver(StatusCallback callback) {
    return new SingleObjectObserver<>(callback);
  }

  /**
   * build observer for SendCallback
   * @param callback send callback
   * @return observer
   */
  public static SingleObjectObserver<AVNull> buildSingleObserver(SendCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }

  /**
   * build observer for GetDataCallback
   * @param callback get data callback
   * @return observer
   */
  public static SingleObjectObserver<byte[]> buildSingleObserver(GetDataCallback callback) {
    return new SingleObjectObserver<>(callback);
  }

  /**
   * build observer for GetDataStreamCallback
   * @param callback get data stream callback
   * @return observer
   */
  public static SingleObjectObserver<InputStream> buildSingleObserver(GetDataStreamCallback callback) {
    return new SingleObjectObserver<>(callback);
  }

  /************************************************
   * Multiple Objects Observer.
   ************************************************/

  /**
   * build observer for FindCallback
   * @param callback find callback
   * @param <T> template type
   * @return observer
   * @deprecated please use buildCollectionObserver(FindCallback<T> callback).
   */
  public static <T extends AVObject> CollectionObserver<T> buildSingleObserver(FindCallback<T> callback) {
    return new CollectionObserver<T>(callback);
  }

  /**
   * build observer for FindCallback
   * @param callback find callback
   * @param <T> template type
   * @return observer
   */
  public static <T extends AVObject> CollectionObserver<T> buildCollectionObserver(FindCallback<T> callback) {
    return new CollectionObserver<T>(callback);
  }

  /**
   * build observer for StatusListCallback
   * @param callback status list callback
   * @return observer
   */
  public static CollectionObserver<AVStatus> buildCollectionObserver(StatusListCallback callback) {
    return new CollectionObserver<>(callback);
  }
}
