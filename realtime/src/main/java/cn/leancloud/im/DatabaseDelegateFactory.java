package cn.leancloud.im;

public interface DatabaseDelegateFactory {
  DatabaseDelegate createInstance(String clientId);
}
