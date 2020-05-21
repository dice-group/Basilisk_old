package de.upb.dss.basilisk.bll.benchmark;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is the utility class for supporting the docker process.
 */
public class DockerUtils {
    private static DefaultDockerClientConfig.Builder config = null;
    private static DockerClient dockerClient = null;
    private static final String logPrefix = "Docker Utils";

    /**
     * This method sets up the docker java api by creating all the necessary object for docker java api.
     */
    private static void setUpDockerApi() {
        if (config == null) {
            config = DefaultDockerClientConfig.createDefaultConfigBuilder();
        }

        if (dockerClient == null) {
            dockerClient = DockerClientBuilder
                    .getInstance(config)
                    .build();
        }
    }

    /**
     * This method builds the Docker image.
     *
     * @param repoName Repository name
     * @param tag      Tag
     * @return Returns the Status code.
     */
    public static int buildImage(String repoName, String tag) {
        setUpDockerApi();

        LoggerUtils.logForBasilisk(logPrefix, "Building the docker image for the Dockerfile in bmWorkSpace directory.", 1);
        String dockerFile = new ApplicationPropertiesUtils().getDockerFile();

        try {
            String imageId = dockerClient.buildImageCmd()
                    .withDockerfile(new File(dockerFile))
                    .withNoCache(true)
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();

            dockerClient.tagImageCmd(imageId, repoName, tag).exec();
        } catch (Exception ex) {
            System.out.println("Check this : ");
            ex.printStackTrace();
            return 100;
        }
        return 0;
    }

    /**
     * This method pulls the docker image from the docker hub.
     *
     * @param repoName Image's repository name.
     * @param tag      Image's tag
     * @return Boolean to indicate whether pull command is success or not.
     * @throws InterruptedException If Basilisk is interrupted.
     */
    public static boolean pullImage(String repoName, String tag) throws InterruptedException {
        setUpDockerApi();

        LoggerUtils.logForBasilisk(logPrefix, repoName + ":" + tag + ": Pulling the docker image ", 1);
        boolean flag = false;

        flag = dockerClient.pullImageCmd(repoName)
                .withTag(tag)
                .exec(new PullImageResultCallback())
                .awaitCompletion(30, TimeUnit.SECONDS);

        return flag;
    }

