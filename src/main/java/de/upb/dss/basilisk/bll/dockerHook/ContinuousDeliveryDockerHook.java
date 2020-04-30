package de.upb.dss.basilisk.bll.dockerHook;

import de.upb.dss.basilisk.bll.Hook.Yaml.YamlUtils;
import de.upb.dss.basilisk.bll.benchmark.BenchmarkForDockerHook;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * This is the hook for Docker hub for Continuous benchmarking process(CPB).
 */
public class ContinuousDeliveryDockerHook {

    private JSONArray dockerHookBenchmarkedFileData;
    private String currentBenchmarkedTag, currentTripleStore, currentRepoName;
    private ArrayList<String> alreadyBenchmarkedTagList;
    private final String continuousBmPath;
    private final String dockerHookMetadataFileName;
    private final String dockerHookBenchmarkedFileName;
    private final String errorLogFileName;
    private final String bmWorkspacePath;
    private String currentPortNum;
    private String currentDatasetFilePath;
    private String currentQueriesFilePath;

    /**
     * This constructs the ContinuousDeliveryDockerHook object.
     *
     * @param continuousBmPath    Path to the continuousBM directory.
     * @param metadataFileName    Docker meta data file name.
     * @param benchmarkedFileName Docker already benchmarked file name.
     * @param errorLogFileName    Error log file name.
     * @param bmWorkspacePath     Path to the bmWorkSpace directory.
     */
    public ContinuousDeliveryDockerHook(String continuousBmPath, String metadataFileName, String benchmarkedFileName,
                                        String errorLogFileName, String bmWorkspacePath) {
        super();
        this.continuousBmPath = continuousBmPath;
        this.dockerHookMetadataFileName = metadataFileName;
        this.dockerHookBenchmarkedFileName = benchmarkedFileName;
        this.errorLogFileName = errorLogFileName;
        this.bmWorkspacePath = bmWorkspacePath;
    }

