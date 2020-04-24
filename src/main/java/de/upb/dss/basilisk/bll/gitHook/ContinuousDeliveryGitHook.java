package de.upb.dss.basilisk.bll.gitHook;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;

import de.upb.dss.basilisk.bll.benchmark.BenchmarkForGitHook;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is the hook for Git hub for Continuous benchmarking process(CPB).
 */
public class ContinuousDeliveryGitHook {

    private JSONArray gitHookBenchmarkedFileData;
    private String currentBenchmarkedVersion, currentTripleStore;
    private ArrayList<String> alreadyBenchmarkedVersionsList;
    private final String continuousBmPath;
    private final String gitHookMetadataFileName;
    private final String gitHookBenchmarkedFileName;
    private final String errorLogFileName;
    private final String bmWorkspacePath;
    private String currentPortNum;
    private String currentDatasetFilePath;
    private String currentQueriesFilePath;

    /**
     * This constructs the ContinuousDeliveryGitHook object.
     *
     * @param continuousBmPath           Path to the continuousBM directory.
     * @param gitHookMetadataFileName    Git meta data file name.
     * @param gitHookBenchmarkedFileName Git already benchmarked file name.
     * @param errorLogFileName           Error log file name.
     * @param bmWorkspacePath            Path to the bmWorkSpace directory.
     */
    public ContinuousDeliveryGitHook(String continuousBmPath, String gitHookMetadataFileName, String gitHookBenchmarkedFileName,
                                     String errorLogFileName, String bmWorkspacePath) {
        super();
        this.continuousBmPath = continuousBmPath;
        this.gitHookMetadataFileName = gitHookMetadataFileName;
        this.gitHookBenchmarkedFileName = gitHookBenchmarkedFileName;
        this.errorLogFileName = errorLogFileName;
        this.bmWorkspacePath = bmWorkspacePath;
    }

    /**
     * This method updates the log file.
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
     * This method deletes the downloaded zip file and clears the benchmarking workspace for the next run.
     *
     * @throws IOException If fails to clear the bmWorkSpace directory.
     */
    public void delRepository() throws IOException {
        File file = new File(this.bmWorkspacePath + this.currentTripleStore + ".zip");
        file.delete();
        if (new File(this.bmWorkspacePath).isDirectory()) {
            // FileUtils.cleanDirectory(new File(this.bmWorkspacePath));
            FileUtils.deleteDirectory(new File(this.bmWorkspacePath));
            new File(this.bmWorkspacePath).mkdir();
        }
    }

    /**
     * This method updates the GitHookBenchmarked.json file to keep track of the already benchmarked versions.
     *
     * @throws JSONException If fails to parse the Json data.
     * @throws IOException   If fails to update the already benchmarked file.
     */
    public void updateVersionList() throws JSONException, IOException {
        this.alreadyBenchmarkedVersionsList.add(this.currentBenchmarkedVersion);
        try {
            for (int i = 0; i < this.gitHookBenchmarkedFileData.length(); i++) {
                JSONObject jsonObj = this.gitHookBenchmarkedFileData.getJSONObject(i);
                if (jsonObj.has(this.currentTripleStore)) {
                    jsonObj.put(this.currentTripleStore, this.alreadyBenchmarkedVersionsList);
                    this.gitHookBenchmarkedFileData.put(i, jsonObj);
                }
            }

            FileWriter f = new FileWriter(this.continuousBmPath + this.gitHookBenchmarkedFileName, false);
            f.write(this.gitHookBenchmarkedFileData.toString(2));
            f.close();
        } catch (FileNotFoundException e) {
            this.updateErrorLog("Benchmarked.json file could not updated", e.toString());
            e.printStackTrace();
        } catch (JSONException e) {
            this.updateErrorLog("Could be a JSON file parsing issue", e.toString());
            e.printStackTrace();
        }
        try {
            this.delRepository();
        } catch (Exception e) {
            this.updateErrorLog("Zip file could not be deleted successfully", e.toString());
            e.printStackTrace();
        }
    }

