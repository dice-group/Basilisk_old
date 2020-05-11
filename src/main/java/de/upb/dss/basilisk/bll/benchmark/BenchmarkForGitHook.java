package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This class runs the benchmarking process for the git hub hook.
 */
public class BenchmarkForGitHook {
    private static Logger logger;
    private static File dockerFile;
    private static File bmWorkSpace;
    private static File iguanaPath;
    private static String configPath;

    private static String repoName, port, testDataset, queryFile, tag, testDatasetPath, iguanaIdPath;

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
        LoggerUtils myLoggerUtils = new LoggerUtils();

        ApplicationPropertiesUtils myAppUtils = new ApplicationPropertiesUtils();

        dockerFile = new File(myAppUtils.getDockerFile());
        bmWorkSpace = new File(myAppUtils.getBmWorkSpace());
        iguanaPath = new File(myAppUtils.getIguanaPath());
        String logFilePath = myAppUtils.getLogFilePath();
        configPath = myAppUtils.getConfigPath();
        testDatasetPath = myAppUtils.getTestDatasetPath();
        iguanaIdPath = myAppUtils.getIguanaIdPath();

        //Set all the required info for running the benchmark.
        repoName = argRepoName;
        port = argPort;
        testDataset = argTestDataSet;
        queryFile = argQueryFile;
        tag = argVersionNumber;

        logger = myLoggerUtils.getLogger(logFilePath, "GitBenchmark");

        //Clear the docker, so that next benchmark can be run.
        clearDocker();

        //Run the triple stores
        int exitCode = runTripleStores();

        //Move the results to results folder and rename it.
        ResultFileProcessing.renameResults(repoName, tag);

        //Clear the docker, so that next benchmark can be run.
        clearDocker();
        return exitCode;
    }

    /**
     * This method builds the docker image and runs the respective docker image, then it called the Iguana to run the
     * benchmarking process.
     *
     * @return Status code.
     */
    protected static int runTripleStores() {
        UnixUtils myUnixUtils = new UnixUtils();

        String cmd = "";
        String err = "";

        try {
            logger.info("Trying to build the docker image.\n");
            System.out.println(dockerFile.getAbsoluteFile());
            if (dockerFile.exists()) {
                //Command to build the docker
                //docker build --tag cbm:${serverName} .
                cmd = "docker build --tag "
                        + repoName + ":" + tag
                        + " .";

                //Run the command through Process.
                int exitCode = myUnixUtils.runUnixCommand(cmd, bmWorkSpace, true);

                if (exitCode != 0) {
                    System.out.println("Something went wrong while building the docker");
                    System.out.println("Exit code = " + exitCode);
                    System.out.println("Error message = \n" + err);
                    return exitCode;
                }

                logger.info("Successfully built docker image\n");
                logger.info("Running the " + repoName + " server\n");


                /*
                 * Command to run the docker image.
                 * nohup docker run -p ${port}:${port} -v ../../continuousBM/testDataSet:/datasets --name ${serverName}_server cbm:{serverName} \
                 * -f /dataset/ ${testDataset} -p ${port} &
                 *
                 * Example for tentris.
                 * ${port} = 9080, ${serverName} = tentris, ${testDataset} = sp2b.nt
                 * nohup docker run -p 9080:9080 -v home/dss/continuousBM/testDataSet:/datasets --name tentris_server cbm:tentris \
                 * -f /datasets/sp2b.nt -p 9080 &
                 */

                logger.info("Running the docker: ");

                if (repoName.toLowerCase().equals("tentris")) {
                    testDatasetPath = Paths.get(".").toAbsolutePath().normalize().toString() + testDatasetPath;
                    DockerUtils.runTentrisDockerImage(
                            port,
                            testDatasetPath,
                            testDataset,
                            repoName,
                            tag,
                            bmWorkSpace
                    );
                } else if (repoName.toLowerCase().equals("fuseki")) {
                    DockerUtils.runFuesikiDockerImage(
                            port,
                            testDatasetPath,
                            testDataset,
                            repoName,
                            bmWorkSpace
                    );
                }

                //Wait for 10 seconds to docker image to setup and keep running.
                TimeUnit.SECONDS.sleep(10);

                //If the process is alive run Iguana benchmarl otherwise could not run the docker image.
                //Command to check whether the respective docker container is running or not, to avoid the infinite loop.
                //docker inspect -f '{{.State.Status}}' ${serverName}_server
                cmd = "docker inspect -f '{{.State.Status}}' "
                        + repoName
                        + "_server";

                myUnixUtils.runUnixCommand(cmd, bmWorkSpace, true);

                String dockerId = myUnixUtils.getOutput();

                if (dockerId.contains("running")) {
                    int iguanaExitCode = IguanaUtils.runIguana(repoName,tag,port,queryFile);

                    if (iguanaExitCode != 0)
                        return iguanaExitCode;
                } else {
                    System.out.println("Empty!! not existed docker container");
                    logger.info("Empty!! not existed docker container\n");
                    return -1;
                }
            } else {
                logger.info("Dockerfile does not exist\n");
                System.out.println("Dockerfile does not exist");
                return -151;
            }
        } catch (Exception e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
        return 0;
    }

    /**
     * This method stops all the docker container and removes all the docker image.
     */
    protected static void clearDocker() {
        try {
            //Clear the complete docker, so that next benchamrk can be done.
            //First kill the docker container we ran.
            String cmd = "docker kill "
                    + repoName + "_server";
            Process p = Runtime.getRuntime().exec(cmd, null, iguanaPath);
            p.waitFor();

            //Second prune the docker system, so that all the stoped container will be removed.
            cmd = "docker system prune";
            p = Runtime.getRuntime().exec(cmd, null, iguanaPath);

            //For the above command we will have to give a confirmation "y".
            OutputStream out = p.getOutputStream();
            out.write("y".getBytes());
            out.close();
            p.waitFor();

            cmd = "docker image rm cbm:"
                    + repoName;
            p = Runtime.getRuntime().exec(cmd, null, iguanaPath);
            p.waitFor();
        } catch (Exception e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
