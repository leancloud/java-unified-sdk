package cn.leancloud;

public class LCStatistic {
    private String statisticName;
    private double statisticValue;
    private int version;
    private LCUser user;
    private LCObject object;
    private String entity;

    /**
     * get name
     * @return name
     */
    public String getStatisticName() {
        return statisticName;
    }

    /**
     * get name
     * @return name
     */
    public String getName() {
        return getStatisticName();
    }

    /**
     * set name
     * @param statisticName name
     */
    public void setStatisticName(String statisticName) {
        this.statisticName = statisticName;
    }

    /**
     * get value
     * @return statistic value.
     */
    public double getStatisticValue() {
        return statisticValue;
    }
    /**
     * get value
     * @return statistic value.
     */
    public double getValue() {
        return getStatisticValue();
    }

    /**
     * set value
     * @param statisticValue value
     */
    public void setStatisticValue(double statisticValue) {
        this.statisticValue = statisticValue;
    }

    /**
     * get version
     * @return version number
     */
    public int getVersion() {
        return version;
    }

    /**
     * set version
     * @param version version number
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * get target user(only valid for leaderboard which member type is _User)
     * @return LCUser instance, for object/entity leaderboard, the result is null.
     */
    public LCUser getUser() {
        return user;
    }

    public void setUser(LCUser user) {
        this.user = user;
    }

    /**
     * get target object(only valid for leaderboard which member type is LCObjct)
     * @return LCObject instance, for user/entity leaderboard, the result is null.
     */
    public LCObject getObject() {
        return object;
    }

    public void setObject(LCObject object) {
        this.object = object;
    }

    /**
     * get target entity id(only valid for leaderboard which member type is _Entity)
     * @return entity objectId, for user/object leaderboard, the result is null.
     */
    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
}
