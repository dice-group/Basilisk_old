package de.upb.dss.basilisk.bll;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContinuousDelivery {

	private JSONArray benchmarkedFileData;
	private String currentBenchmarkedVersion, currentStore;
	private ArrayList<String> alreadyBenchmarkedVersionsList;
	private String continuousBmPath;
	private String metadataFileName;
	private String benchmarkedFileName;
	private String errorLogFileName;
	private String bmWorkspacePath;
	private String currentPortNum;
	private String currentDatasetFilePath;
	private String currentQueriesFilePath;

	public ContinuousDelivery(String continuousBmPath, String metadataFileName, String benchmarkedFileName,
			String errorLogFileName, String bmWorkspacePath) {
		super();
		this.continuousBmPath = continuousBmPath;
		this.metadataFileName = metadataFileName;
		this.benchmarkedFileName = benchmarkedFileName;
		this.errorLogFileName = errorLogFileName;
		this.bmWorkspacePath = bmWorkspacePath;
	}

	public void updateErrorLog(String GeneralDesc, String ExceptionMessage) throws IOException {
		try {
			FileWriter er = new FileWriter(this.continuousBmPath + this.errorLogFileName, true);
			er.write(Calendar.getInstance().getTime() + "~" + this.currentStore + "~" + GeneralDesc + "~"
					+ ExceptionMessage + "~" + Calendar.getInstance().getTimeInMillis() + "\n");
			er.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delRepository() throws IOException {
		File file = new File(this.continuousBmPath + this.currentStore + ".zip");
		file.delete();
		if (!new File(this.bmWorkspacePath).isDirectory()) {
			// FileUtils.cleanDirectory(new File(this.bmWorkspacePath));
			FileUtils.deleteDirectory(new File(this.bmWorkspacePath));
			new File(this.bmWorkspacePath).mkdir();
		}
	}

	public void updateVersionList() throws JSONException, IOException {
		this.alreadyBenchmarkedVersionsList.add(this.currentBenchmarkedVersion);
		try {
			for (int i = 0; i < this.benchmarkedFileData.length(); i++) {
				JSONObject jsonObj = this.benchmarkedFileData.getJSONObject(i);
				if (jsonObj.has(this.currentStore)) {
					jsonObj.put(this.currentStore, this.alreadyBenchmarkedVersionsList);
					this.benchmarkedFileData.put(i, jsonObj);
				}
			}

			FileWriter f = new FileWriter(this.continuousBmPath + this.benchmarkedFileName, false);
			f.write(this.benchmarkedFileData.toString(2));
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

	public void resolveDependencies() throws IOException {
		if (this.currentStore.equalsIgnoreCase("Virtuoso")) {
			if (new File(this.continuousBmPath + "Dockerfile_Virtuoso").exists()) {
				FileUtils.copyFile(new File(this.continuousBmPath + "Dockerfile_Virtuoso"),
						new File(this.bmWorkspacePath + "Dockerfile"));
			}
		}
	}

	public void benchmark() throws IOException, InterruptedException {
		File zipFile = new File(this.continuousBmPath + this.currentStore + ".zip");
		String tempStore = this.currentStore;
		if (zipFile.exists()) {
			Extraction obj = new Extraction();
			if (this.currentStore.equalsIgnoreCase("Fuseki")) {
				obj.unzipJena(this.continuousBmPath + tempStore + ".zip", this.bmWorkspacePath);
			} else {
				obj.unzipGeneric(this.continuousBmPath + tempStore + ".zip", this.bmWorkspacePath);
			}
			
			this.resolveDependencies();
			Benchmark.runBenchmark(this.currentPortNum, this.currentStore, this.currentDatasetFilePath,
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

	public int downloadRepo(String url) throws IOException, InterruptedException {
		System.out.println("Downloading of the release(.zip) will start ..");
		try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
				FileOutputStream fileOutputStream = new FileOutputStream(
						this.continuousBmPath + this.currentStore + ".zip")) {
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

	public String check(JSONArray githubJsonArray, ArrayList<String> benchmarkedVersionsList)
			throws JSONException, IOException, InterruptedException {
		for (int i = 0; i < githubJsonArray.length(); i++) {
			try {
				JSONObject versionObj = githubJsonArray.getJSONObject(i);
				if (!benchmarkedVersionsList.contains(versionObj.get("name"))) {
					this.delRepository();
					this.currentBenchmarkedVersion = (String) versionObj.get("name");
					System.out.println("Benchmarking will run for version: "+ this.currentBenchmarkedVersion);
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
		return null;
	}

	public JSONArray getGithubTags(String comm) throws IOException, JSONException {
		ProcessBuilder pb = new ProcessBuilder(comm.split(" "));
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

	public ArrayList<String> getBenchmarkedDetails(String name) throws JSONException, IOException {
		try {

			for (int i = 0; i < this.benchmarkedFileData.length(); i++) {
				JSONObject currentBenchmarkedFileObject = this.benchmarkedFileData.getJSONObject(i);
				if (currentBenchmarkedFileObject.has(name)) {
					ArrayList<String> list = new ArrayList<String>();
					JSONArray jsonArray = (JSONArray) currentBenchmarkedFileObject.get(name);
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

	public int forEachStore() throws IOException, InterruptedException {

		try {
			String metadataFile = new String(
					Files.readAllBytes(Paths.get(this.continuousBmPath + this.metadataFileName)),
					StandardCharsets.UTF_8);
			String benchFile = new String(
					Files.readAllBytes(Paths.get(this.continuousBmPath + this.benchmarkedFileName)),
					StandardCharsets.UTF_8);
			this.benchmarkedFileData = new JSONArray(benchFile);
			JSONArray metadataFileArray = new JSONArray(metadataFile);
			for (int i = 0; i < metadataFileArray.length(); i++) {
				JSONObject jsonObj = metadataFileArray.getJSONObject(i);
				this.currentStore = (String) jsonObj.get("name");
				this.currentPortNum = (String) jsonObj.get("port");
				this.currentDatasetFilePath = (String) jsonObj.get("dataset");
				this.currentQueriesFilePath = (String) jsonObj.get("queriesFilePath");
				System.out.println("Currently checking for this triple store: " + this.currentStore);
				this.alreadyBenchmarkedVersionsList = this.getBenchmarkedDetails(this.currentStore);
				JSONArray githubJsonArray = this.getGithubTags((String) jsonObj.get("command"));
				this.check(githubJsonArray, this.alreadyBenchmarkedVersionsList);
			}
		} catch (IOException e) {
			this.updateErrorLog("Metadata.json or Benchmarked.json file could not found", e.toString());
			e.printStackTrace();
		} catch (JSONException e) {
			this.updateErrorLog("Could be a Metadata.json or Benchmarked.json file parsing issue", e.toString());
			e.printStackTrace();
		}
		System.out.println("One run completed");
		return 0;
	}
}
