package cn.leancloud.im.v2;

import cn.leancloud.AVQuery;
import cn.leancloud.query.QueryOperation;
import cn.leancloud.types.AVGeoPoint;

import java.util.*;

public class AVIMConversationsQuery {
  private AVIMClient client;
  AVIMConversationQueryConditions conditions;
  AVQuery.CachePolicy policy = AVQuery.CachePolicy.CACHE_ELSE_NETWORK;
  private static final long MAX_CONVERSATION_CACHE_TIME = 60 * 60 * 1000;
  private long maxAge = MAX_CONVERSATION_CACHE_TIME;

  protected AVIMConversationsQuery(AVIMClient client) {
    this.client = client;
    this.conditions = new AVIMConversationQueryConditions();
  }

  /**
   * 增加查询条件，指定聊天室的组员条件满足条件的才返回
   *
   * @param peerIds
   * @return
   */
  public AVIMConversationsQuery withMembers(List<String> peerIds) {
    return withMembers(peerIds, false);
  }

  /**
   * 增加查询条件，指定聊天室的组员条件满足条件的才返回
   *
   * @param peerIds
   * @param includeSelf  是否包含自己
   * @return
   */
  public AVIMConversationsQuery withMembers(List<String> peerIds, boolean includeSelf) {
    Set<String> targetPeerIds = new HashSet<String>(peerIds);
    if (includeSelf) {
      targetPeerIds.add(client.getClientId());
    }
    containsMembers(new LinkedList<String>(targetPeerIds));
    this.whereSizeEqual(Conversation.MEMBERS, targetPeerIds.size());
    return this;
  }

  /**
   * 增加查询条件，指定聊天室的组员包含某些成员即可返回
   *
   * @param peerIds
   * @return
   */

