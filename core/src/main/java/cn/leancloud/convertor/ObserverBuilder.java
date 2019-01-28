package cn.leancloud.convertor;

import cn.leancloud.AVUser;
import cn.leancloud.callback.*;
import cn.leancloud.AVException;
import cn.leancloud.AVObject;
import cn.leancloud.types.AVNull;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

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

  /**
   * Single Object Observer.
   */

  public static <T extends AVObject> SingleObjectObserver<T> buildSingleObserver(GetCallback<T> callback) {
    return new SingleObjectObserver<T>(callback);
  }
  public static <T extends AVObject> SingleObjectObserver<T> buildSingleObserver(SaveCallback<T> callback) {
    return new SingleObjectObserver<T>(callback);
  }
  public static SingleObjectObserver<AVNull> buildSingleObserver(DeleteCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }
  public static <T extends AVObject> SingleObjectObserver<T> buildSingleObserver(RefreshCallback<T> callback) {
    return new SingleObjectObserver<T>(callback);
  }
  public static <T extends AVUser> SingleObjectObserver<T> buildSingleObserver(LogInCallback<T> callback) {
    return new SingleObjectObserver<T>(callback);
  }
  public static SingleObjectObserver<AVUser> buildSingleObserver(SignUpCallback callback) {
    return new SingleObjectObserver<AVUser>(callback);
  }
  public static SingleObjectObserver<AVNull> buildSingleObserver(RequestEmailVerifyCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }
  public static SingleObjectObserver<AVNull> buildSingleObserver(RequestMobileCodeCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }
  public static SingleObjectObserver<JSONObject> buildSingleObserver(FollowersAndFolloweesCallback callback) {
    return new SingleObjectObserver<>(callback);
  }
  public static SingleObjectObserver<AVNull> buildSingleObserver(RequestPasswordResetCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }
  public static SingleObjectObserver<Integer> buildSingleObserver(CountCallback callback) {
    return new SingleObjectObserver<Integer>(callback);
  }

  public static SingleObjectObserver<AVNull> buildSingleObserver(UpdatePasswordCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }
  public static SingleObjectObserver<AVNull> buildSingleObserver(MobilePhoneVerifyCallback callback) {
    return new SingleObjectObserver<AVNull>(callback);
  }
  /**
   * Multiple Objects Observer.
   */

  public static <T extends AVObject> CollectionObserver<T> buildSingleObserver(FindCallback<T> callback) {
    return new CollectionObserver<T>(callback);
  }
}
