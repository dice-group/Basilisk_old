package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class runs the benchmark for the docker hub hook.
 */
public class BenchmarkForDockerHook {
    private static Logger logger;
    private static File bmWorkSpace;
    private static File iguanaPath;
    private static String logFilePath;
    private static String configPath;

    private static String tripleStoreName, port, testDataset, queryFile,
            testDatasetPath, iguanaIdPath, repoName, tag;

    /**
     * This method runs the docker for the triple store and runs the benchmarking process.
     *
     * @param argPort            Port number on which the triplpe store should run.
     * @param argTripleStoreName Triple store name.
     * @param argTestDataSet     Test dataset file name.
     * @param argQueryFile       Query file name.
     * @param argRepoName        Repository name.
     * @param argTag             Docker hub repository tag.
     * @return Status code.
     * @throws IOException          If fails to read the output of the commands.
     * @throws InterruptedException If the process is interrupted.
     */
    public static int runBenchmarkForDockerHook(String argPort, String argTripleStoreName, String argTestDataSet,
                                                String argQueryFile, String argRepoName, String argTag) throws InterruptedException, IOException {

        ApplicationPropertiesUtils myAppUtils = new ApplicationPropertiesUtils();
        bmWorkSpace = new File(myAppUtils.getBmWorkSpace());
        iguanaPath = new File(myAppUtils.getIguanaPath());
        logFilePath = myAppUtils.getLogFilePath();
        configPath = myAppUtils.getConfigPath();
        testDatasetPath = myAppUtils.getTestDatasetPath();
        iguanaIdPath = myAppUtils.getIguanaIdPath();

        //Set all the required info for running the benchmark.
        tripleStoreName = argTripleStoreName;
        port = argPort;
        testDataset = argTestDataSet;
        queryFile = argQueryFile;
        repoName = argRepoName;
        tag = argTag;

        //Run the triple stores
        int exitCode = runTripleStores();

        if(exitCode == 0) {
            try {
                IguanaUtils.runIguana(repoName,tag,port,queryFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return exitCode;
    }

    /**
     * This method runs the triple store in the docker and continues the benchmarking process.
     *
     * @return Status code.
     * @throws IOException          If fails to read the output of the command.
     * @throws InterruptedException If the process is interrupted.
     */
    public static int runTripleStores() throws IOException, InterruptedException {
        int exitCode = 0;

        //Initialize the logger to log
        logger = new LoggerUtils().getLogger(logFilePath, "DockerBenchmark");

        logger.info("Running the docker container");
        if (tripleStoreName.toLowerCase().equals("tentris"))
            exitCode = DockerUtils.runTentrisDockerImage(port, testDatasetPath, testDataset, repoName,
                    tag, bmWorkSpace);

        return exitCode;
    }
}