    /**
     * This method runs the benchmarking process on the currently downloaded git repository.
     *
     * @throws IOException          If I/O occurs while running the command.
     * @throws InterruptedException If the process is interrupted.
     */
    public void benchmark() throws IOException, InterruptedException {
        File zipFile = new File(this.bmWorkspacePath + this.currentTripleStore + ".zip");
        String tempStore = this.currentTripleStore;
        if (zipFile.exists()) {
            Extraction obj = new Extraction();
            if (this.currentTripleStore.equalsIgnoreCase("Fuseki")) {
                obj.unzipJena(this.bmWorkspacePath + tempStore + ".zip", this.bmWorkspacePath);
            } else {
                obj.unzipGeneric(this.bmWorkspacePath + tempStore + ".zip", this.bmWorkspacePath);
            }

            BenchmarkForGitHook.runBenchmark(this.currentPortNum, this.currentTripleStore, this.currentDatasetFilePath,
                    this.currentQueriesFilePath, this.currentBenchmarkedVersion.replace(" ", ""));
        } else {
            this.updateErrorLog(tempStore + " zip file not found", "");
        }

        try {
            this.updateVersionList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method downloads the git repository from the given url into the benchmarking
     * workspace.
     *
     * @param url URL to the git repository.
     * @return Status code whether successfully downloaded the repository from the git or not.
     * @throws IOException If I/O occurs while running the command.
     */
    public int downloadRepo(String url) throws IOException {
        System.out.println("Downloading of the release(.zip) will start ..");
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(
                     this.bmWorkspacePath + this.currentTripleStore + ".zip")) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            return 1;
        } catch (FileNotFoundException e) {
            this.updateErrorLog("Exception occurred in saving code repository", e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            this.updateErrorLog("Exception occurred in downloading code repository", e.toString());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * This method checks whether the benchmark is already run for the current version, if not, it will
     * download the git repository and continue with the CPB process. This process continues for all
     * the tags in the dockerHubTagsJsonArray parameter.
     *
     * @param githubJsonArray         List of all the version for a single triple store.
     * @param benchmarkedVersionsList List of all version already benchmarked.
     * @throws JSONException        If fails in processing the Json data.
     * @throws IOException          If I/O occurs while running the command.
     * @throws InterruptedException If the process is interrupted.
     */
    public void check(JSONArray githubJsonArray, ArrayList<String> benchmarkedVersionsList)
            throws JSONException, IOException, InterruptedException {
        for (int i = 0; i < githubJsonArray.length(); i++) {
            try {
                JSONObject versionObj = githubJsonArray.getJSONObject(i);
                if (!benchmarkedVersionsList.contains(versionObj.get("name"))) {
                    this.delRepository();
                    this.currentBenchmarkedVersion = (String) versionObj.get("name");
                    System.out.println("Benchmarking will run for version: " + this.currentBenchmarkedVersion);
                    int flag = this.downloadRepo((String) versionObj.get("zipball_url"));
                    if (flag == 1) {
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
     * This methods connects to git hub and retrieves the list of all the version available for the given triple store
     * name in the command.
     *
     * @param command Command to get the version from the git hub for a particular triple store.
     * @return List of all the version available in the docker hub.
     * @throws IOException   If I/O error occurs while running the given command.
     * @throws JSONException If fails in processing the Json data.
     */
    public JSONArray getGithubTags(String command) throws IOException, JSONException {
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
        Process p;
        JSONArray jArr = null;

        p = pb.start();
        InputStream is = p.getInputStream();
        String s = IOUtils.toString(is, StandardCharsets.UTF_8);
        if (s.length() != 0) {
            if (s.charAt(0) == '[') {
                jArr = new JSONArray(s);
                p.destroy();
                return jArr;
            } else {
                this.updateErrorLog(
                        "Cannot connect to github to fetch tags or curl command incorrect causing JSON parsing issue",
                        "");
                return new JSONArray("[]");
            }
        } else {
            this.updateErrorLog(
                    "Cannot connect to github to fetch tags or curl command incorrect causing JSON parsing issue", "");
            return new JSONArray("[]");
        }
    }

    /**
     * This method returns the information of the already benchmarked version in the git hook
     * for the given triple store name.
     *
     * @param tripleStoreName Triple store name.
     * @return List of all the version already tried benchmarking process for the given triple store name.
     * @throws JSONException If fails in processing the Json data.
     * @throws IOException   If I/O error occurs while running the given command.
     */
    public ArrayList<String> getBenchmarkedDetails(String tripleStoreName) throws JSONException, IOException {
        try {
            for (int i = 0; i < this.gitHookBenchmarkedFileData.length(); i++) {
                JSONObject currentBenchmarkedFileObject = this.gitHookBenchmarkedFileData.getJSONObject(i);
                if (currentBenchmarkedFileObject.has(tripleStoreName)) {
                    ArrayList<String> list = new ArrayList<String>();
                    JSONArray jsonArray = (JSONArray) currentBenchmarkedFileObject.get(tripleStoreName);
                    for (int i1 = 0; i1 < jsonArray.length(); i1++) {
                        list.add(jsonArray.get(i1).toString());
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
     * This method runs for the Tentris and fuesiki triple stores, which downloads the git repo from the git hub
     * and runs the benchmarking process on the respective triple store.
     *
     * @return Status code.
     * @throws IOException          If I/O error occurs while running the given command.
     * @throws InterruptedException If the process is interrupted.
     */
    public int forEachStore() throws IOException, InterruptedException {

        try {
            this.gitHookBenchmarkedFileData = new JSONArray(new String(
                    Files.readAllBytes(
                            Paths.get(this.continuousBmPath + this.gitHookBenchmarkedFileName)),
                    StandardCharsets.UTF_8));

            JSONArray metadataFileArray = new JSONArray(new String(
                    Files.readAllBytes(
                            Paths.get(this.continuousBmPath + this.gitHookMetadataFileName)),
                    StandardCharsets.UTF_8));

            for (int i = 0; i < metadataFileArray.length(); i++) {
                JSONObject jsonObj = metadataFileArray.getJSONObject(i);
                this.currentTripleStore = (String) jsonObj.get("name");
                this.currentPortNum = (String) jsonObj.get("port");
                this.currentDatasetFilePath = (String) jsonObj.get("dataset");
                this.currentQueriesFilePath = (String) jsonObj.get("queriesFilePath");
                System.out.println("Currently checking for this triple store: " + this.currentTripleStore);
                this.alreadyBenchmarkedVersionsList = this.getBenchmarkedDetails(this.currentTripleStore);
                JSONArray githubJsonArray = this.getGithubTags((String) jsonObj.get("command"));
                this.check(githubJsonArray, this.alreadyBenchmarkedVersionsList);
            }
        } catch (IOException e) {
            this.updateErrorLog("GitMetadata.json or GitBenchmarked.json file could not found", e.toString());
            e.printStackTrace();
        } catch (JSONException e) {
            this.updateErrorLog("Could be a GitMetadata.json or GitBenchmarked.json file parsing issue", e.toString());
            e.printStackTrace();
        }
        System.out.println("One run completed");
        return 0;
    }
}
