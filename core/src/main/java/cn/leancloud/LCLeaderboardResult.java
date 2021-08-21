package cn.leancloud;

import cn.leancloud.json.JSON;

import java.util.List;

public class LCLeaderboardResult {
    private List<LCRanking> results;
    private int count;

    /**
     * get result count
     * @return result count
     */
    public int getCount() {
        return count;
    }

    /**
     * setter
     * @param count result count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * result list getter
     * @return result list
     */
    public List<LCRanking> getResults() {
        return results;
    }

    /**
     * result list setter
     * @param results result list.
     */
    public void setResults(List<LCRanking> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
