package de.upb.dss.basilisk.bll.benchmark;

/**
 * This class runs the current triple store from Docker hook and then runs the Iguana for benchmarking the triple store.
 *
 * @author Ranjith Krishnamurthy
 * @author Rahul Sethi
 */
public class BenchmarkForDockerHook {
    private static String tripleStoreName;
    private static String port;
    private static String testDataset;
    private static String repoName;
    private static String tag;
    private static final String logPrefix = "DockerBenchmark";

    /**
     * This method runs the triple store in docker and then runs the Iguana for benchmarking the triple store.
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
            LoggerUtils.logForBasilisk(logPrefix, repoName + ":" + tag +
                    "Could not run docker. Exit code of docker = " + exitCode, 4);
        }

        return exitCode;
    }

    /**
     * This method runs the triple store in the docker.
     *
     * @return Exit code of Docker run command from Docker API.
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
