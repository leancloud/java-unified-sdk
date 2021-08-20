package cn.leancloud;

import cn.leancloud.json.JSON;

import java.util.List;

public class LCLeaderboardResult {
    private List<LCRanking> results;
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<LCRanking> getResults() {
        return results;
    }

    public void setResults(List<LCRanking> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
