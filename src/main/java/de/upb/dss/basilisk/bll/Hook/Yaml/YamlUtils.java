package de.upb.dss.basilisk.bll.Hook.Yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

public class YamlUtils {
    private static Map<String, Object> getYamlData(InputStream ymlFileStream) {
        return new Yaml().load(ymlFileStream);
    }

    public static JSONArray getDockerBenchmarkAttempted() {

        try {
            InputStream inputStream = new FileInputStream(
                    new File("continuousBM/DockerBenchmarkedAttempted.yml")
            );

            JSONObject dockerBenchmarkedAttempted = new JSONObject(getYamlData(inputStream));
            return dockerBenchmarkedAttempted.getJSONArray("DockerBenchmarkedAttempted");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new JSONArray("[]");
    }

    public static JSONArray getGitBenchmarkAttempted() {

        try {
            InputStream inputStream = new FileInputStream(
                    new File("continuousBM/GitBenchmarkedAttempted.yml")
            );

            JSONObject dockerBenchmarkedAttempted = new JSONObject(getYamlData(inputStream));
            return dockerBenchmarkedAttempted.getJSONArray("GitBenchmarkedAttempted");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new JSONArray("[]");
    }

    public static JSONArray getGitMetaData() {

        try {
            InputStream inputStream = new FileInputStream(
                    new File("continuousBM/GitMetaData.yml")
            );

            JSONObject dockerBenchmarkedAttempted = new JSONObject(getYamlData(inputStream));
            return dockerBenchmarkedAttempted.getJSONArray("GitMetaData");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new JSONArray("[]");
    }

    public static JSONArray getDockerMetaData() {

        try {
            InputStream inputStream = new FileInputStream(
                    new File("continuousBM/DockerMetaData.yml")
            );

            JSONObject dockerBenchmarkedAttempted = new JSONObject(getYamlData(inputStream));
            return dockerBenchmarkedAttempted.getJSONArray("DockerMetaData");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new JSONArray("[]");
    }

    private static JSONArray updateYamlFile(JSONArray oldFileData, String newTag, String tripleStoreName, int hook) {
        JSONArray oldData = oldFileData;

        for (int i = 0; i < oldData.length(); i++) {
            JSONObject jsonObj = oldData.getJSONObject(i);

            if (jsonObj.has(tripleStoreName)) {
                JSONArray arr = jsonObj.getJSONArray(tripleStoreName);
                arr.put(arr.length(),newTag);
                jsonObj.put(tripleStoreName, arr);
            }
            oldData.put(i, jsonObj);
        }


        JSONObject newData = new JSONObject();

        if(hook == 1)
            newData.put("GitBenchmarkedAttempted",oldData);
        else
            newData.put("DockerBenchmarkedAttempted",oldData);

        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String,Object> myMap = mapper.readValue(newData.toString(),
                    new TypeReference<Map<String, Object>>() {
                    });

            DumperOptions options = new DumperOptions();
            options.setIndent(2);
            options.setPrettyFlow(true);
            // Fix below - additional configuration
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml output = new Yaml(options);

            FileWriter fw = null;
            if(hook == 1)
                fw = new FileWriter("continuousBM/GitBenchmarkedAttempted.yml");
            else
                fw = new FileWriter("continuousBM/DockerBenchmarkedAttempted.yml");

            output.dump(myMap,fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return oldData;
    }

    public static JSONArray addTagToDockerBenchmarkedAttempted(JSONArray oldData, String newTag, String tripleStoreName) {
        return updateYamlFile(oldData, newTag, tripleStoreName, 2);
    }

    public static JSONArray addVersionToGitBenchmarkedAttempted(JSONArray oldData, String newTag, String tripleStoreName) {
        return updateYamlFile(oldData, newTag, tripleStoreName, 1);
    }
}
