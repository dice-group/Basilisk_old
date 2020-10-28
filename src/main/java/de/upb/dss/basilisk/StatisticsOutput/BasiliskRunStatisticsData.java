package de.upb.dss.basilisk.StatisticsOutput;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents the Basilisk run statistics data.
 *
 * @author Ranjith Krishnamurthy
 */
public class BasiliskRunStatisticsData {
    private List<HashMap<String, Object>> basiliskRunStats = new ArrayList<>();
    private String startDate;
    private String endDate;

    /**
     * Constructs the Basilisk run statistics data with start time
     */
    public BasiliskRunStatisticsData() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Date date = new Date();

        startDate = dateFormat.format(date);
        endDate = "";
    }

    /**
     * This method adds the CBP process results of a single triple store into BasiliskRunStatisticsData
     *
     * @param repoName Triple store's repository name
     * @param version  Triple store's version
     * @param hash     Triple store's hash
     * @param hook     Hook (Docker or Git)
     * @param status   CBP status
     */
    public void addTripleStore(String repoName, String version, String hash, String hook, BasiliskRunStatus status) {
        HashMap<String, Object> tripleStoreStat = new HashMap<>();

        tripleStoreStat.put("repoName", repoName);
        tripleStoreStat.put("version", version);
        tripleStoreStat.put("hash", hash);
        tripleStoreStat.put("hook", hook);
        tripleStoreStat.put("status", status);
        basiliskRunStats.add(tripleStoreStat);
    }

    /**
     * This returns the Basilisk run statistics data
     *
     * @return Basilisk run statistics data
     */
    public List<HashMap<String, Object>> getBasiliskRunStatData() {
        return basiliskRunStats;
    }

    /**
     * This returns the start date and time of the CBP run.
     *
     * @return Start date and time of the CBP run.
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * This returns the end date and time of the CBP run.
     *
     * @return End date and time of the CBP run.
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * This sets the end date and time of the CBP run.
     */
    public void setEndTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Date date = new Date();

        endDate = dateFormat.format(date);
    }
}