    /**
     * This method removes all the container and images.
     */
    public static void clearDocker() {
        setUpDockerApi();

        LoggerUtils.logForBasilisk(logPrefix, "Killing all the docker containers.", 1);
        //Delete all the containers
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> containerList = listContainersCmd.exec();

        Iterator<Container> containerItr = containerList.iterator();

        while (containerItr.hasNext()) {
            Container container = containerItr.next();
            dockerClient.killContainerCmd(container.getId()).exec();
            dockerClient.removeContainerCmd(container.getId()).exec();
        }

        LoggerUtils.logForBasilisk(logPrefix, "Removing all the docker containers in exited state.", 1);
        //Remove all the containers which are in exited state.
        listContainersCmd = dockerClient.listContainersCmd().withStatusFilter("exited");
        containerList = listContainersCmd.exec();

        containerItr = containerList.iterator();

        while (containerItr.hasNext()) {
            Container c = containerItr.next();
            dockerClient.removeContainerCmd(c.getId()).exec();
        }

        LoggerUtils.logForBasilisk(logPrefix, "Removing all the docker images.", 1);
        ListImagesCmd listImageCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImageCmd.exec();

        for (Image c : imageList) {
            try {
                dockerClient.removeImageCmd(c.getId()).withNoPrune(false).withForce(true).exec();
            } catch (Exception ex) {
                LoggerUtils.logForBasilisk(logPrefix, "Something went wrong", 4);
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method runs the Tentris docker for the given tag.
     *
     * @param repoName    Tentris's repository name
     * @param tag         Tentris's tag
     * @param port        Port number on which the Tentris should run.
     * @param dataSetName Dataset name to load into the Tentris triple store.
     * @throws InterruptedException If Basilisk is interrupted.
     */
    public static int runTentrisDocker(String repoName, String tag, String port, String dataSetName) throws InterruptedException {
        setUpDockerApi();
        String testDataSetPath = new ApplicationPropertiesUtils().getTestDatasetPath();

        ExposedPort tcpPort = ExposedPort.tcp(Integer.parseInt(port));

        Ports portBindings = new Ports();
        portBindings.bind(tcpPort, Ports.Binding.bindPort(Integer.parseInt(port)));

        Bind b = new Bind(new File(testDataSetPath).getAbsolutePath(), new Volume("/datasets"), AccessMode.rw);
        CreateContainerResponse container
                = dockerClient.createContainerCmd(repoName + ":" + tag)
                .withCmd("-f", "/datasets/" + dataSetName, "-p", port)
                .withName("tentris_server")
                .withHostName("tentris_server")
                .withPortBindings(portBindings)
                .withExposedPorts(new ExposedPort(Integer.parseInt(port)))
                .withBinds(b).exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        TimeUnit.SECONDS.sleep(15);

        InspectContainerResponse i = dockerClient.inspectContainerCmd(container.getId()).exec();

        return i.getState().getExitCode();
    }

    /**
     * This method runs the Virtuoso triple store (Docker).
     *
     * @param repoName    Repository name of the docker image.
     * @param tag         Tag of the docker image.
     * @param port        Port number
     * @param dataSetName Testdataset name to be loaded into the virtuoso.
     * @return Exit code.
     */
    public static int runVirtuosoDocker(String repoName, String tag, String port, String dataSetName) throws InterruptedException {
        setUpDockerApi();
        String testDataSetPath = new ApplicationPropertiesUtils().getTestDatasetPath();

        ExposedPort tcpPort = ExposedPort.tcp(Integer.parseInt(port));

        Ports portBindings = new Ports();
        portBindings.bind(tcpPort, Ports.Binding.bindPort(Integer.parseInt(port)));

        Bind b = new Bind(new File(testDataSetPath).getAbsolutePath(), new Volume("/usr/share/proj"), AccessMode.rw);
        CreateContainerResponse container
                = dockerClient.createContainerCmd(repoName + ":" + tag)
                .withName("my_virtdb")
                .withHostName("my_virtdb")
                .withEnv("DBA_PASSWORD=mysecret")
                .withPortBindings(portBindings)
                .withExposedPorts(new ExposedPort(Integer.parseInt(port)))
                .withBinds(b).exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        InspectContainerResponse i = dockerClient.inspectContainerCmd(container.getId()).exec();

        TimeUnit.SECONDS.sleep(15);

        int exitCode = i.getState().getExitCode();
        if (exitCode == 0) {
            //Loads the test data into the virtuoso triple store.
            exitCode = loadTestDataIntoVirtuoso(dataSetName, container.getId(), port);
        }

        return exitCode;
    }

    /**
     * This method removes all the system dataset in the virtuoso triple store and loads only the test dataset into the
     * virtuoso triple store.
     *
     * @param testDataset Test dataset name.
     * @param containerId Container id.
     * @param port        Port number.
     * @return Exit code.
     * @throws InterruptedException If Basilisk is interrupted.
     */
    private static int loadTestDataIntoVirtuoso(String testDataset, String containerId, String port) throws InterruptedException {
        FreeMarkerTemplateEngineUtils.setLoadingScript(testDataset, containerId, port);

        LoggerUtils.logForBasilisk(logPrefix,
                "Running loadTestData.sh script to load the test dataset into the virtuoso triple store.",
                1);
        String cmd = "./loadTestData.sh";

        int exitCode = 0;
        try {
            exitCode = new UnixUtils().runUnixCommand(cmd, new File(".").getAbsoluteFile(), true);

        } catch (IOException e) {
            e.printStackTrace();
            exitCode = -1994;
        }

        LoggerUtils.logForBasilisk(logPrefix,
                "Test dataset loaded successfully into virtuoso triple store.",
                1);
        new File("./loadTestData.sh").delete();
        return exitCode;
    }

    /**
     * This method runs the Fuseki docker.
     *
     * @param port
     * @param testDatasetPath
     * @param testDataset
     * @param serverName
     * @param path
     * @return
     * @throws IOException
     * @throws InterruptedException If Basilisk is interrupted.
     */
    public static int runFuesikiDocker(String port, String testDatasetPath, String testDataset,
                                       String serverName, File path) throws InterruptedException {
        //Todo: This is outdated. update the code for fuseki.
        String command = "docker run -p "
                + port + ":3030"
                + " -v "
                + testDatasetPath
                + ":/staging --name "
                + serverName + "_server cbm:" + serverName
                + " /jena-fuseki/fuseki-server --file /staging/"
                + testDataset + " /sparql";

        try {
            return new UnixUtils().runUnixCommand(command, path, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -100;
    }
}
