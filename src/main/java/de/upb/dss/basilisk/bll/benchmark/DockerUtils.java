package de.upb.dss.basilisk.bll.benchmark;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;

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

    public static boolean pullImage(String repoName, String tag) {
        if(config == null) {
            config = DefaultDockerClientConfig.createDefaultConfigBuilder();
        }

        if(dockerClient == null) {
            dockerClient = DockerClientBuilder
                    .getInstance(config)
                    .build();
        }

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

    public static void clearDocker() {
        if(config == null) {
            config = DefaultDockerClientConfig.createDefaultConfigBuilder();
        }

        if(dockerClient == null) {
            dockerClient = DockerClientBuilder
                    .getInstance(config)
                    .build();
        }

        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> exec = listContainersCmd.exec();

        Iterator<Container> itr = exec.iterator();

        while(itr.hasNext()) {
            Container c = itr.next();
            dockerClient.killContainerCmd(c.getId()).exec();
            dockerClient.removeContainerCmd(c.getId()).exec();
        }

        ListImagesCmd listImageCmd = dockerClient.listImagesCmd();
        List<Image> exe = listImageCmd.exec();

        Iterator<Image> itrr = exe.iterator();

        while(itrr.hasNext()) {
            Image c = itrr.next();
            System.out.println(c.getId());
            dockerClient.removeImageCmd(c.getId()).exec();
        }
    }
    public static void runTentrisDocker(String repoName, String tag, String port, String dataSetName) {
        if(config == null) {
            config = DefaultDockerClientConfig.createDefaultConfigBuilder();
        }

        if(dockerClient == null) {
            dockerClient = DockerClientBuilder
                    .getInstance(config)
                    .build();
        }

        ExposedPort tcp4444 = ExposedPort.tcp(Integer.parseInt(port));

        Ports portBindings = new Ports();
        portBindings.bind(tcp4444, Ports.Binding.bindPort(Integer.parseInt(port)));

        CreateContainerResponse container
                = dockerClient.createContainerCmd(repoName + ":" + tag)
                .withCmd(repoName + " -f /datasets/" + dataSetName + " -p " + port)
                .withName("tentris_server")
                .withHostName("testing_server")
                .withPortBindings(portBindings)
                .withExposedPorts(new ExposedPort(Integer.parseInt(port)))
                .withBinds(Bind.parse("dataset:/dataset")).exec();

        dockerClient.startContainerCmd(container.getId()).exec();
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
