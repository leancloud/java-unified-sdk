package cn.leancloud;

import java.util.List;

public class LCStatisticResult {
    private List<LCStatistic> results;

    /**
     * getter
     * @return statistic list.
     */
    public List<LCStatistic> getResults() {
        return results;
    }

    /**
     * setter
     * @param results statistic list.
     */
    public void setResults(List<LCStatistic> results) {
        this.results = results;
    }
}
