package de.upb.dss.basilisk.bll.gitHook;

import de.upb.dss.basilisk.StatisticsOutput.BasiliskRunStatisticsData;
import de.upb.dss.basilisk.StatisticsOutput.BasiliskRunStatus;
import de.upb.dss.basilisk.bll.Hook.Yaml.YamlUtils;
import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import de.upb.dss.basilisk.bll.benchmark.BenchmarkForGitHook;
import de.upb.dss.basilisk.bll.benchmark.FreeMarkerTemplateEngineUtils;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * This is the hook for Git hub for Continuous benchmarking process(CPB).
 *
 * @author Jalaj Bajpai
 * @author Ranjith Krishnamurthy
 */
public class ContinuousDeliveryGitHook {

    private JSONArray gitHookBenchmarkedFileData;
    private String currentBenchmarkedVersion, currentTripleStore;
    private ArrayList<String> alreadyBenchmarkedVersionsList;
    private final String bmWorkspacePath;
    private String currentPortNum;
    private String currentDatasetFilePath;
    private String currentQueriesFilePath;
    private String currentTripleStoreDigest;
    private static final String logPrefix = "Git Hook";
    private BasiliskRunStatisticsData basiliskRunStatisticsData = new BasiliskRunStatisticsData();

    /**
     * This constructs the ContinuousDeliveryGitHook object.
     */
    public ContinuousDeliveryGitHook() {
        super();
        this.bmWorkspacePath = new ApplicationPropertiesUtils().getBmWorkSpace();
    }

    /**
     * This method deletes the downloaded zip file and clears the benchmarking workspace for the next run.
     */
    public void delRepository() {
        //Delete the zip file.
        File file = new File(this.bmWorkspacePath + this.currentTripleStore + ".zip");
        file.delete();

        //Clear the bmWorkSpace directory for next run.
        if (new File(this.bmWorkspacePath).isDirectory()) {
            try {
                FileUtils.deleteDirectory(new File(this.bmWorkspacePath));
            } catch (IOException e) {
                LoggerUtils.logForBasilisk(logPrefix, "Could not clear bmWorkSpace directory got an IOException", 4);
                e.printStackTrace();
            }
            new File(this.bmWorkspacePath).mkdir();
        }
    }

    /**
     * This method updates the GitBenchmarkedAttempted.yml file to keep track of the already benchmarked versions.
     */
    public void updateVersionList() {
        this.alreadyBenchmarkedVersionsList.add(this.currentTripleStoreDigest);
        this.gitHookBenchmarkedFileData = YamlUtils.addVersionToGitBenchmarkedAttempted(
                this.gitHookBenchmarkedFileData,
                this.currentTripleStoreDigest,
                this.currentTripleStore
        );

        this.delRepository();
    }

