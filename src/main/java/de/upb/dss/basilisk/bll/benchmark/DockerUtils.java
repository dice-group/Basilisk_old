package de.upb.dss.basilisk.bll.benchmark;

import java.io.File;
import java.io.IOException;

/**
 * This is the utility class for supporting the docker process.
 */
public class DockerUtils {
    /**
     * This method runs the Tentris docker image.
     *
     * @param port            Port number to which the tentris should run.
     * @param testDatasetPath Path of the test dataset to mount into docker container.
     * @param testDataset     Test dataset file name to load into the tentris.
     * @param repoName        Name of the repository in the docker image.
     * @param tag             Name of the tag in the docker image.
     * @param path            Path where the command should be run.
     * @return returns the status code.
     * @throws IOException          If I/O error occurs while running the command.
     * @throws InterruptedException
     */
    public static int runTentrisDockerImage(String port, String testDatasetPath, String testDataset, String repoName,
                                            String tag, File path) throws IOException, InterruptedException {
        String command = "docker run -p "
                + port + ":" + port
                + " -v "
                + testDatasetPath
                + ":/datasets --name "
                + "Tentris_server_"
                + tag + " "
                + repoName + ":" + tag
                + " -f /datasets/"
                + testDataset + " -p "
                + port;

        return new UnixUtils().runUnixCommand(command, path, false);
    }

    public static int runVirtuosoDockerImage(String port, String testDatasetPath, String testDataset,
                                             String serverName, File path) throws IOException, InterruptedException {
        String command = "docker run -p "
                + port + ":" + port
                + " --name "
                + serverName + "_server cbm:" + serverName;

        return new UnixUtils().runUnixCommand(command, path, false);
    }

    public static int runFuesikiDockerImage(String port, String testDatasetPath, String testDataset,
                                            String serverName, File path) throws IOException, InterruptedException {
        String command = "docker run -p "
                + port + ":3030"
                + " -v "
                + testDatasetPath
                + ":/staging --name "
                + serverName + "_server cbm:" + serverName
                + " /jena-fuseki/fuseki-server --file /staging/"
                + testDataset + " /sparql";

        return new UnixUtils().runUnixCommand(command, path, false);
    }
}
