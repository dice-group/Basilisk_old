package de.upb.dss.basilisk.StatisticsOutput;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This creates the Basilisk run statistics result file.
 *
 * @author Ranjith Krishnamurthy
 */
public class PrintBasiliskRunStatData {
    private static BasiliskRunStatisticsData basiliskRunStatisticsData;
    private static StringBuilder success = new StringBuilder("\n*******************************************\n" +
            "*               Run Success               *\n" +
            "*******************************************\n");

    private static StringBuilder fail = new StringBuilder("\n*******************************************\n" +
            "*                Run Fail                 *\n" +
            "*******************************************\n");

    private static StringBuilder alreadyRan = new StringBuilder("\n*******************************************\n" +
            "*               Already Ran               *\n" +
            "*******************************************\n");
    private static boolean isSuccessPresent = false;
    private static boolean isFailPresent = false;
    private static boolean isAlreadyRanPresent = false;
    private static String statFileData = "";
    private static int successCount = 0;
    private static int failCount = 0;
    private static int alreadyRanCount = 0;

    /**
     * This prints the stats in the stat file
     *
     * @param basiliskRunStatsData Basilisk run statistics data
     */
    public static void printRunStat(BasiliskRunStatisticsData basiliskRunStatsData) {
        basiliskRunStatisticsData = basiliskRunStatsData;

        processStatData();
        printInitialInfo();
        printStatData();
    }

    /**
     * This process the Basilisk run statistics data
     */
    private static void processStatData() {
        List<HashMap<String, Object>> basiliskRunStats = basiliskRunStatisticsData.getBasiliskRunStatData();

        for (HashMap<String, Object> singleTiplrInfo : basiliskRunStats) {
            String repoName = (String) singleTiplrInfo.get("repoName");
            String version = (String) singleTiplrInfo.get("version");
            String hash = (String) singleTiplrInfo.get("hash");
            String hook = (String) singleTiplrInfo.get("hook");
            BasiliskRunStatus status = (BasiliskRunStatus) singleTiplrInfo.get("status");

            if (status == BasiliskRunStatus.RUN_SUCCESS) {
                success.append("\nRepository name\t\t:  ").append(repoName);
                success.append("\nVersion\t\t\t:  ").append(version);
                success.append("\nHash\t\t\t:  ").append(hash);
                success.append("\nHook\t\t\t:  ").append(hook);
                success.append("\n");
                isSuccessPresent = true;
                successCount++;

            } else if (status == BasiliskRunStatus.RUN_FAIL) {
                fail.append("\nRepository name\t\t:  ").append(repoName);
                fail.append("\nVersion\t\t\t:  ").append(version);
                fail.append("\nHash\t\t\t:  ").append(hash);
                fail.append("\nHook\t\t\t:  ").append(hook);
                fail.append("\n");
                isFailPresent = true;
                failCount++;

            } else {
                alreadyRan.append("\nRepository name\t\t:  ").append(repoName);
                alreadyRan.append("\nVersion\t\t\t:  ").append(version);
                alreadyRan.append("\nHash\t\t\t:  ").append(hash);
                alreadyRan.append("\nHook\t\t\t:  ").append(hook);
                alreadyRan.append("\n");
                isAlreadyRanPresent = true;
                alreadyRanCount++;

            }
        }
    }

    /**
     * This writes the Initial information to stat file
     */
    private static void printInitialInfo() {
        statFileData = "\nBasilisk " +
                new ApplicationPropertiesUtils().getBasiliskVersion() + " CBP run statistics\n\n";

        statFileData += "Start Time: " + basiliskRunStatisticsData.getStartDate();

        statFileData += "\nEnd Time: " + basiliskRunStatisticsData.getEndDate();

        statFileData += "\n\nCount: ";
        statFileData += "\n\nSuccess: " + successCount;
        statFileData += "\n\nFail: " + failCount;
        statFileData += "\n\nAlready Ran: " + alreadyRanCount;
        int total = (alreadyRanCount + failCount + successCount);
        statFileData += "\n\nTotal: " + total;
    }

    /**
     * This writes the Basilisk run stat data into stat file
     */
    private static void printStatData() {

        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");

        Date date = new Date();

        String dateString = dateFormat.format(date);

        File statFile = new File("runStat/BasiliskRunStat" + dateString);

        if (isSuccessPresent) {
            statFileData += "\n\n";
            statFileData += success.toString();
        }

        if (isFailPresent) {
            statFileData += "\n\n";
            statFileData += fail.toString();
        }

        if (isAlreadyRanPresent) {
            statFileData += "\n\n";
            statFileData += alreadyRan.toString();
        }

        try {
            FileWriter fileWriter = new FileWriter(statFile);

            fileWriter.write(statFileData);
            fileWriter.close();

        } catch (IOException e) {
            LoggerUtils.logForBasilisk("BasiliskStat Generator",
                    "Something went wrong while generating the Basilisk CBP process",
                    4);

            e.printStackTrace();
        }
    }
}