    /**
     * This method unzips the zip file downloaded for the current triple store.
     */
    public int unzipGitFile() {
        File zipFile = new File(this.bmWorkspacePath + this.currentTripleStore + ".zip");
        String tempStore = this.currentTripleStore;

        if (zipFile.exists()) {
            Extraction obj = new Extraction();

            if (this.currentTripleStore.equalsIgnoreCase("Fuseki")) {
                try {
                    if (!obj.unzipJena(this.bmWorkspacePath + tempStore + ".zip", this.bmWorkspacePath)) {
                        LoggerUtils.logForBasilisk(logPrefix,
                                this.currentBenchmarkedVersion + " release does not contain fuseki2 project.",
                                4);
                        this.updateVersionList();

                        basiliskRunStatisticsData.addTripleStore(
                                this.currentTripleStore,
                                this.currentBenchmarkedVersion.replace(" ", ""),
                                currentTripleStoreDigest,
                                "Git",
                                BasiliskRunStatus.RUN_FAIL
                        );

                        return -1;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return -1;
                }
            } else {
                try {
                    obj.unzipGeneric(this.bmWorkspacePath + tempStore + ".zip", this.bmWorkspacePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    return -1;
                }
            }
        } else {
            LoggerUtils.logForBasilisk(logPrefix, "zip file not found for: " + tempStore, 4);
            return -1;
        }

        return 0;
    }

    /**
     * This method downloads the git repository from the given url into the benchmarking
     * workspace.
     *
     * @param url URL to the git repository.
     * @return Status code whether successfully downloaded the repository from the git or not.
     */
    public int downloadRepo(String url) {
        LoggerUtils.logForBasilisk(logPrefix, "Downloading of the release(.zip) will start....", 1);

        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(
                     this.bmWorkspacePath + this.currentTripleStore + ".zip")) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            return 1;
        } catch (FileNotFoundException e) {
            LoggerUtils.logForBasilisk(logPrefix, "Exception occurred in saving code repository", 4);
            e.printStackTrace();
        } catch (IOException e) {
            LoggerUtils.logForBasilisk(logPrefix, "Exception occurred in downloading code repository", 4);
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * This method creates the shiro.ini security configuration file required for Fuseki server.
     *
     * @return Exit code.
     */
    private int createSecurityConfigFileForFuseki() {
        LoggerUtils.logForBasilisk(logPrefix, "Creating shiro.ini for fuseki", 1);
        try {
            File shiroFile = new File(new ApplicationPropertiesUtils().getBmWorkSpace() + "shiro.ini");

            String shiroContent = "[main]\n" +
                    "ssl.enabled = false\n" +
                    "plainMatcher=org.apache.shiro.authc.credential.SimpleCredentialsMatcher\n" +
                    "iniRealm.credentialsMatcher = $plainMatcher\n" +
                    "[users]\n" +
                    "admin=pw\n" +
                    "[roles]\n" +
                    "[urls]\n" +
                    "/$/status  = anon\n" +
                    "/$/ping    = anon\n" +
                    "/$/metrics = anon\n" +
                    "/$/** = anon\n" +
                    "/**=anon";

            if (shiroFile.exists())
                shiroFile.delete();

            FileWriter fileWriter = new FileWriter(shiroFile);

            fileWriter.write(shiroContent);
            fileWriter.close();
        } catch (IOException e) {
            LoggerUtils.logForBasilisk(logPrefix,
                    "Something went wrong while creating shiro.ini for fuseiki", 4);
            e.printStackTrace();
            return -1;
        }

        LoggerUtils.logForBasilisk(logPrefix, "Successfully created shiro.ini for fuseki", 1);

        return 0;
    }

    /**
     * This method checks whether the benchmark is already run for the current version, if not, it will
     * download the git repository and continue with the CPB process. This process continues for all
     * the tags in the dockerHubTagsJsonArray parameter.
     *
     * @param githubJsonArray         List of all the version for a single triple store.
     * @param benchmarkedVersionsList List of all version already benchmarked.
     */
    public void checkAndRunCPB(JSONArray githubJsonArray, ArrayList<String> benchmarkedVersionsList) throws InterruptedException {
        for (int i = 0; i < githubJsonArray.length(); i++) {
            try {
                JSONObject versionObj = githubJsonArray.getJSONObject(i);

                String version = (String) versionObj.get("name");
                String digest = (String) versionObj.getJSONObject("commit").get("sha");

                if (!benchmarkedVersionsList.contains(digest)) {
                    //Delete the previous zip file if exist and clear the bmWorkSpace directory.
                    this.delRepository();

                    this.currentBenchmarkedVersion = version;
                    this.currentTripleStoreDigest = digest;

                    int flag = this.downloadRepo((String) versionObj.get("zipball_url"));

                    if (flag == 1) {
                        int code = this.unzipGitFile();

                        if (code == 0) {
                            if (currentTripleStore.equals("fuseki")) {
                                int exitCode = FreeMarkerTemplateEngineUtils.setDockerfileForFuseki(currentPortNum);

                                if (exitCode != 0)
                                    continue;

                                exitCode = createSecurityConfigFileForFuseki();

                                if (exitCode != 0)
                                    continue;
                            }

                            LoggerUtils.logForBasilisk(logPrefix, "Benchmarking will run for version: " + this.currentBenchmarkedVersion, 1);

                            int exitCode = BenchmarkForGitHook.runBenchmark(this.currentPortNum, this.currentTripleStore, this.currentDatasetFilePath,
                                    this.currentQueriesFilePath, this.currentBenchmarkedVersion.replace(" ", ""));

                            if (exitCode == 0) {
                                basiliskRunStatisticsData.addTripleStore(
                                        this.currentTripleStore,
                                        this.currentBenchmarkedVersion.replace(" ", ""),
                                        currentTripleStoreDigest,
                                        "Git",
                                        BasiliskRunStatus.RUN_SUCCESS
                                );
                            } else {
                                basiliskRunStatisticsData.addTripleStore(
                                        this.currentTripleStore,
                                        this.currentBenchmarkedVersion.replace(" ", ""),
                                        currentTripleStoreDigest,
                                        "Git",
                                        BasiliskRunStatus.RUN_FAIL
                                );
                            }

                            //Add the current verion to the already benchmarked file.
                            this.updateVersionList();
                        }
                    }
                } else {
                    LoggerUtils.logForBasilisk(logPrefix,
                            "Already CBP ran for " + this.currentTripleStore + ":" + version,
                            1);

                    basiliskRunStatisticsData.addTripleStore(
                            this.currentTripleStore,
                            version,
                            digest,
                            "Git",
                            BasiliskRunStatus.ALREADY_RAN
                    );
                }
            } catch (JSONException e) {
                LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while parsing the JSON object.", 4);
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
     */
    public JSONArray getGithubTags(String command) {
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
        Process p;
        JSONArray jArr;

        String s;

        try {
            p = pb.start();

            InputStream is = p.getInputStream();
            s = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while trying to fetch the list of tags from Docker hub.", 4);
            e.printStackTrace();
            return new JSONArray("[]");
        }

        if (s.length() != 0) {
            if (s.charAt(0) == '[') {
                jArr = new JSONArray(s);

                p.destroy();

                return jArr;
            } else {
                LoggerUtils.logForBasilisk(logPrefix, "Cannot connect to github to fetch tags or curl " +
                        "command incorrect causing JSON parsing issue", 4);
                return new JSONArray("[]");
            }
        } else {
            LoggerUtils.logForBasilisk(logPrefix, "Cannot connect to github to fetch tags or curl " +
                    "command incorrect causing JSON parsing issue", 4);
            return new JSONArray("[]");
        }
    }

    /**
     * This method returns the information of the already benchmarked version in the git hook
     * for the given triple store name.
     *
     * @param tripleStoreName Triple store name.
     * @return List of all the version already tried benchmarking process for the given triple store name.
     */
    public ArrayList<String> getBenchmarkedDetails(String tripleStoreName) {
        try {
            for (int i = 0; i < this.gitHookBenchmarkedFileData.length(); i++) {
                JSONObject currentBenchmarkedFileObject = this.gitHookBenchmarkedFileData.getJSONObject(i);
                if (currentBenchmarkedFileObject.has(tripleStoreName)) {
                    ArrayList<String> list = new ArrayList<>();
                    JSONArray jsonArray = (JSONArray) currentBenchmarkedFileObject.get(tripleStoreName);
                    for (int i1 = 0; i1 < jsonArray.length(); i1++) {
                        list.add(jsonArray.get(i1).toString());
                    }
                    return list;
                }
            }
        } catch (JSONException e) {
            LoggerUtils.logForBasilisk(logPrefix, "Could be a JSON file parsing issue", 4);
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * This method runs for the Tentris and fuesiki triple stores, which downloads the git repo from the git hub
     * and runs the benchmarking process on the respective triple store.
     *
     * @return Exit code.
     */
    public BasiliskRunStatisticsData forEachStore(BasiliskRunStatisticsData basiliskRunStatisticsData) throws InterruptedException {
        this.basiliskRunStatisticsData = basiliskRunStatisticsData;
        try {
            this.gitHookBenchmarkedFileData = YamlUtils.getGitBenchmarkAttempted();

            JSONArray metadataFileArray = YamlUtils.getGitMetaData();

            for (int i = 0; i < metadataFileArray.length(); i++) {
                JSONObject jsonObj = metadataFileArray.getJSONObject(i);

                this.currentTripleStore = (String) jsonObj.get("name");
                this.currentPortNum = (String) jsonObj.get("port");
                this.currentDatasetFilePath = (String) jsonObj.get("dataset");
                this.currentQueriesFilePath = (String) jsonObj.get("queriesFilePath");

                LoggerUtils.logForBasilisk(logPrefix, "Currently checking for this triple store: " + this.currentTripleStore, 1);

                //Get the list of all the tag of the current triple store that is already benchmarked in Git hook.
                this.alreadyBenchmarkedVersionsList = this.getBenchmarkedDetails(this.currentTripleStore);

                //Get the list of all tags from the git hub for the current triple store.
                JSONArray githubJsonArray = this.getGithubTags((String) jsonObj.get("command"));

                /*
                Check all the tags from the git hub and run the benchmark process
                for the tags that is not yet benchmarked.
                 */
                this.checkAndRunCPB(githubJsonArray, this.alreadyBenchmarkedVersionsList);
            }
        } catch (JSONException e) {
            LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while parsing the JSON object.", 4);
            e.printStackTrace();
        }

        LoggerUtils.logForBasilisk(logPrefix, "Basilisk completed benchmark process  on Git hook.", 1);
        return basiliskRunStatisticsData;
    }
}
