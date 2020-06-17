package de.upb.dss.basilisk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;
import de.upb.dss.basilisk.bll.gitHook.Extraction;
import de.upb.dss.basilisk.security.BasiliskAdminInitializer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class sets up the Basilisk for the first time with the necessary file structures and required files.
 *
 * @author Ranjith Krishnamurthy
 */
public class InitialSetup {
    private static ApplicationPropertiesUtils applicationPropertiesUtils = null;

    /**
     * This method creates a directory.
     *
     * @param dir File object to Directory
     */
    private static void createDir(File dir) {
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            if (!dir.isDirectory()) {
                dir.delete();
                dir.mkdir();
            }
        }
    }

    /**
     * This method deletes the given file.
     *
     * @param file File
     */
    private static void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * This method downloads the Iguana required for the Basilisk.
     *
     * @throws IOException If fails to download Iguana zip file.
     */
    private static void downloadIguana() throws IOException {
        deleteFile(new File(applicationPropertiesUtils.getIguanaPath() + "iguana.zip"));

        try (BufferedInputStream in = new BufferedInputStream(new URL(applicationPropertiesUtils.getIguanaJarLink()).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(
                     applicationPropertiesUtils.getIguanaPath() + "iguana.zip")) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            LoggerUtils.logForBasilisk("Initial Setup", "Something went wrong while downloading Iguana jar file", 100);
            throw e;
        }
    }

    /**
     * This method sets the required permision to run the jar and sh files.
     *
     * @throws IOException If fails to change the permission of a file.
     */
    private static void setPermisionToExec() throws IOException {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        String iguanaPath = new ApplicationPropertiesUtils().getIguanaPath();

        File f = new File(iguanaPath);

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("(.*.jar|.*.sh)");
            }
        };


        for (File file : f.listFiles(filter)) {
            Files.setPosixFilePermissions(file.toPath(), perms);
        }
    }

    /**
     * This method creates the initial configuration file for the Basilisk. This method creates below 4 files.
     * 1. DockerMetaData.yml
     * 2. DockerBenchmarkedAttempted.yml
     * 3. GitMetaData.yml
     * 4. GitBenchmarkedAttempted.yml
     *
     * @param initData Initial data to be written to the file.
     * @param fileName File name. One of the 4 file name mentioned above.
     */
    private static void createBasiliskConfigFile(String initData, String fileName) {

        if (new File(fileName).exists())
            return;

        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> myMap = mapper.readValue(initData.toString(),
                    new TypeReference<Map<String, Object>>() {
                    });

            DumperOptions options = new DumperOptions();
            options.setIndent(2);
            options.setPrettyFlow(true);
            // Fix below - additional configuration
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml output = new Yaml(options);

            FileWriter fw = null;
            fw = new FileWriter(
                    fileName
            );

            output.dump(myMap, fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method creates the 4 necessary initial configuration file for Basilisk.
     * 1. DockerMetaData.yml
     * 2. DockerBenchmarkedAttempted.yml
     * 3. GitMetaData.yml
     * 4. GitBenchmarkedAttempted.yml
     */
    private static void setUpBasiliskConfig() {
        String dockerBenchmarkedAttempted = "{\n" +
                "  \"DockerBenchmarkedAttempted\": [\n" +
                "    {\n" +
                "      \"tentris\": [\n" +
                "        \"initialTempKickOffData\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"virtuoso\": [\n" +
                "        \"initialTempKickOffData\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        createBasiliskConfigFile(dockerBenchmarkedAttempted, applicationPropertiesUtils.getDockerBenchmarkedFileName());

        String dockerMetaData = "{\n" +
                "  \"DockerMetaData\": [\n" +
                "    {\n" +
                "      \"name\": \"tentris\",\n" +
                "      \"repositoryName\": \"dicegroup/tentris_server\",\n" +
                "      \"command\": \"curl https://registry.hub.docker.com/v2/repositories/dicegroup/tentris_server/tags\",\n" +
                "      \"port\": \"9080\",\n" +
                "      \"dataset\": \"sp2b.nt\",\n" +
                "      \"queriesFilePath\": \"sp2b.txt\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"virtuoso\",\n" +
                "      \"repositoryName\": \"openlink/virtuoso-opensource-7\",\n" +
                "      \"command\": \"curl https://registry.hub.docker.com/v2/repositories/openlink/virtuoso-opensource-7/tags\",\n" +
                "      \"port\": \"8890\",\n" +
                "      \"dataset\": \"sp2b.nt\",\n" +
                "      \"queriesFilePath\": \"sp2b.txt\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";


        createBasiliskConfigFile(dockerMetaData, applicationPropertiesUtils.getDockerMetadataFileName());

        String gitMetaData = "{\n" +
                "  \"GitMetaData\": [\n" +
                "    {\n" +
                "      \"name\": \"tentris\",\n" +
                "      \"command\": \"curl https://api.github.com/repos/dice-group/tentris/tags\",\n" +
                "      \"port\": \"9080\",\n" +
                "      \"dataset\": \"sp2b.nt\",\n" +
                "      \"queriesFilePath\": \"sp2b.txt\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"fuseki\",\n" +
                "      \"command\": \"curl https://api.github.com/repos/apache/jena/tags\",\n" +
                "      \"port\": \"9999\",\n" +
                "      \"dataset\": \"sp2b.nt\",\n" +
                "      \"queriesFilePath\": \"sp2b.txt\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        createBasiliskConfigFile(gitMetaData, applicationPropertiesUtils.getGitMetaDataFileName());

        String gitBenchmarkedAttempted = "{\n" +
                "  \"GitBenchmarkedAttempted\": [\n" +
                "    {\n" +
                "      \"tentris\": [\n" +
                "        \"initialTempKickOffData\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"fuseki\": [\n" +
                "        \"initialTempKickOffData\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        createBasiliskConfigFile(gitBenchmarkedAttempted, applicationPropertiesUtils.getGitBenchmarkedFileName());
    }

    /**
     * This method sets up the Basilisk for the first time with the necessary file structures and required files.
     *
     * @throws IOException If fails to download zip file or fails to change the file permission.
     */
    public static void setup(String[] args) throws IOException {
        applicationPropertiesUtils = new ApplicationPropertiesUtils();

        File results = new File("results");
        File logs = new File(applicationPropertiesUtils.getLogDirectory());
        File continuousBM = new File(applicationPropertiesUtils.getContinuousBmPath());
        File iguana = new File(applicationPropertiesUtils.getIguanaPath());
        File bmWorkSpace = new File(applicationPropertiesUtils.getBmWorkSpace());
        File testDataSet = new File(applicationPropertiesUtils.getTestDatasetPath());

        System.out.println("\t\t* Creating results directory");
        createDir(results);
        System.out.println("\t\t* Creating logs directory");
        createDir(logs);
        System.out.println("\t\t* Creating continuousBM directory");
        createDir(continuousBM);
        System.out.println("\t\t* Creating iguana directory");
        createDir(iguana);
        System.out.println("\t\t* Creating bmWorkSpace directory");
        createDir(bmWorkSpace);
        System.out.println("\t\t* Creating testDataSet directory");
        createDir(testDataSet);

        System.out.println("\t\t* Downloading Iguana");
        downloadIguana();
        new Extraction().unzipGeneric(applicationPropertiesUtils.getIguanaPath() + "iguana.zip",
                applicationPropertiesUtils.getIguanaPath());
        deleteFile(new File(applicationPropertiesUtils.getIguanaPath() + "iguana.zip"));

        System.out.println("\t\t* Setting permission to executables");
        setPermisionToExec();

        System.out.println("\t\t* Setting up the initial Basilisk configuration files");
        setUpBasiliskConfig();

        String adminUserName = null;
        String adminPass = null;

        if ("--admin-pass".equals(args[0]))
            adminPass = args[1];
        else if ("--admin-user-name".equals(args[0]))
            adminUserName = args[1];

        if ("--admin-pass".equals(args[2]))
            adminPass = args[3];
        else if ("--admin-user-name".equals(args[2]))
            adminUserName = args[3];

        System.out.println("\t\t* Setting up the admin account");

        BasiliskAdminInitializer.initializeBasiliskAdmin(adminUserName, adminPass);
        System.out.println("\t\t\t* Added admin account.");
    }
}
