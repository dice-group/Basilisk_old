package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * This class runs the benchmarking process for the git hub hook.
 */
public class BenchmarkForGitHook {
    private static File dockerFile;
    private static File bmWorkSpace;

    private static String repoName;
    private static String port;
    private static String testDataset;
    private static String queryFile;
    private static String tag;
    private static String testDatasetPath;
    private static final String logPrefix = "GitBenchmark";

    /**
     * This method builds the docker image, runs the container and then runs the Iguana
     * for benchmarking the triple store.
     *
     * @param argPort          Port number on which the triple store should run.
     * @param argRepoName      Repository name.
     * @param argTestDataSet   Test dataset file name.
     * @param argQueryFile     Query file name.
     * @param argVersionNumber Git repository version.
     * @return Status of the benchmarking process.
     * @throws IOException If fails to rename the results file.
     */
    public static int runBenchmark(String argPort, String argRepoName, String argTestDataSet, String argQueryFile, String argVersionNumber) throws IOException {
        ApplicationPropertiesUtils myAppUtils = new ApplicationPropertiesUtils();

        dockerFile = new File(myAppUtils.getDockerFile());
        bmWorkSpace = new File(myAppUtils.getBmWorkSpace());
        String logFilePath = myAppUtils.getLogFilePath();
        testDatasetPath = myAppUtils.getTestDatasetPath();

        //Set all the required info for running the benchmark.
        repoName = argRepoName;
        port = argPort;
        testDataset = argTestDataSet;
        queryFile = argQueryFile;
        tag = argVersionNumber;

        //Clear the docker, so that next benchmark can be run.
        DockerUtils.clearDocker();

        //Run the triple stores
        int exitCode = runTripleStores();

        //Store the results into Fuseki server and Move the results to results folder and rename it.
        ResultStoringFusekiUtils.processResultFIle(repoName, repoName, tag);

        //Clear the docker, so that next benchmark can be run.
        DockerUtils.clearDocker();
        return exitCode;
    }

    /**
     * This method builds the docker image and runs the respective docker image, then it called the Iguana to run the
     * benchmarking process.
     *
     * @return Status code.
     */
    protected static int runTripleStores() {

        try {
            LoggerUtils.logForBasilisk(logPrefix, "Trying to build the docker image.", 1);

            if (dockerFile.exists()) {
                int exitCode = DockerUtils.buildImage(repoName, tag);

                if (exitCode != 0) {
                    LoggerUtils.logForBasilisk(logPrefix,
                            "Something went wrong while building docker image. Exit code = " + exitCode,
                            4);
                    return exitCode;
                }

                LoggerUtils.logForBasilisk(logPrefix, "Successfully built docker image", 1);
                LoggerUtils.logForBasilisk(logPrefix,
                        "Running the docker " + repoName + ":" + tag,
                        1);

                int dockerStatusCode = 0;

                if (repoName.toLowerCase().equals("tentris")) {
                    testDatasetPath = Paths.get(".").toAbsolutePath().normalize().toString() + testDatasetPath;
                    dockerStatusCode = DockerUtils.runTentrisDocker(repoName, tag, port, testDataset);
                } else if (repoName.toLowerCase().equals("fuseki")) {
                    //Todo:This outdated. Update the code for fuseki triple store.
                    DockerUtils.runFuesikiDocker(
                            port,
                            testDatasetPath,
                            testDataset,
                            repoName,
                            bmWorkSpace
                    );
                }

                if (dockerStatusCode == 0) {
                    int iguanaExitCode = IguanaUtils
                            .runIguana(repoName, tag, port, queryFile);

                    if (iguanaExitCode != 0)
                        return iguanaExitCode;
                } else {
                    LoggerUtils.logForBasilisk(logPrefix, repoName + ":"+ tag +
                            "Could not run docker. Exit code of docker = " + exitCode, 4);
                    return -100;
                }
            } else {
                LoggerUtils.logForBasilisk(logPrefix, "Dockerfile does not exist\n", 4);
                return -151;
            }
        } catch (Exception e) {
            LoggerUtils.logForBasilisk(logPrefix, "Something went wrong", 4);
            e.printStackTrace();
            return -180;
        }
        return 0;
    }
}
