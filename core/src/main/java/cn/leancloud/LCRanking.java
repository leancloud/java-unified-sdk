package cn.leancloud;

import java.util.List;

public class LCRanking {
    private int rank;
    private LCUser user;
    private double value;
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

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public List<LCStatistic> getIncludedStatistics() {
        return includedStatistics;
    }

    public void setIncludedStatistics(List<LCStatistic> includedStatistics) {
        this.includedStatistics = includedStatistics;
    }
}
