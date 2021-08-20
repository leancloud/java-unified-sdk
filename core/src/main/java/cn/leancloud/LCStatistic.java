package cn.leancloud;

public class LCStatistic {
    private String statisticName;
    private double statisticValue;
    private int version;
    private LCUser user;
    private LCObject object;
    private String entity;

    public String getStatisticName() {
        return statisticName;
    }

    public void setStatisticName(String statisticName) {
        this.statisticName = statisticName;
    }

    public double getStatisticValue() {
        return statisticValue;
    }

    public void setStatisticValue(double statisticValue) {
        this.statisticValue = statisticValue;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public LCUser getUser() {
        return user;
    }

    public void setUser(LCUser user) {
        this.user = user;
    }

    public LCObject getObject() {
        return object;
    }

    public void setObject(LCObject object) {
        this.object = object;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
}
