package cn.leancloud;

import cn.leancloud.core.PaasClient;
import cn.leancloud.json.JSONObject;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LCLeaderboard {
    public static int INVALID_VERSION = -1;
    public static final String MEMBER_TYPE_USER = "_User";
    public static final String MEMBER_TYPE_ENTITY = "_Entity";

    static final String ATTR_STATISTIC_NAME = "statisticName";
    static final String ATTR_MEMBER_TYPE = "memberType";
    static final String ATTR_UPDATE_STRATEGY = "updateStrategy";
    static final String ATTR_ORDER = "order";
    static final String ATTR_VERSION_CHANGE_INTERVAL = "versionChangeInterval";
    static final String ATTR_VERSION = "version";
    static final String ATTR_EXPIRED_AT = "expiredAt";

    public enum LCLeaderboardOrder {
        Ascending,
        Descending
    }
    public enum LCLeaderboardUpdateStrategy {
        Better,
        Last,
        Sum
    }
    public enum LCLeaderboardVersionChangeInterval {
        Never,
        Day,
        Week,
        Month
    }
    private String memberType = MEMBER_TYPE_USER;
    private String statisticName = null;
    private LCLeaderboardOrder order = LCLeaderboardOrder.Ascending;
    private LCLeaderboardUpdateStrategy updateStrategy = LCLeaderboardUpdateStrategy.Better;
    private LCLeaderboardVersionChangeInterval versionChangeInterval = LCLeaderboardVersionChangeInterval.Never;
    private int version = INVALID_VERSION;
    private Date nextResetAt = null;
    private Date createdAt = null;

    public String getStatisticName() {
        return statisticName;
    }

    public LCLeaderboardOrder getOrder() {
        return order;
    }

    public LCLeaderboardUpdateStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    public LCLeaderboardVersionChangeInterval getVersionChangeInterval() {
        return versionChangeInterval;
    }

    public int getVersion() {
        return version;
    }

    public Date getNextResetAt() {
        return nextResetAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    void setOrder(LCLeaderboardOrder order) {
        if (null != order) {
            this.order = order;
        }
    }

    void setUpdateStrategy(LCLeaderboardUpdateStrategy updateStrategy) {
        if (null != updateStrategy) {
            this.updateStrategy = updateStrategy;
        }
    }

    void setVersionChangeInterval(LCLeaderboardVersionChangeInterval versionChangeInterval) {
        if (null != versionChangeInterval) {
            this.versionChangeInterval = versionChangeInterval;
        }
    }

    void setVersion(int version) {
        this.version = version;
    }

    void setNextResetAt(Date nextResetAt) {
        this.nextResetAt = nextResetAt;
    }

    void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    private LCLeaderboard(String name) {
        this.statisticName = name;
    }

    protected static <T extends Enum<T>> T lookup(Class<T> enumType, String name) {
        for (T item : enumType.getEnumConstants()) {
            if (item.name().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    protected LCLeaderboard(LCObject object) {
        if (null == object) {
            return;
        }
        this.memberType = object.getString(ATTR_MEMBER_TYPE);
        this.statisticName = object.getString(ATTR_STATISTIC_NAME);
        String order = object.getString(ATTR_ORDER);
        if (!StringUtil.isEmpty(order)) {
            setOrder(lookup(LCLeaderboardOrder.class, order));
        }
        String updateStrategy = object.getString(ATTR_UPDATE_STRATEGY);
        if (!StringUtil.isEmpty(updateStrategy)) {
            setUpdateStrategy(lookup(LCLeaderboardUpdateStrategy.class, updateStrategy));
        }
        String versionUpdateInterval = object.getString(ATTR_VERSION_CHANGE_INTERVAL);
        if (!StringUtil.isEmpty(versionUpdateInterval)) {
            setVersionChangeInterval(lookup(LCLeaderboardVersionChangeInterval.class, versionUpdateInterval));
        }
        int version = object.getInt(ATTR_VERSION);
        setVersion(version);
        setNextResetAt(object.getDate(ATTR_EXPIRED_AT));
        setCreatedAt(object.getCreatedAt());
    }

    public static LCLeaderboard createWithoutData(String name) {
        return new LCLeaderboard(name);
    }

    public static Observable<LCStatisticResult> updateStatistic(LCUser user, Map<String, Double> values) {
        return updateStatistic(user, values, false);
    }

    public static Observable<LCStatisticResult> updateStatistic(LCUser user, Map<String, Double> params, boolean overwrite) {
        if (null == user) {
            return Observable.error(new IllegalArgumentException("user is null"));
        }
        if (null == params || params.size() < 1) {
            return Observable.error(new IllegalArgumentException("params is empty"));
        }
        List<Map<String, Object>> statistics = new ArrayList<>(params.size());
        for (Map.Entry entry: params.entrySet()) {
            Map<String, Object> statistic = new HashMap<>();
            statistic.put("statisticName", entry.getKey());
            statistic.put("statisticValue", entry.getValue());
            statistics.add(statistic);
        }
        return PaasClient.getStorageClient().updateUserStatistics(user, statistics, overwrite);
    }

    public static Observable<LCStatisticResult> getUserStatistics(LCUser user) {
        return getUserStatistics(user, null);
    }
    public static Observable<LCStatisticResult> getUserStatistics(LCUser user, List<String> statisticNames) {
        if (null == user || StringUtil.isEmpty(user.getObjectId())) {
            return Observable.error(new IllegalArgumentException("user is invalid."));
        }
        return PaasClient.getStorageClient().getUserStatistics(user.getObjectId(), statisticNames);
    }

    static String convertLeaderboardType(String memberType) {
        String leaderboardType = null;
        if (MEMBER_TYPE_USER.equalsIgnoreCase(memberType)) {
            leaderboardType = "user";
        } else if (MEMBER_TYPE_ENTITY.equalsIgnoreCase(memberType)) {
            leaderboardType = "entity";
        } else {
            leaderboardType = "object";
        }
        return leaderboardType;
    }

    public Observable<LCLeaderboardResult> getResults(int skip, int limit, List<String> selectUserKeys,
                                                      List<String> includeStatistics) {
        return getResults(skip, limit, selectUserKeys, null, includeStatistics, false);
    }

    public Observable<LCLeaderboardResult> getResults(int skip, int limit, List<String> selectUserKeys,
                                                      List<String> includeUserKeys,
                                                      List<String> includeStatistics,
                                                      boolean withCount) {
        if (StringUtil.isEmpty(this.statisticName)) {
            return Observable.error(new IllegalArgumentException("name is empty"));
        }
        String leaderboardType = convertLeaderboardType(this.memberType);
        return PaasClient.getStorageClient().getLeaderboardResults(leaderboardType, this.statisticName,
                skip, limit, selectUserKeys, includeUserKeys, includeStatistics, this.version, withCount);
    }

    public Observable<LCLeaderboardResult> getAroundResults(String targetId, int skip, int limit, List<String> selectUserKeys,
                                                        List<String> includeStatistics) {
        return getAroundResults(targetId, skip, limit, selectUserKeys, null, includeStatistics);
    }

    public Observable<LCLeaderboardResult> getAroundResults(String targetId, int skip, int limit, List<String> selectUserKeys,
                                                  List<String> includeUserKeys, List<String> includeStatistics) {
        if (StringUtil.isEmpty(this.statisticName)) {
            return Observable.error(new IllegalArgumentException("name is empty"));
        }
        String leaderboardType = convertLeaderboardType(this.memberType);
        return PaasClient.getStorageClient().getLeaderboardAroundResults(leaderboardType, this.statisticName, targetId,
                skip, limit, selectUserKeys, includeUserKeys, includeStatistics, this.version);
    }

    /**
     * create leaderboard with default member type(User).
     * @param name name
     * @param order order
     * @param updateStrategy update strategy.
     * @param versionChangeInterval version change interval.
     * @return leaderboard observer.
     */
    public static Observable<LCLeaderboard> create(String name, LCLeaderboardOrder order,
                                                   LCLeaderboardUpdateStrategy updateStrategy,
                                                   LCLeaderboardVersionChangeInterval versionChangeInterval) {
        return createWithMemberType(MEMBER_TYPE_USER, name, order, updateStrategy, versionChangeInterval);
    }

    /**
     * create leaderboard with customized member type
     * @param memberType member type
     * @param name name
     * @param order order
     * @param updateStrategy update strategy.
     * @param versionChangeInterval version change interval.
     * @return leaderboard observer.
     */
    public static Observable<LCLeaderboard> createWithMemberType(String memberType, String name, LCLeaderboardOrder order,
                                                   LCLeaderboardUpdateStrategy updateStrategy,
                                                   LCLeaderboardVersionChangeInterval versionChangeInterval) {
        if (StringUtil.isEmpty(name)) {
            return Observable.error(new IllegalArgumentException("name is empty"));
        }
        Map<String, Object> params = new HashMap<>();
        params.put(ATTR_STATISTIC_NAME, name);
        params.put(ATTR_MEMBER_TYPE, memberType);
        if (null != order) {
            params.put(ATTR_ORDER, order.toString().toLowerCase(Locale.ROOT));
        } else {
            params.put(ATTR_ORDER, "descending");
        }
        if (null != updateStrategy) {
            params.put(ATTR_UPDATE_STRATEGY, updateStrategy.toString().toLowerCase(Locale.ROOT));
        } else {
            params.put(ATTR_UPDATE_STRATEGY, "better");
        }
        if (null != versionChangeInterval) {
            params.put(ATTR_VERSION_CHANGE_INTERVAL, versionChangeInterval.toString().toLowerCase(Locale.ROOT));
        } else {
            params.put(ATTR_VERSION_CHANGE_INTERVAL, "week");
        }
        return PaasClient.getStorageClient().createLeaderboard(params).map(new Function<LCObject, LCLeaderboard>() {
            @Override
            public LCLeaderboard apply(@NotNull LCObject object) throws Exception {
                return new LCLeaderboard(object);
            }
        });
    }

    /**
     * fetch leaderboard with name
     * @param name leaderboard name
     * @return leaderboard observer.
     */
    public static Observable<LCLeaderboard> fetchByName(String name) {
        if (StringUtil.isEmpty(name)) {
            return Observable.error(new IllegalArgumentException("name is empty"));
        }
        return PaasClient.getStorageClient().fetchLeaderboard(name).map(new Function<LCObject, LCLeaderboard>() {
            @Override
            public LCLeaderboard apply(@NotNull LCObject object) throws Exception {
                return new LCLeaderboard(object);
            }
        });
    }

    /**
     * reset current leaderboard.
     * @return true or false observer.
     */
    public Observable<Boolean> reset() {
        if (StringUtil.isEmpty(this.statisticName)) {
            return Observable.error(new IllegalStateException("statistic name is empty."));
        }
        return PaasClient.getStorageClient().resetLeaderboard(this.statisticName).map(new Function<LCObject, Boolean>() {
            @Override
            public Boolean apply(@NotNull LCObject object) throws Exception {
                LCLeaderboard.this.setVersion(object.getInt(ATTR_VERSION));
                Date nextReset = object.getDate(ATTR_EXPIRED_AT);
                if (null != nextReset) {
                    LCLeaderboard.this.setNextResetAt(nextReset);
                }
                return null != object;
            }
        });
    }

    /**
     * update current leaderboard's change interval
     * @param interval new change interval
     * @return true or false observer.
     */
    public Observable<Boolean> updateVersionChangeInterval(final LCLeaderboardVersionChangeInterval interval) {
        if (StringUtil.isEmpty(this.statisticName)) {
            return Observable.error(new IllegalStateException("statistic name is empty."));
        }
        if (null == interval) {
            return Observable.error(new IllegalArgumentException("interval is null."));
        }
        Map<String, Object> params = new HashMap<>();
        params.put(ATTR_VERSION_CHANGE_INTERVAL, interval.toString().toLowerCase(Locale.ROOT));
        return PaasClient.getStorageClient().updateLeaderboard(this.statisticName, params).map(new Function<LCObject, Boolean>() {
            @Override
            public Boolean apply(@NotNull LCObject object) throws Exception {
                LCLeaderboard.this.setVersionChangeInterval(interval);
                Date nextReset = object.getDate(ATTR_EXPIRED_AT);
                if (null != nextReset) {
                    LCLeaderboard.this.setNextResetAt(nextReset);
                }
                return null != object;
            }
        });
    }

    /**
     * update current leaderboard's update strategy.
     * @param strategy new update strategy.
     * @return true or false observer.
     */
    public Observable<Boolean> updateUpdateStrategy(final LCLeaderboardUpdateStrategy strategy) {
        if (StringUtil.isEmpty(this.statisticName)) {
            return Observable.error(new IllegalStateException("statistic name is empty."));
        }
        if (null == strategy) {
            return Observable.error(new IllegalArgumentException("strategy is null."));
        }
        Map<String, Object> params = new HashMap<>();
        params.put(ATTR_UPDATE_STRATEGY, strategy.toString().toLowerCase(Locale.ROOT));
        return PaasClient.getStorageClient().updateLeaderboard(this.statisticName, params).map(new Function<LCObject, Boolean>() {
            @Override
            public Boolean apply(@NotNull LCObject object) throws Exception {
                LCLeaderboard.this.setUpdateStrategy(strategy);
                return null != object;
            }
        });
    }

    /**
     * destroy current leaderboard.
     * @return true or false observer.
     */
    public Observable<Boolean> destroy() {
        if (StringUtil.isEmpty(this.statisticName)) {
            return Observable.error(new IllegalStateException("statistic name is empty."));
        }
        return PaasClient.getStorageClient().destroyLeaderboard(this.statisticName);
    }
}
