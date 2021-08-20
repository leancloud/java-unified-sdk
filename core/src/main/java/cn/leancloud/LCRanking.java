package cn.leancloud;

import java.util.List;

public class LCRanking {
    private int rank;
    private LCUser user;
    private LCObject object;
    private String entityId;
    private double statisticValue;
    private List<LCStatistic> includedStatistics;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public LCUser getUser() {
        return user;
    }

    public void setUser(LCUser user) {
        this.user = user;
    }

    public double getStatisticValue() {
        return statisticValue;
    }

    public void setStatisticValue(double statisticValue) {
        this.statisticValue = statisticValue;
    }

    public LCObject getObject() {
        return object;
    }

    public void setObject(LCObject object) {
        this.object = object;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public List<LCStatistic> getIncludedStatistics() {
        return includedStatistics;
    }

    public void setIncludedStatistics(List<LCStatistic> includedStatistics) {
        this.includedStatistics = includedStatistics;
    }
}