  public AVIMConversationsQuery containsMembers(List<String> peerIds) {
    conditions.addWhereItem(Conversation.MEMBERS, "$all", peerIds);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段满足等于条件时即可返回
   *
   * @param key
   * @param value
   * @return
   */
  public AVIMConversationsQuery whereEqualTo(String key, Object value) {
    conditions.whereEqualTo(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段满足小于条件时即可返回
   *
   * @param key
   * @param value
   * @return
   */
  public AVIMConversationsQuery whereLessThan(String key, Object value) {
    conditions.whereLessThan(key, value);
    return this;
  }


  /**
   * 增加查询条件，当conversation的属性中对应的字段满足小于等于条件时即可返回
   *
   * @param key
   * @param value
   * @return
   */
  public AVIMConversationsQuery whereLessThanOrEqualsTo(String key, Object value) {
    conditions.whereLessThanOrEqualTo(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段满足大于条件时即可返回
   *
   * @param key
   * @param value
   * @return
   */

  public AVIMConversationsQuery whereGreaterThan(String key, Object value) {
    conditions.whereGreaterThan(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段满足大于等于条件时即可返回
   *
   * @param key
   * @param value
   * @return
   */

  public AVIMConversationsQuery whereGreaterThanOrEqualsTo(String key, Object value) {
    conditions.whereGreaterThanOrEqualTo(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段满足不等于条件时即可返回
   *
   * @param key
   * @param value
   * @return
   */
  public AVIMConversationsQuery whereNotEqualsTo(String key, Object value) {
    conditions.whereNotEqualTo(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值包含在指定值中时即可返回
   *
   * @param key
   * @param value
   * @return
   */

  public AVIMConversationsQuery whereContainsIn(String key, Collection<?> value) {
    conditions.whereContainedIn(key, value);
    return this;
  }

  /**
   * 增加查询条件，当 conversation 的属性中对应的字段有值时即可返回
   *
   * @param key The key that should exist.
   */
  public AVIMConversationsQuery whereExists(String key) {
    conditions.whereExists(key);
    return this;
  }

  /**
   * 增加查询条件，当 conversation 的属性中对应的字段没有值时即可返回
   * @param key
   * @return
   */
  public AVIMConversationsQuery whereDoesNotExist(String key) {
    conditions.whereDoesNotExist(key);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值不包含在指定值中时即可返回
   *
   * @param key
   * @param value
   * @return
   */

  public AVIMConversationsQuery whereNotContainsIn(String key, Collection<?> value) {
    conditions.whereNotContainedIn(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段中的元素包含所有的值才可返回
   *
   * @param key
   * @param values
   * @return
   */

  public AVIMConversationsQuery whereContainsAll(String key, Collection<?> values) {
    conditions.whereContainsAll(key, values);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值包含此字符串即可返回
   *
   * @param key
   * @param subString
   * @return
   */
  public AVIMConversationsQuery whereContains(String key, String subString) {
    conditions.whereContains(key, subString);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值以此字符串起始即可返回
   *
   * @param key
   * @param prefix
   * @return
   */

  public AVIMConversationsQuery whereStartsWith(String key, String prefix) {
    conditions.whereStartsWith(key, prefix);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值以此字符串结束即可返回
   *
   * @param key
   * @param suffix
   * @return
   */
  public AVIMConversationsQuery whereEndsWith(String key, String suffix) {
    conditions.whereEndsWith(key, suffix);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值满足提供的正则表达式即可返回
   *
   * @param key
   * @param regex
   * @return
   */
  public AVIMConversationsQuery whereMatches(String key, String regex) {
    conditions.whereMatches(key, regex);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值满足提供的正则表达式即可返回
   *
   * @param key
   * @param regex
   * @param modifiers 正则表达式的匹配模式，比如"-i"表示忽视大小写区分等
   * @return
   */
  public AVIMConversationsQuery whereMatches(String key, String regex, String modifiers) {
    conditions.whereMatches(key, regex, modifiers);
    return this;
  }

  /**
   * 增加一个基于地理位置的近似查询，当conversation的属性中对应字段对应的地理位置在pointer附近时即可返回
   *
   * @param key
   * @param point
   * @return
   */

  public AVIMConversationsQuery whereNear(String key, AVGeoPoint point) {
    conditions.whereNear(key, point);
    return this;
  }

  /**
   * 增加一个基于地理位置的查询，当conversation的属性中有对应字段对应的地址位置在指定的矩形区域内时即可返回
   *
   * @param key       查询字段
   * @param southwest 矩形区域的左下角坐标
   * @param northeast 去兴趣鱼的右上角坐标
   * @return
   */
  public AVIMConversationsQuery whereWithinGeoBox(String key, AVGeoPoint southwest,
                                                  AVGeoPoint northeast) {
    conditions.whereWithinGeoBox(key, southwest, northeast);
    return this;
  }

  /**
   * 增加一个基于地理位置的近似查询，当conversation的属性中有对应的地址位置与指定的地理位置间距不超过指定距离时返回
   * <p/>
   * 地球半径为6371.0 千米
   *
   * @param key
   * @param point       指定的地理位置
   * @param maxDistance 距离，以千米计算
   * @return
   */
  public AVIMConversationsQuery whereWithinKilometers(String key, AVGeoPoint point,
                                                      double maxDistance) {
    conditions.whereWithinKilometers(key, point, maxDistance);
    return this;
  }

  /**
   * 增加一个基于地理位置的近似查询，当conversation的属性中有对应的地址位置与指定的地理位置间距不超过指定距离时返回
   *
   * @param key
   * @param point       指定的地理位置
   * @param maxDistance 距离，以英里计算
   * @return
   */

  public AVIMConversationsQuery whereWithinMiles(String key, AVGeoPoint point, double maxDistance) {
    conditions.whereWithinMiles(key, point, maxDistance);
    return this;
  }

  /**
   * 增加一个基于地理位置的近似查询，当conversation的属性中有对应的地址位置与指定的地理位置间距不超过指定距离时返回
   *
   * @param key
   * @param point       指定的地理位置
   * @param maxDistance 距离，以角度计算
   * @return
   */

  public AVIMConversationsQuery whereWithinRadians(String key, AVGeoPoint point, double maxDistance) {
    conditions.whereWithinRadians(key, point, maxDistance);
    return this;
  }

  /**
   * 设置返回集合的大小上限
   *
   * @param limit 上限
   * @return
   */
  public AVIMConversationsQuery setLimit(int limit) {
    conditions.setLimit(limit);
    return this;
  }

  /**
   * 设置返回集合的大小上限
   *
   * @param limit 上限
   * @return
   */
  public AVIMConversationsQuery limit(int limit) {
    return this.setLimit(limit);
  }

  /**
   * 设置返回集合的起始位置，一般用于分页
   *
   * @param skip 起始位置跳过几个对象
   * @return
   */
  public AVIMConversationsQuery setSkip(int skip) {
    conditions.setSkip(skip);
    return this;
  }

  /**
   * 设置返回集合的起始位置，一般用于分页
   *
   * @param skip 起始位置跳过几个对象
   * @return
   */
  public AVIMConversationsQuery skip(int skip) {
    return this.setSkip(skip);
  }

  /**
   * 设置返回集合按照指定key进行增序排列
   *
   * @param key
   * @return
   */
  public AVIMConversationsQuery orderByAscending(String key) {
    conditions.orderByAscending(key);
    return this;
  }

  /**
   * 设置返回集合按照指定key进行降序排列
   *
   * @param key
   * @return
   */

  public AVIMConversationsQuery orderByDescending(String key) {
    conditions.orderByDescending(key);
    return this;
  }

  /**
   * 设置返回集合按照指定key进行升序排列，此 key 的优先级小于先前设置的 key
   *
   * @param key
   * @return
   */
  public AVIMConversationsQuery addAscendingOrder(String key) {
    conditions.addAscendingOrder(key);
    return this;
  }

  /**
   * 设置返回集合按照指定key进行降序排列，此 key 的优先级小于先前设置的 key
   *
   * @param key
   * @return
   */
  public AVIMConversationsQuery addDescendingOrder(String key) {
    conditions.addDescendingOrder(key);
    return this;
  }

  /**
   * 添加查询约束条件，查找key类型是数组，该数组的长度匹配提供的数值
   *
   * @param key
   * @param size
   * @return
   */
  public AVIMConversationsQuery whereSizeEqual(String key, int size) {
    conditions.whereSizeEqual(key, size);
    return this;
  }


  /**
   * 是否携带最后一条消息
   *
   * @return
   */
  public boolean isWithLastMessagesRefreshed() {
    return conditions.isWithLastMessagesRefreshed();
  }

  /**
   * 设置是否携带最后一条消息
   *
   * @param isWithLastMessageRefreshed
   */
  public AVIMConversationsQuery setWithLastMessagesRefreshed(boolean isWithLastMessageRefreshed) {
    conditions.setWithLastMessagesRefreshed(isWithLastMessageRefreshed);
    return this;
  }

  /**
   * 设置 AVIMConversationsQuery 的查询策略
   *
   * @param policy
   */
  public void setQueryPolicy(AVQuery.CachePolicy policy) {
    this.policy = policy;
  }

  /**
   * Constructs a AVIMConversationsQuery that is the or of the given queries.
   *
   * @param queries
   * @return
   */

  public static AVIMConversationsQuery or(List<AVIMConversationsQuery> queries) {
    if (null == queries || 0 == queries.size()) {
      throw new IllegalArgumentException("Queries cannot be empty");
    }
    AVIMClient client = queries.get(0).client;
    AVIMConversationsQuery result = new AVIMConversationsQuery(client);
    for (AVIMConversationsQuery query : queries) {
      if (!client.getClientId().equals(query.client.getClientId())) {
        throw new IllegalArgumentException("All queries must be for the same client");
      }
      result.conditions.addOrItems(new QueryOperation("$or", "$or", query.conditions
              .compileWhereOperationMap()));
    }
    return result;
  }

  /**
   * 设置查询缓存的有效时间
   * @param maxAgeInSecond
   */
  public void setCacheMaxAge(long maxAgeInSecond){
    this.maxAge = maxAgeInSecond * 1000;
  }

  public long getCacheMaxAge(){
    return maxAge/1000;
  }
}
