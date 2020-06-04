package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This class runs the current triple store from Git hook and then runs the Iguana for benchmarking the triple store.
 *
 * @author Ranjith Krishnamurthy
 * @author Rahul Sethi
 */
public class BenchmarkForGitHook {
    private static File dockerFile;

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
     * @throws IOException If fails to rename the results file.
     */
    public static void runBenchmark(String argPort, String argRepoName, String argTestDataSet, String argQueryFile, String argVersionNumber) throws IOException {
        ApplicationPropertiesUtils myAppUtils = new ApplicationPropertiesUtils();

        dockerFile = new File(myAppUtils.getDockerFile());
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
        if (exitCode == 0)
            ResultStoringFusekiUtils.processResultFIle(repoName, repoName, tag);
        else
            //Todo: Just delete the result file if any
            System.out.println("");

        //Clear the docker, so that next benchmark can be run.
        DockerUtils.clearDocker();
    }

    /**
     * This method builds the docker image and runs the respective docker image, then it calls the Iguana to run the
     * benchmarking process.
     *
     * @return Exit code.
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
                    dockerStatusCode = DockerUtils.runFuesikiDocker(repoName, tag, port, testDataset);

                    if (dockerStatusCode == 0) {
                        if (createTestDataSetInFuseki() != 0)
                            return -180;

                        loadTestDataInFuseki();
                    }
                }

                if (dockerStatusCode == 0) {
                    int iguanaExitCode = IguanaUtils
                            .runIguana(repoName, tag, port, queryFile);

                    if (iguanaExitCode != 0)
                        return iguanaExitCode;
                } else {
                    LoggerUtils.logForBasilisk(logPrefix, repoName + ":" + tag +
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

    /**
     * Creates a Dataset in the currently running Fuseki triple store to load the test data.
     *
     * @return Exit code.
     */
    private static int createTestDataSetInFuseki() {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(
                new ApplicationPropertiesUtils().getBasiliskEndPoint() + port + "/$/datasets");

        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("dbType", "tdb"));
            params.add(new BasicNameValuePair("dbName", "sparql"));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
                    // do something useful
                    System.out.println(instream.toString());
                }
            }
        } catch (Exception ex) {
            LoggerUtils.logForBasilisk(logPrefix, "Something went wrong", 4);
            ex.printStackTrace();
            return -180;
        }
        return 0;
    }

    /**
     * This loads the test data into the Fuseki triple store.
     */
    private static void loadTestDataInFuseki() {
        ApplicationPropertiesUtils myAppProp = new ApplicationPropertiesUtils();
        RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
                .destination(myAppProp.getBasiliskEndPoint() + port + "/sparql");

        RDFConnection connection = builder.build();

        Model model = ModelFactory.createDefaultModel();
        model.read(myAppProp.getTestDatasetPath() + "/" + testDataset);


        connection.load(model);

        connection.commit();
        connection.close();
    }
}
