package de.upb.dss.basilisk.bll.dockerHook;

import de.upb.dss.basilisk.bll.Hook.Yaml.YamlUtils;
import de.upb.dss.basilisk.bll.benchmark.BenchmarkForDockerHook;
import de.upb.dss.basilisk.bll.benchmark.DockerUtils;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * This is the hook for Docker hub for Continuous benchmarking process(CPB).
 */
public class ContinuousDeliveryDockerHook {

    private JSONArray dockerHookBenchmarkedFileData;
    private String currentBenchmarkedTag, currentTripleStore, currentRepoName;
    private ArrayList<String> alreadyBenchmarkedTagList;
    private String currentPortNum;
    private String currentDatasetFilePath;
    private String currentQueriesFilePath;
    private static final String logPrefix = "Docker Hook";


    /**
     * This method updates the DockerBenchmarkedAttempted.yml file to keep track of the already
     * benchmarked tags.
     */
    public void updateTagList() {
        this.alreadyBenchmarkedTagList.add(this.currentBenchmarkedTag);

        this.dockerHookBenchmarkedFileData = YamlUtils.addTagToDockerBenchmarkedAttempted(
                this.dockerHookBenchmarkedFileData,
                this.currentBenchmarkedTag,
                this.currentTripleStore);

        //Clears the docker environment for the next iteration.
        DockerUtils.clearDocker();
    }

    /**
     * This method checks whether the benchmark is already run for the current tag, if not, it will
     * pull the docker image for the current tag and continue with the CPB process. This process continues for all
     * the tags in the dockerHubTagsJsonArray parameter.
     *
     * @param dockerHubTagsJsonArray List of all the tags for a single triple store.
     * @throws InterruptedException If Basilisk is interrupted.
     */
    public void checkAndRunCPB(JSONArray dockerHubTagsJsonArray) throws InterruptedException {
        try {
            for (int i = 0; i < dockerHubTagsJsonArray.length(); i++) {
                JSONObject singleTagData = dockerHubTagsJsonArray.getJSONObject(i);

                String tag = (String) singleTagData.get("name");

                if (!this.alreadyBenchmarkedTagList.contains(tag)) {
                    //Clears the docker environment before starting the benchmark process.
                    DockerUtils.clearDocker();

                    this.currentBenchmarkedTag = tag;

                    boolean flag = DockerUtils.pullImage(this.currentRepoName, tag);

                    if (flag) {
                        LoggerUtils.logForBasilisk(logPrefix,
                                currentRepoName + ":" + this.currentBenchmarkedTag +
                                        ": Basilisk will run benchmarking process on.",
                                1);

                        //Calls the Benchmarking process on the currently pulled triple store.
                        BenchmarkForDockerHook.runBenchmarkForDockerHook(currentPortNum, currentTripleStore, currentDatasetFilePath,
                                currentQueriesFilePath, currentRepoName, currentBenchmarkedTag);

                        //Add the current triple store in the already benchmarked file.
                        this.updateTagList();
                    } else {
                        LoggerUtils.logForBasilisk(logPrefix,
                                "Basilisk failed to pull the docker image for: " + currentRepoName + ":" +
                                        this.currentBenchmarkedTag,
                                1);
                    }
                }
            }
        } catch (JSONException e) {
            LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while parsing the JSON object.", 4);
            e.printStackTrace();
        }

    }

