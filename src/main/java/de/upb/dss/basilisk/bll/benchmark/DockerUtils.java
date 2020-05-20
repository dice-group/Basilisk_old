package de.upb.dss.basilisk.bll.benchmark;

import ch.qos.logback.core.util.TimeUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is the utility class for supporting the docker process.
 */
public class DockerUtils {
    private static DefaultDockerClientConfig.Builder config = null;
    private static DockerClient dockerClient = null;

    /**
     * This method sets up the docker java api be creating all the necessary object for docker java api.
     */
    private static void setUpDockerApi() {
        if(config == null) {
            config = DefaultDockerClientConfig.createDefaultConfigBuilder();
        }

        if(dockerClient == null) {
            dockerClient = DockerClientBuilder
                    .getInstance(config)
                    .build();
        }
    }

    /**
     * This method builds the Docker image.
     * @param repoName Repository name
     * @param tag Tag
     * @return Returns the Status code.
     */
    public static int buildImage(String repoName, String tag) {
        setUpDockerApi();

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
     * @param repoName Image's repository name.
     * @param tag Image's tag
     * @return Boolean to indicate whether pull command is success or not.
     */
    public static boolean pullImage(String repoName, String tag) {
        setUpDockerApi();

        boolean flag = false;
        try {
            flag = dockerClient.pullImageCmd(repoName)
                    .withTag(tag)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * This method removes all the container and images.
     */
    public static void clearDocker() {
        setUpDockerApi();

        //Delete all the containers
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> containerList = listContainersCmd.exec();

        Iterator<Container> containerItr = containerList.iterator();

        while(containerItr.hasNext()) {
            Container container = containerItr.next();
            dockerClient.killContainerCmd(container.getId()).exec();
            dockerClient.removeContainerCmd(container.getId()).exec();
        }

        //Remove all the containers which are in exited state.
        listContainersCmd = dockerClient.listContainersCmd().withStatusFilter("exited");
        containerList = listContainersCmd.exec();

        containerItr = containerList.iterator();

        while(containerItr.hasNext()) {
            Container c = containerItr.next();
            dockerClient.removeContainerCmd(c.getId()).exec();
        }

        ListImagesCmd listImageCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImageCmd.exec();

        Iterator<Image> imageItr = imageList.iterator();

        while(imageItr.hasNext()) {
            Image c = imageItr.next();
            System.out.println(c.getId());
            try {
                dockerClient.removeImageCmd(c.getId()).withNoPrune(false).withForce(true).exec();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method runs the Tentris docker for the given tag.
     * @param repoName Tentris's repository name
     * @param tag Tentris's tag
     * @param port Port number on which the Tentris should run.
     * @param dataSetName Dataset name to load into the Tentris triple store.
     */
    public static int runTentrisDocker(String repoName, String tag, String port, String dataSetName) {
        setUpDockerApi();
        String testDataSetPath = new ApplicationPropertiesUtils().getTestDatasetPath();

        ExposedPort tcp4444 = ExposedPort.tcp(Integer.parseInt(port));

        Ports portBindings = new Ports();
        portBindings.bind(tcp4444, Ports.Binding.bindPort(Integer.parseInt(port)));

        Bind b = new Bind(new File(testDataSetPath).getAbsolutePath(),new Volume("/datasets"), AccessMode.rw);
        CreateContainerResponse container
                = dockerClient.createContainerCmd(repoName + ":" + tag)
                .withCmd("-f", "/datasets/" + dataSetName, "-p", port)
                .withName("tentris_server")
                .withHostName("tentris_server")
                .withPortBindings(portBindings)
                .withExposedPorts(new ExposedPort(Integer.parseInt(port)))
                .withBinds(b).exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        InspectContainerResponse i = dockerClient.inspectContainerCmd(container.getId()).exec();
        System.out.println(i);
        int Ext = i.getState().getExitCode();
        System.out.println("Exited code = " + Ext);

        return Ext;
    }

    public static int runVirtuosoDocker(String repoName, String tag, String port, String dataSetName) {
        setUpDockerApi();
        String testDataSetPath = new ApplicationPropertiesUtils().getTestDatasetPath();

        ExposedPort tcpPort = ExposedPort.tcp(Integer.parseInt(port));

        Ports portBindings = new Ports();
        portBindings.bind(tcpPort, Ports.Binding.bindPort(Integer.parseInt(port)));

        Bind b = new Bind(new File(testDataSetPath).getAbsolutePath(),new Volume("/usr/share/proj"), AccessMode.rw);
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
        System.out.println(i.getState().getExitCode());

        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int exitCode = i.getState().getExitCode();
        if(exitCode == 0) {
            exitCode = loadTestDataIntoVirtuoso(dataSetName, container.getId(), port);
        }

        return exitCode;
    }

    private static int loadTestDataIntoVirtuoso(String testDataset,String containerId, String port) {
       FreeMarkerTemplateEngineUtils.setLoadingScript(testDataset, containerId, port);

       String cmd = "./loadTestData.sh";

       int exitCode = 0;
        try {
            exitCode = new UnixUtils().runUnixCommand(cmd, new File(".").getAbsoluteFile(), true);

        } catch (InterruptedException e) {
            e.printStackTrace();
            exitCode = -1994;
        } catch (IOException e) {
            e.printStackTrace();
            exitCode = -1994;
        }

        new File("./loadTestData.sh").delete();
        return exitCode;
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
