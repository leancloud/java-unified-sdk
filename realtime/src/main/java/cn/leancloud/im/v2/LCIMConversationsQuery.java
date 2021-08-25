package cn.leancloud.im.v2;

import cn.leancloud.*;
import cn.leancloud.cache.QueryResultCache;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.im.InternalConfiguration;
import cn.leancloud.im.v2.callback.LCIMCommonJsonCallback;
import cn.leancloud.im.v2.callback.LCIMConversationQueryCallback;
import cn.leancloud.query.QueryOperation;
import cn.leancloud.types.LCGeoPoint;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

import java.util.*;

public class LCIMConversationsQuery {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCIMConversationsQuery.class);

  private static final String CONVERSATION_CLASS_NAME = "_conversation";
  private LCIMClient client;
  LCIMConversationQueryConditions conditions;
  LCQuery.CachePolicy policy = LCQuery.CachePolicy.CACHE_ELSE_NETWORK;
  private static final long MAX_CONVERSATION_CACHE_TIME = 60 * 60 * 1000;
  private long maxAge = MAX_CONVERSATION_CACHE_TIME;

  protected LCIMConversationsQuery(LCIMClient client) {
    this.client = client;
    this.conditions = new LCIMConversationQueryConditions();
  }

  /**
   * 增加查询条件，指定聊天室的组员条件满足条件的才返回
   *
   * @param peerIds peer id list.
   * @return current instance.
   */
  public LCIMConversationsQuery withMembers(List<String> peerIds) {
    return withMembers(peerIds, false);
  }

  /**
   * 增加查询条件，指定聊天室的组员条件满足条件的才返回
   *
   * @param peerIds peer client id list.
   * @param includeSelf  是否包含自己
   * @return current instance.
   */
  public LCIMConversationsQuery withMembers(List<String> peerIds, boolean includeSelf) {
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
   * @param peerIds peer client id list.
   * @return current instance.
   */

  public LCIMConversationsQuery containsMembers(List<String> peerIds) {
    conditions.addWhereItem(Conversation.MEMBERS, "$all", peerIds);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段满足等于条件时即可返回
   *
   * @param key     attribute key.
   * @param value   attribute value
   * @return current instance.
   */
  public LCIMConversationsQuery whereEqualTo(String key, Object value) {
    conditions.whereEqualTo(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段满足小于条件时即可返回
   *
   * @param key     attribute key.
   * @param value   attribute value
   * @return current instance.
   */
  public LCIMConversationsQuery whereLessThan(String key, Object value) {
    conditions.whereLessThan(key, value);
    return this;
  }


  /**
   * 增加查询条件，当conversation的属性中对应的字段满足小于等于条件时即可返回
   *
   * @param key     attribute key.
   * @param value   attribute value.
   * @return current instance.
   */
  public LCIMConversationsQuery whereLessThanOrEqualsTo(String key, Object value) {
    conditions.whereLessThanOrEqualTo(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段满足大于条件时即可返回
   *
   * @param key     attribute key.
   * @param value   attribute value.
   * @return current instance.
   */

  public LCIMConversationsQuery whereGreaterThan(String key, Object value) {
    conditions.whereGreaterThan(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段满足大于等于条件时即可返回
   *
   * @param key    attribute key.
   * @param value  attribute value.
   * @return current instance.
   */

  public LCIMConversationsQuery whereGreaterThanOrEqualsTo(String key, Object value) {
    conditions.whereGreaterThanOrEqualTo(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段满足不等于条件时即可返回
   *
   * @param key   attribute key.
   * @param value attribute value.
   * @return current instance.
   */
  public LCIMConversationsQuery whereNotEqualsTo(String key, Object value) {
    conditions.whereNotEqualTo(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值包含在指定值中时即可返回
   *
   * @param key     attribute key.
   * @param value   value collection.
   * @return current instance.
   * Notice: equals to whereContainedIn
   */

  public LCIMConversationsQuery whereContainsIn(String key, Collection<?> value) {
    conditions.whereContainedIn(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值包含在指定值中时即可返回
   *
   * @param key     attribute key.
   * @param value   value collection.
   * @return current instance.
   */
  public LCIMConversationsQuery whereContainedIn(String key, Collection<?> value) {
    conditions.whereContainedIn(key, value);
    return this;
  }

  /**
   * 增加查询条件，当 conversation 的属性中对应的字段有值时即可返回
   *
   * @param key The key that should exist.
   * @return current instance.
   */
  public LCIMConversationsQuery whereExists(String key) {
    conditions.whereExists(key);
    return this;
  }

  /**
   * 增加查询条件，当 conversation 的属性中对应的字段没有值时即可返回
   * @param key attribute key.
   * @return current instance.
   */
  public LCIMConversationsQuery whereDoesNotExist(String key) {
    conditions.whereDoesNotExist(key);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值不包含在指定值中时即可返回
   *
   * @param key      attribute key.
   * @param value    attribute values
   * @return current instance.
   */

  public LCIMConversationsQuery whereNotContainsIn(String key, Collection<?> value) {
    conditions.whereNotContainedIn(key, value);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段中的元素包含所有的值才可返回
   *
   * @param key      attribute key.
   * @param values   attribute values.
   * @return current instance.
   */

  public LCIMConversationsQuery whereContainsAll(String key, Collection<?> values) {
    conditions.whereContainsAll(key, values);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值包含此字符串即可返回
   *
   * @param key          attribute key.
   * @param subString    sub string.
   * @return current instance.
   */
  public LCIMConversationsQuery whereContains(String key, String subString) {
    conditions.whereContains(key, subString);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值以此字符串起始即可返回
   *
   * @param key     attribute key.
   * @param prefix  prefix string
   * @return current instance.
   */

  public LCIMConversationsQuery whereStartsWith(String key, String prefix) {
    conditions.whereStartsWith(key, prefix);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值以此字符串结束即可返回
   *
   * @param key     attribute key.
   * @param suffix  suffix string
   * @return current instance.
   */
  public LCIMConversationsQuery whereEndsWith(String key, String suffix) {
    conditions.whereEndsWith(key, suffix);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值满足提供的正则表达式即可返回
   *
   * @param key       attribute key.
   * @param regex     regex pattern
   * @return current instance.
   */
  public LCIMConversationsQuery whereMatches(String key, String regex) {
    conditions.whereMatches(key, regex);
    return this;
  }

  /**
   * 增加查询条件，当conversation的属性中对应的字段对应的值满足提供的正则表达式即可返回
   *
   * @param key       attribute key.
   * @param regex     regex pattern
   * @param modifiers 正则表达式的匹配模式，比如"-i"表示忽视大小写区分等
   * @return current instance.
   */
  public LCIMConversationsQuery whereMatches(String key, String regex, String modifiers) {
    conditions.whereMatches(key, regex, modifiers);
    return this;
  }

  /**
   * 增加一个基于地理位置的近似查询，当conversation的属性中对应字段对应的地理位置在pointer附近时即可返回
   *
   * @param key     attribute key.
   * @param point   GeoPoint
   * @return current instance.
   */

  public LCIMConversationsQuery whereNear(String key, LCGeoPoint point) {
    conditions.whereNear(key, point);
    return this;
  }

  /**
   * 增加一个基于地理位置的查询，当conversation的属性中有对应字段对应的地址位置在指定的矩形区域内时即可返回
   *
   * @param key       查询字段
   * @param southwest 矩形区域的左下角坐标
   * @param northeast 去兴趣鱼的右上角坐标
   * @return current instance.
   */
  public LCIMConversationsQuery whereWithinGeoBox(String key, LCGeoPoint southwest,
                                                  LCGeoPoint northeast) {
    conditions.whereWithinGeoBox(key, southwest, northeast);
    return this;
  }

  /**
   * 增加一个基于地理位置的近似查询，当conversation的属性中有对应的地址位置与指定的地理位置间距不超过指定距离时返回
   *
   * 地球半径为6371.0 千米
   *
   * @param key         attribute key.
   * @param point       指定的地理位置
   * @param maxDistance 距离，以千米计算
   * @return current instance.
   */
  public LCIMConversationsQuery whereWithinKilometers(String key, LCGeoPoint point,
                                                      double maxDistance) {
    conditions.whereWithinKilometers(key, point, maxDistance);
    return this;
  }

  /**
   * 增加一个基于地理位置的近似查询，当conversation的属性中有对应的地址位置与指定的地理位置间距不超过指定距离时返回
   *
   * @param key         attribute key.
   * @param point       指定的地理位置
   * @param maxDistance 距离，以英里计算
   * @return current instance.
   */

  public LCIMConversationsQuery whereWithinMiles(String key, LCGeoPoint point, double maxDistance) {
    conditions.whereWithinMiles(key, point, maxDistance);
    return this;
  }

  /**
   * 增加一个基于地理位置的近似查询，当conversation的属性中有对应的地址位置与指定的地理位置间距不超过指定距离时返回
   *
   * @param key         attribute key.
   * @param point       指定的地理位置
   * @param maxDistance 距离，以角度计算
   * @return current instance.
   */

  public LCIMConversationsQuery whereWithinRadians(String key, LCGeoPoint point, double maxDistance) {
    conditions.whereWithinRadians(key, point, maxDistance);
    return this;
  }

  /**
   * 设置返回集合的大小上限
   *
   * @param limit 上限
   * @return current instance.
   */
  public LCIMConversationsQuery setLimit(int limit) {
    conditions.setLimit(limit);
    return this;
  }

  /**
   * 设置返回集合的大小上限
   *
   * @param limit 上限
   * @return current instance.
   */
  public LCIMConversationsQuery limit(int limit) {
    return this.setLimit(limit);
  }

  /**
   * 设置返回集合的起始位置，一般用于分页
   *
   * @param skip 起始位置跳过几个对象
   * @return current instance.
   */
  public LCIMConversationsQuery setSkip(int skip) {
    conditions.setSkip(skip);
    return this;
  }

  /**
   * 设置返回集合的起始位置，一般用于分页
   *
   * @param skip 起始位置跳过几个对象
   * @return current instance.
   */
  public LCIMConversationsQuery skip(int skip) {
    return this.setSkip(skip);
  }

  /**
   * 设置返回集合按照指定key进行增序排列
   *
   * @param key attribute key.
   * @return current instance.
   */
  public LCIMConversationsQuery orderByAscending(String key) {
    conditions.orderByAscending(key);
    return this;
  }

  /**
   * 设置返回集合按照指定key进行降序排列
   *
   * @param key attribute key.
   * @return current instance.
   */

  public LCIMConversationsQuery orderByDescending(String key) {
    conditions.orderByDescending(key);
    return this;
  }

  /**
   * 设置返回集合按照指定key进行升序排列，此 key 的优先级小于先前设置的 key
   *
   * @param key attribute key.
   * @return current instance.
   */
  public LCIMConversationsQuery addAscendingOrder(String key) {
    conditions.addAscendingOrder(key);
    return this;
  }

  /**
   * 设置返回集合按照指定key进行降序排列，此 key 的优先级小于先前设置的 key
   *
   * @param key attribute key.
   * @return current instance.
   */
  public LCIMConversationsQuery addDescendingOrder(String key) {
    conditions.addDescendingOrder(key);
    return this;
  }

  /**
   * 添加查询约束条件，查找key类型是数组，该数组的长度匹配提供的数值
   *
   * @param key attribute key.
   * @param size size value.
   * @return current instance.
   */
  public LCIMConversationsQuery whereSizeEqual(String key, int size) {
    conditions.whereSizeEqual(key, size);
    return this;
  }


  /**
   * 是否携带最后一条消息
   *
   * @return flag indicating attaches with latest message.
   */
  public boolean isWithLastMessagesRefreshed() {
    return conditions.isWithLastMessagesRefreshed();
  }

  /**
   * 设置是否携带最后一条消息
   *
   * @param isWithLastMessageRefreshed flag indicating attaches with latest message.
   * @return current instance.
   */
  public LCIMConversationsQuery setWithLastMessagesRefreshed(boolean isWithLastMessageRefreshed) {
    conditions.setWithLastMessagesRefreshed(isWithLastMessageRefreshed);
    return this;
  }

  /**
   * 是否返回成员列表
   * @param isCompact 为 true 的话则不返回，为 false 的话则返回成员列表，默认为 false
   * @return current instance.
   */
  public LCIMConversationsQuery setCompact(boolean isCompact) {
    conditions.setCompact(isCompact);
    return this;
  }

  /**
   * 设置 IMConversationsQuery 的查询策略
   *
   * @param policy query policy
   */
  public void setQueryPolicy(LCQuery.CachePolicy policy) {
    this.policy = policy;
  }

  /**
   * Constructs a IMConversationsQuery that is the or of the given queries.
   *
   * @param queries query list.
   * @return new conversation query instance.
   */

  public static LCIMConversationsQuery or(List<LCIMConversationsQuery> queries) {
    if (null == queries || 0 == queries.size()) {
      throw new IllegalArgumentException("Queries cannot be empty");
    }
    LCIMClient client = queries.get(0).client;
    LCIMConversationsQuery result = new LCIMConversationsQuery(client);
    for (LCIMConversationsQuery query : queries) {
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
   * @param maxAgeInSecond max age of cache in seconds.
   */
  public void setCacheMaxAge(long maxAgeInSecond){
    this.maxAge = maxAgeInSecond * 1000;
  }

  public long getCacheMaxAge(){
    return maxAge/1000;
  }

  /**
   * find in background.
   * @param callback callback handler.
   */
  public void findInBackground(final LCIMConversationQueryCallback callback) {
    final Map<String, String> queryParams = conditions.assembleParameters();
    // always fetch from network while lastMessage is necessary.
    if (conditions.isWithLastMessagesRefreshed()) {
      if (LCQuery.CachePolicy.CACHE_ELSE_NETWORK == this.policy
              || LCQuery.CachePolicy.CACHE_THEN_NETWORK == this.policy) {
        this.policy = LCQuery.CachePolicy.NETWORK_ELSE_CACHE;
      }
    }
    findWithCondition(queryParams, callback);
  }

  /**
   * find temporary conversations in background.
   * @param conversationIds conversation id list.
   * @param callback callback handler.
   */
  public void findTempConversationsInBackground(List<String> conversationIds, final LCIMConversationQueryCallback callback) {
    this.conditions.setTempConversationIds(conversationIds);
    findInBackground(callback);
  }

  private void findWithCondition(final Map<String, String> queryParams, final LCIMConversationQueryCallback callback) {
    switch (policy) {
      case CACHE_THEN_NETWORK:
      case CACHE_ELSE_NETWORK:
        queryFromCache(new LCIMConversationQueryCallback() {
          @Override
          public void done(List<LCIMConversation> conversations, LCIMException e) {
            if (null != e) {
              LOGGER.d("failed to query cache. cause:" + e.getMessage());
              queryFromNetwork(callback, queryParams);
            } else if (null != callback ){
              callback.internalDone(conversations, null);
            }
          }
        }, queryParams);
        break;
      case NETWORK_ELSE_CACHE:
        if (AppConfiguration.getGlobalNetworkingDetector().isConnected()) {
          queryFromNetwork(callback, queryParams);
        } else {
          queryFromCache(callback, queryParams);
        }
        break;
      case CACHE_ONLY:
        queryFromCache(callback, queryParams);
        break;
      case NETWORK_ONLY:
      case IGNORE_CACHE:
        queryFromNetwork(callback, queryParams);
        break;
    }
  }

  /**
   * direct find with conditions in background.
   * @param where query condition
   * @param sort sort attributes
   * @param skip skip number
   * @param limit result maximum size
   * @param flag query flag:
   *            0 - Normal,
   *            1 - don't need member list within a conversation item,
   *            2 - attach last message data within a conversation item.
   * @param callback callback function.
   */
  public void directFindInBackground(String where, String sort, int skip, int limit, int flag,
                                     final LCIMConversationQueryCallback callback) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("where", where);
    if (!StringUtil.isEmpty(sort)) {
      queryParams.put("order", sort);
    }
    if (skip > 0) {
      queryParams.put("skip", Integer.toString(skip));
    }
    if (limit > 0) {
      queryParams.put("limit", Integer.toString(limit));
    }
    queryParams = LCIMConversationQueryConditions.assembleParameters(queryParams, flag);
    if (LCIMConversationQueryConditions.isWithLastMessagesRefreshed(flag)) {
      // always fetch from network while lastMessage is necessary.
      this.policy = LCQuery.CachePolicy.NETWORK_ELSE_CACHE;
    }
    findWithCondition(queryParams, callback);
  }

  private void queryFromCache(final LCIMConversationQueryCallback callback,
                              final Map<String, String> queryParams) {
    QueryResultCache.getInstance().getCacheRawResult(CONVERSATION_CLASS_NAME, queryParams,
            maxAge, true).map(new Function<String, List<LCIMConversation>>() {
              @Override
              public List<LCIMConversation> apply(@NonNull String content) throws Exception {
                LOGGER.d("map function. input: " + content);
                List<String> conversationList = JSON.parseObject(content, List.class);
                List<LCIMConversation> conversations =
                        client.getStorage().getCachedConversations(conversationList);
                LOGGER.d("map function. output: " + conversations.size());
                if (conversations.size() < conversationList.size()) {
                  throw new LCIMException(LCException.CACHE_MISS, "missing conversation cache in database");
                }
                return conversations;
              }
            }).subscribe(new Observer<List<LCIMConversation>>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(List<LCIMConversation> LCIMConversations) {
        if (null != callback) {
          callback.internalDone(LCIMConversations, null);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        if (null != callback) {
          callback.internalDone(null, new LCException(throwable));
        }
      }

      @Override
      public void onComplete() {
      }
    });

  }

  private void queryFromNetwork(final LCIMConversationQueryCallback callback,
                                final Map<String, String> queryParams) {
    if (!AppConfiguration.getGlobalNetworkingDetector().isConnected()) {
      if (callback != null) {
        callback.internalDone(null, new LCException(LCException.CONNECTION_FAILED,
                "Connection lost"));
      }
      return;
    }

    final String queryParamsString = JSON.toJSONString(queryParams);
    final LCIMCommonJsonCallback tmpCallback = new LCIMCommonJsonCallback() {
      @Override
      public void done(Map<String, Object> result, LCIMException e) {
        List<LCIMConversation> conversations = null;
        if (null != result) {
          Object callbackData = result.get(Conversation.callbackData);
          if (callbackData instanceof List) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) callbackData;
            conversations = parseQueryResult(content);
            if (null != conversations && conversations.size() > 0) {
              cacheQueryResult(queryParams, conversations);
            }
          } else if (callbackData instanceof String) {
            conversations = parseQueryResult(JSON.parseObject(String.valueOf(callbackData), List.class));
            if (null != conversations && conversations.size() > 0) {
              cacheQueryResult(queryParams, conversations);
            }
          }
        }
        if (null != callback) {
          callback.internalDone(conversations, e);
        }
      }
    };

    InternalConfiguration.getOperationTube().queryConversations(this.client.getConnectionManager(),
            this.client.getClientId(), queryParamsString, tmpCallback);
  }

  private void cacheQueryResult(final Map<String, String> queryParams, List<LCIMConversation> conversations) {
    List<String> conversationList = new LinkedList<String>();
    LCIMMessageStorage storage = null;
    for (LCIMConversation conversation : conversations) {
      conversationList.add(conversation.getConversationId());
      storage = conversation.storage;
    }
    if (storage != null) {
      storage.insertConversations(conversations);
    } else {
      LOGGER.d("Message Storage is null, skip save queryResult.");
    }
    String cacheKey = QueryResultCache.generateKeyForQueryCondition(CONVERSATION_CLASS_NAME, queryParams);
    QueryResultCache.getInstance().cacheResult(cacheKey, JSON.toJSONString(conversationList));
  }

  private List<LCIMConversation> parseQueryResult(List<Map<String, Object>> content) {
    List<LCIMConversation> conversations = new LinkedList<LCIMConversation>();
    for (int i = 0; i < content.size(); i++) {
      Map<String, Object> jsonObject = content.get(i);
      LCIMConversation allNewConversation = LCIMConversation.parseFromJson(client, jsonObject);
      if (null != allNewConversation) {
        LCIMConversation convResult = client.mergeConversationCache(allNewConversation, false, jsonObject);
        if (null != convResult) {
          conversations.add(convResult);
        }
      }
    }
    return conversations;
  }
}
