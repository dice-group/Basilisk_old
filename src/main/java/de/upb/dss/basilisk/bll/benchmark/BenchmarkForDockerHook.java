package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class runs the benchmark for the docker hub hook.
 */
public class BenchmarkForDockerHook {
    private static String logFilePath;

    private static String tripleStoreName;
    private static String port;
    private static String testDataset;
    private static String repoName;
    private static String tag;

    /**
     * This method runs the docker for the triple store and runs the benchmarking process.
     *
     * @param argPort            Port number on which the triple store should run.
     * @param argTripleStoreName Triple store name.
     * @param argTestDataSet     Test dataset file name.
     * @param argQueryFile       Query file name.
     * @param argRepoName        Repository name.
     * @param argTag             Docker hub repository tag.
     * @return Exit code.
     */
    public static int runBenchmarkForDockerHook(String argPort, String argTripleStoreName, String argTestDataSet,
                                                String argQueryFile, String argRepoName, String argTag) {

        ApplicationPropertiesUtils myAppUtils = new ApplicationPropertiesUtils();
        logFilePath = myAppUtils.getLogFilePath();

        //Set all the required info for running the benchmark.
        tripleStoreName = argTripleStoreName;
        port = argPort;
        testDataset = argTestDataSet;
        repoName = argRepoName;
        tag = argTag;

        //Run the triple stores
        int exitCode = runTripleStores();

        if(exitCode == 0) {
            try {
                IguanaUtils.runIguana(repoName,tag,port, argQueryFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return exitCode;
    }

    /**
     * This method runs the triple store in the docker and continues the benchmarking process.
     *
     * @return Exit code.
     */
    public static int runTripleStores() {
        int exitCode = 0;

        //Initialize the logger to log
        Logger logger = new LoggerUtils().getLogger(logFilePath, "DockerBenchmark");

        logger.info("Running the docker container");
        if (tripleStoreName.toLowerCase().equals("tentris")) {
            DockerUtils.runTentrisDocker(repoName,tag,port,testDataset);
        }
        else if(tripleStoreName.toLowerCase().equals("virtuoso")) {
            //Todo: Implement Running Virtuoso docker.
        }


        return exitCode;
    }
}
