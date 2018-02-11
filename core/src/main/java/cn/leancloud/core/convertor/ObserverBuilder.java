package cn.leancloud.core.convertor;

import cn.leancloud.AVCallback;
import cn.leancloud.AVException;
import cn.leancloud.GetCallback;
import cn.leancloud.core.AVObject;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

import java.util.List;

public class ObserverBuilder {

  static class SingleObjectObserver<T extends AVObject> implements Observer<T> {
    private AVCallback callback;
    SingleObjectObserver(GetCallback<T> callback) {
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

  public static <T extends AVObject> SingleObjectObserver<T> buildSingleObserver(GetCallback<T> callback) {
    return new SingleObjectObserver<T>(callback);
  }
}
