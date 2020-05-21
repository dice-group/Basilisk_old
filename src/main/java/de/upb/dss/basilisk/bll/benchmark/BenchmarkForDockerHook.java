package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;

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
    private static final String logPrefix = "DockerBenchmark";

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
     * @throws InterruptedException If Basilisk is interrupted.
     */
    public static int runBenchmarkForDockerHook(String argPort, String argTripleStoreName, String argTestDataSet,
                                                String argQueryFile, String argRepoName, String argTag) throws InterruptedException {

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

        if (exitCode == 0) {
            try {
                IguanaUtils.runIguana(repoName, tag, port, argQueryFile);
                ResultStoringFusekiUtils.processResultFIle(tripleStoreName, repoName, tag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LoggerUtils.logForBasilisk(logPrefix, repoName + ":"+ tag +
                    "Could not run docker. Exit code of docker = " + exitCode, 4);
        }
        return exitCode;
    }

    /**
     * This method runs the triple store in the docker and continues the benchmarking process.
     *
     * @return Exit code.
     * @throws InterruptedException If Basilisk is interrupted.
     */
    public static int runTripleStores() throws InterruptedException {
        int exitCode = 0;

        LoggerUtils.logForBasilisk(logPrefix, repoName + ":" + tag + ": Running the docker ", 1);
        if (tripleStoreName.toLowerCase().equals("tentris")) {
            return DockerUtils.runTentrisDocker(repoName, tag, port, testDataset);
        } else if (tripleStoreName.toLowerCase().equals("virtuoso")) {
            return DockerUtils.runVirtuosoDocker(repoName, tag, port, testDataset);
        }


        return exitCode;
    }
}