    /**
     * This methods connects to docker hub and retrieves the list of all the tags available for the given triple store
     * name in the command.
     *
     * @param command Command to get the tags from the docker hub for a particular triple store.
     * @return List of all the tags available in the docker hub.
     */
    public JSONArray getDockerHubTags(String command) {
        JSONArray dockerHubTags;
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));

        String s = "";
        Process p = null;
        try {
            p = pb.start();

            InputStream processIS = p.getInputStream();
            s = IOUtils.toString(processIS, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoggerUtils.logForBasilisk(logPrefix,
                    "Something went wrong while trying to fetch the list of tags from Docker hub.",
                    4);
            e.printStackTrace();
            return new JSONArray("[]");
        }

        if (s.length() != 0) {
            if (s.charAt(0) == '[') {
                dockerHubTags = new JSONArray(s);

                p.destroy();

                return dockerHubTags;
            } else {
                LoggerUtils.logForBasilisk(logPrefix,
                        "Cannot connect to docker hub to fetch tags or curl command incorrect causing" +
                                "JSON parsing issue",
                        4);
                return new JSONArray("[]");
            }
        } else {
            LoggerUtils.logForBasilisk(logPrefix,
                    "Cannot connect to docker hub to fetch tags or curl command incorrect causing" +
                            "JSON parsing issue",
                    4);
            return new JSONArray("[]");
        }
    }

    /**
     * This method returns the information of the already benchmarked tags in the docker hook
     * for the given triple store name.
     *
     * @param tripleStoreName Triple store name.
     * @return List of all the tags already tried benchmarking process for the given triple store name.
     * @throws JSONException If fails to parse the Json data.
     */
    public ArrayList<String> getBenchmarkedDetails(String tripleStoreName) throws JSONException {
        try {
            for (int i = 0; i < this.dockerHookBenchmarkedFileData.length(); i++) {
                JSONObject singleTripleStoreData = this.dockerHookBenchmarkedFileData.getJSONObject(i);
                if (singleTripleStoreData.has(tripleStoreName)) {
                    ArrayList<String> list = new ArrayList<>();
                    JSONArray currentBenchmarkedData = singleTripleStoreData.getJSONArray(tripleStoreName);
                    for (int j = 0; j < currentBenchmarkedData.length(); j++) {
                        list.add(currentBenchmarkedData.get(j).toString());
                    }
                    return list;
                }
            }
        } catch (JSONException e) {
            LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while parsing the JSON object.", 4);
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * This method runs for the Tentris and Virtuoso triple stores, which pulls the each docker image from the
     * docker hub and runs the benchmarking process on the respective triple stores.
     *
     * @return Exit code.
     * @throws InterruptedException If Basilisk is interrupted.
     */
    public int forEachStore() throws InterruptedException {

        this.dockerHookBenchmarkedFileData = YamlUtils.getDockerBenchmarkAttempted();

        try {
            JSONArray metaDataFileArray = YamlUtils.getDockerMetaData();

            for (int i = 0; i < metaDataFileArray.length(); i++) {
                JSONObject singleStoreMetaData = metaDataFileArray.getJSONObject(i);

                this.currentTripleStore = (String) singleStoreMetaData.get("name");
                this.currentRepoName = (String) singleStoreMetaData.get("repositoryName");
                this.currentPortNum = (String) singleStoreMetaData.get("port");
                this.currentDatasetFilePath = (String) singleStoreMetaData.get("dataset");
                this.currentQueriesFilePath = (String) singleStoreMetaData.get("queriesFilePath");

                LoggerUtils.logForBasilisk(logPrefix, "Currently checking for " + this.currentTripleStore + " triple store.", 1);

                //Get the list of all the tag of the current triple store that is already benchmarked in Docker hook.
                this.alreadyBenchmarkedTagList = this.getBenchmarkedDetails(this.currentTripleStore);

                //Get the list of all tags from the docker hub for the current triple store.
                JSONArray dockerHubTagsJsonArray = this.getDockerHubTags((String) singleStoreMetaData.get("command"));

                /*
                Check all the tags from the docker hub and run the benchmark process
                for the tags that is not yet benchmarked.
                 */
                this.checkAndRunCPB(dockerHubTagsJsonArray);
            }
        } catch (JSONException e) {
            LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while parsing the JSON object.", 4);
            e.printStackTrace();
        }

        LoggerUtils.logForBasilisk(logPrefix, "Basilisk completed benchmark process  on Docker hook.", 1);
        return 0;
    }
}
