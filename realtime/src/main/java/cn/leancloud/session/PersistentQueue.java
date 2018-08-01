package cn.leancloud.session;

import cn.leancloud.cache.SystemSetting;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PersistentQueue<E> implements Queue<E> {
  Queue<E> messages;
  private final String queueKey;
  private static final String MESSAGE_ZONE = "com.avoscloud.chat.message";
  private static final String QUEUE_KEY = "com.avoscloud.chat.message.queue";
  private final Class<E> type;

  public PersistentQueue(String peerId, Class<E> clazz) {
    messages = new ConcurrentLinkedQueue<>();
    this.type = clazz;
    queueKey = QUEUE_KEY + "." + peerId;
    LinkedList<E> storedMessages = restoreMessageQueue();
    if (null != storedMessages && !storedMessages.isEmpty()) {
      messages.addAll(storedMessages);
    }
  }

  @Override
  public boolean addAll(Collection<? extends E> collection) {
    boolean result = messages.addAll(collection);
    this.storeMessageQueue();
    return result;
  }

  @Override
  public void clear() {
    messages.clear();
    this.storeMessageQueue();
  }

  @Override
  public boolean contains(Object object) {
    return messages.contains(object);
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    return messages.containsAll(collection);
  }

  @Override
  public boolean isEmpty() {
    return messages.isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    return messages.iterator();
  }

  @Override
  public boolean remove(Object object) {
    boolean result = messages.remove(object);
    this.storeMessageQueue();
    return result;
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    boolean result = messages.removeAll(collection);
    this.storeMessageQueue();
    return result;
  }

  @Override
  public boolean retainAll(Collection<?> collection) {
    boolean result = messages.retainAll(collection);
    this.storeMessageQueue();
    return result;
  }

  @Override
  public int size() {
    return messages.size();
  }

  @Override
  public Object[] toArray() {
    return messages.toArray();
  }

  @Override
  public <T> T[] toArray(T[] array) {
    return messages.toArray(array);
  }

  @Override
  public boolean add(E e) {
    boolean result = messages.add(e);
    this.storeMessageQueue();
    return result;
  }

  @Override
  public boolean offer(E e) {
    boolean result = messages.offer(e);
    this.storeMessageQueue();
    return result;
  }

  @Override
  public E remove() {
    E result = messages.remove();
    this.storeMessageQueue();
    return result;
  }

  @Override
  public E poll() {
    E result = messages.poll();
    this.storeMessageQueue();
    return result;
  }

  @Override
  public E element() {
    E result = messages.element();
    this.storeMessageQueue();
    return result;
  }

  @Override
  public E peek() {
    return messages.peek();
  }

  private void storeMessageQueue() {
    // 异步序列化，保证效率
    String queueString =
            JSON.toJSONString(messages, SerializerFeature.SkipTransientField,
                    SerializerFeature.WriteClassName, SerializerFeature.QuoteFieldNames,
                    SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullBooleanAsFalse);
    AppConfiguration.getDefaultSetting().saveString(MESSAGE_ZONE, queueKey, queueString);
  }

  private synchronized LinkedList<E> restoreMessageQueue() {
    LinkedList<E> storedMessages = new LinkedList<E>();
    SystemSetting setting = AppConfiguration.getDefaultSetting();
    String queueString = setting.getString(MESSAGE_ZONE, queueKey, null);
    if (!StringUtil.isEmpty(queueString)) {
      try {
        storedMessages.addAll(JSON.parseArray(queueString, type));
      } catch (Exception e) {
        // clean it since there's parse exception
        setting.removeKey(MESSAGE_ZONE, queueKey);
      }
    }
    return storedMessages;
  }
  public interface HasId {
    String getId();
    void setId(String id);
  }
}