    /**
     * This method updates the log.
     *
     * @param GeneralDesc      General description.
     * @param ExceptionMessage Exception message.
     */
    public void updateErrorLog(String GeneralDesc, String ExceptionMessage) {
        try {
            FileWriter er = new FileWriter(this.continuousBmPath + this.errorLogFileName, true);
            er.write(Calendar.getInstance().getTime() + "~" + this.currentTripleStore + "~" + GeneralDesc + "~"
                    + ExceptionMessage + "~" + Calendar.getInstance().getTimeInMillis() + "\n");
            er.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the docker environment.
     */
    public void clearDockerEnv() {
        //Todo: Clear the docker enviornment. All the container and images remove process.
    }

    /**
     * This method updates the DockerHookBenchmarked.json file to keep track of the already
     * benchmarked tags.
     *
     * @throws JSONException If fails to parse the Json data.
     * @throws IOException   If I/O error occurs while running the command to pull the docker image.
     */
    public void updateTagList() throws JSONException, IOException {

        this.alreadyBenchmarkedTagList.add(this.currentBenchmarkedTag);

        this.dockerHookBenchmarkedFileData = YamlUtils.addTagToDockerBenchmarkedAttempted(this.dockerHookBenchmarkedFileData,
                this.currentBenchmarkedTag,
                this.currentTripleStore);

        try {
            this.clearDockerEnv();
        } catch (Exception e) {
            this.updateErrorLog("Zip file could not be deleted successfully", e.toString());
            e.printStackTrace();
        }
    }

    /**
     * This method runs the benchmarking process on the currently pulled docker image.
     *
     * @throws IOException If I/O error occurs while running the command to pull the docker image.
     */
    public void benchmark() throws IOException, InterruptedException {
        //Todo: Run the benchmarking process on the currently pulled docker image.
        BenchmarkForDockerHook.runBenchmarkForDockerHook(currentPortNum, currentTripleStore, currentDatasetFilePath,
                currentQueriesFilePath, currentRepoName, currentBenchmarkedTag);
        System.out.println("Running benchmark");

        try {
            this.updateTagList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method pulls the docker images from the docker hub.
     *
     * @param repoName Repository name.
     * @param tag      Tag.
     * @return Status code.
     * @throws IOException If I/O error occurs while running the command to pull the docker image.
     */
    public int pullDockerImage(String repoName, String tag) throws IOException {
        Process p = Runtime.getRuntime().exec("docker pull " + repoName + ":" + tag);

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return p.exitValue();
    }

    /**
     * This method checks whether the benchmark is already run for the current tag, if not, it will
     * pull the docker image for the current tag and continue with the CPB process. This process continues for all
     * the tags in the dockerHubTagsJsonArray parameter.
     *
     * @param dockerHubTagsJsonArray List of all the tags for a single triple store.
     * @throws IOException If I/O error occurs while running the command to pull the docker image.
     */
    public void checkAndRunCPB(JSONArray dockerHubTagsJsonArray) throws InterruptedException, IOException {
        for (int i = 0; i < dockerHubTagsJsonArray.length(); i++) {
            try {
                JSONObject singleTagData = dockerHubTagsJsonArray.getJSONObject(i);
                if (!this.alreadyBenchmarkedTagList.contains(singleTagData.get("name"))) {
                    this.clearDockerEnv();
                    this.currentBenchmarkedTag = (String) singleTagData.get("name");
                    System.out.println("Benchmarking will run for version: " + this.currentBenchmarkedTag);
                    int flag = this.pullDockerImage(this.currentRepoName, (String) singleTagData.get("name"));
                    if (flag == 0) {
                        this.benchmark();
                    }
                }
            } catch (JSONException e) {
                this.updateErrorLog("Could be a JSON file parsing issue", e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * This methods connects to docker hub and retrieves the list of all the tags available for the given triple store
     * name in the command.
     *
     * @param command Command to get the tags from the docker hub for a particular triple store.
     * @return List of all the tags available in the docker hub.
     * @throws IOException If I/O error occurs while running the given command.
     */
    public JSONArray getDockerHubTags(String command) throws IOException {
        JSONArray dockerHubTags = null;
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));

        Process p = pb.start();
        InputStream processIS = p.getInputStream();
        String s = IOUtils.toString(processIS, StandardCharsets.UTF_8);
        if (s.length() != 0) {
            if (s.charAt(0) == '[') {
                dockerHubTags = new JSONArray(s);
                p.destroy();
                return dockerHubTags;
            } else {
                this.updateErrorLog(
                        "Cannot connect to docker hub to fetch tags or curl command incorrect causing JSON parsing issue",
                        "");
                return new JSONArray("[]");
            }
        } else {
            this.updateErrorLog(
                    "Cannot connect to docker hub to fetch tags or curl command incorrect causing JSON parsing issue", "");
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
                    ArrayList<String> list = new ArrayList<String>();
                    JSONArray currentBenchmarkedData = singleTripleStoreData.getJSONArray(tripleStoreName);
                    for (int j = 0; j < currentBenchmarkedData.length(); j++) {
                        list.add(currentBenchmarkedData.get(j).toString());
                    }
                    return list;
                }
            }
        } catch (JSONException e) {
            this.updateErrorLog("Could be a JSON file parsing issue", e.toString());
            e.printStackTrace();
        }
        return new ArrayList<String>();
    }

    /**
     * This method runs for the Tentris and Virtuoso triple stores, which pulls the each docker image from the
     * docker hub and runs the benchmarking process on the respective triple stores.
     *
     * @return Status code.
     */
    public int forEachStore() throws InterruptedException {
        try {
            this.dockerHookBenchmarkedFileData = YamlUtils.getDockerBenchmarkAttempted();

            JSONArray metaDataFileArray = YamlUtils.getDockerMetaData();

            for (int i = 0; i < metaDataFileArray.length(); i++) {
                JSONObject singleStoreMetaData = metaDataFileArray.getJSONObject(i);
                this.currentTripleStore = (String) singleStoreMetaData.get("name");
                this.currentRepoName = (String) singleStoreMetaData.get("repositoryName");
                this.currentPortNum = (String) singleStoreMetaData.get("port");
                this.currentDatasetFilePath = (String) singleStoreMetaData.get("dataset");
                this.currentQueriesFilePath = (String) singleStoreMetaData.get("queriesFilePath");
                System.out.println("Currently checking for this triple store: " + this.currentTripleStore);
                this.alreadyBenchmarkedTagList = this.getBenchmarkedDetails(this.currentTripleStore);
                JSONArray dockerHubTagsJsonArray = this.getDockerHubTags((String) singleStoreMetaData.get("command"));
                this.checkAndRunCPB(dockerHubTagsJsonArray);
            }
        } catch (IOException e) {
            this.updateErrorLog("DockerHookMetadata.json or DockerHookBenchmarked.json file could not found", e.toString());
            e.printStackTrace();
        } catch (JSONException e) {
            this.updateErrorLog("Could be a DockerHookMetadata.json or DockerHookBenchmarked.json file parsing issue", e.toString());
            e.printStackTrace();
        }
        System.out.println("One run completed");
        return 0;
    }
}
