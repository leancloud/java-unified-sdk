package cn.leancloud.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeakConcurrentHashMap<K, V> extends ConcurrentHashMap<K, List<V>> {

  private static final long serialVersionUID = 1L;

  private Map<K, Long> timeMap = new ConcurrentHashMap<K, Long>();
  private WeakConcurrentHashMapListener<K, V> listener = null;
  private long expiryInMillis;
  private boolean mapAlive = true;

  public WeakConcurrentHashMap() {
    this.expiryInMillis = 10000;
    initialize();
  }

  public WeakConcurrentHashMap(WeakConcurrentHashMapListener<K, V> listener) {
    this.listener = listener;
    this.expiryInMillis = 10000;
    initialize();
  }

  public WeakConcurrentHashMap(long expiryInMillis) {
    this.expiryInMillis = expiryInMillis;
    initialize();
  }

  public WeakConcurrentHashMap(long expiryInMillis, WeakConcurrentHashMapListener<K, V> listener) {
    this.expiryInMillis = expiryInMillis;
    this.listener = listener;
    initialize();
  }

  void initialize() {
    new CleanerThread().start();
  }

  public void registerRemovalListener(WeakConcurrentHashMapListener<K, V> listener) {
    this.listener = listener;
  }

  /**
   * @throws IllegalStateException if trying to insert values into map after quiting
   */
  public V addElement(K key, V value) {
    if (!mapAlive) {
      throw new IllegalStateException("WeakConcurrent Hashmap is no more alive.. Try creating a new one.");	// No I18N
    }
    if (super.containsKey(key)) {
      get(key).add(value);
    } else {
      Date date = new Date();
      timeMap.put(key, date.getTime());

      List<V> valist = new ArrayList<>();
      valist.add(value);
      put(key, valist);
    }
    if (listener != null) {
      listener.notifyOnAdd(key, value);
    }
    return value;
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalStateException if trying to insert values into map after quiting
   */
  public V addIfAbsent(K key, V value) {
    if (!mapAlive) {
      throw new IllegalStateException("WeakConcurrent Hashmap is no more alive.. Try creating a new one.");	// No I18N
    }
    if (!containsKey(key)) {
      return addElement(key, value);
    } else {
      return null;
    }
  }

  /**
   * Should call this method when it's no longer required
   */
  public void quitMap() {
    mapAlive = false;
  }

  public boolean isAlive() {
    return mapAlive;
  }

  /**
   *
   * This thread performs the cleaning operation on the concurrent hashmap once in a specified interval. This wait interval is half of the
   * time from the expiry time.
   *
   *
   */
  class CleanerThread extends Thread {

    @Override
    public void run() {
      while (mapAlive) {
        cleanMap();
        try {
          Thread.sleep(expiryInMillis / 5);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    private void cleanMap() {
      long currentTime = new Date().getTime();
      for (K key : timeMap.keySet()) {
        if (currentTime > (timeMap.get(key) + expiryInMillis)) {
          List<V> values = remove(key);
          timeMap.remove(key);
          if (listener != null && null != values) {
            for (V v:values) {
              listener.notifyOnRemoval(key, v);
            }
          }
        }
      }
    }
  }
}
