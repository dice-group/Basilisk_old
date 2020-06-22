package de.upb.dss.basilisk.bll.applicationProperties;

import de.upb.dss.basilisk.Basilisk;

import java.util.Properties;

/**
 * This is a utility class to get the information from the application.properties file.
 *
 * @author Ranjith Krishnamurthy
 */
public class ApplicationPropertiesUtils {
    private final String logFilePath;
    private final String configPath;
    private final String testDatasetPath;
    private final String continuousBmPath;
    private final String iguanaPath;
    private final String bmWorkSpace;
    private final String basiliskVersion;
    private final String dockerFile;
    private final String gitMetaDataFileName;
    private final String gitBenchmarkedFileName;
    private final String dockerMetadataFileName;
    private final String dockerBenchmarkedFileName;
    private final String logDirectory;
    private final String resultStoringFusekiEndPoint;
    private final String basiliskEndPoint;
    private final String iguanaJarLink;
    private final String securityLogFilePath;

    Properties appProps;

    /**
     * Getter method for the security Log file path.
     *
     * @return Log file path.
     */
    public String getSecurityLogFilePath() {
        return securityLogFilePath;
    }

    /**
     * Getter method for Iguana jar file downloadable link.
     *
     * @return Iguana jar file downloadable link.
     */
    public String getIguanaJarLink() {
        return iguanaJarLink;
    }

    /**
     * Getter method for Basilisk server endpoint.
     *
     * @return Basilisk server endpoint.
     */
    public String getBasiliskEndPoint() {
        return basiliskEndPoint;
    }

    /**
     * Getter method for Fuseki server endpoint to store the results of the benchmark.
     *
     * @return Fuseki server endpoint to store the results of the benchmark.
     */
    public String getResultStoringFusekiEndPoint() {
        return resultStoringFusekiEndPoint;
    }

    /**
     * Getter method for log directory.
     *
     * @return Log directory.
     */
    public String getLogDirectory() {
        return logDirectory;
    }

    /**
     * Getter method for benchmarking process workspace directory.
     *
     * @return Benchmarking process workspace directory.
     */
    public String getBmWorkSpace() {
        return bmWorkSpace;
    }

    /**
     * Getter method for the Iguana directory path.
     *
     * @return Iguana directory.
     */
    public String getIguanaPath() {
        return iguanaPath;
    }

    /**
     * Getter method for the Log file path.
     *
     * @return Log file path.
     */
    public String getLogFilePath() {
        return logFilePath;
    }

    /**
     * Getter method for the iguana configuration path.
     *
     * @return Iguana configuration path.
     */
    public String getConfigPath() {
        return configPath;
    }

    /**
     * Getter method for test dataset path.
     *
     * @return Test dataset path.
     */
    public String getTestDatasetPath() {
        return testDatasetPath;
    }

    /**
     * Getter method for continuous benchmarking process directory path.
     *
     * @return Continuous benchmarking process directory path.
     */
    public String getContinuousBmPath() {
        return continuousBmPath;
    }

    /**
     * Getter method for Basilisk application version number.
     *
     * @return Basilisk version number.
     */
    public String getBasiliskVersion() {
        return basiliskVersion;
    }

    /**
     * Getter method for Docker file path.
     *
     * @return Docker file path.
     */
    public String getDockerFile() {
        return dockerFile;
    }

    /**
     * Getter method for Metadata file name for git hook.
     *
     * @return Metadata file name for git hook.
     */
    public String getGitMetaDataFileName() {
        return gitMetaDataFileName;
    }

    /**
     * Getter method for already benchmarked data for git hook.
     *
     * @return Already benchmarked data for git hook.
     */
    public String getGitBenchmarkedFileName() {
        return gitBenchmarkedFileName;
    }

    /**
     * Getter method for Metadata file name for docker hook.
     *
     * @return Metadata file name for docker hook.
     */
    public String getDockerMetadataFileName() {
        return dockerMetadataFileName;
    }

    /**
     * Getter method for already benchmarked data for docker hook.
     *
     * @return Already benchmarked data for docker hook.
     */
    public String getDockerBenchmarkedFileName() {
        return dockerBenchmarkedFileName;
    }

    /**
     * This constructor reads the application properties and initialize the ApplicationPropertiesUtils object.
     */
    public ApplicationPropertiesUtils() {
        appProps = Basilisk.applicationProperties;
        basiliskVersion = appProps.getProperty("version");
        dockerFile = appProps.getProperty("dockerFile");
        bmWorkSpace = appProps.getProperty("bmWorkSpace");
        iguanaPath = appProps.getProperty("iguanaPath");
        testDatasetPath = appProps.getProperty("testDatasetPath");
        logFilePath = appProps.getProperty("logFilePath");
        configPath = appProps.getProperty("configPath");
        continuousBmPath = appProps.getProperty("continuousBmPath");
        gitMetaDataFileName = appProps.getProperty("gitMetadataFileName");
        gitBenchmarkedFileName = appProps.getProperty("gitBenchmarkedFileName");
        dockerMetadataFileName = appProps.getProperty("dockerMetadataFileName");
        dockerBenchmarkedFileName = appProps.getProperty("dockerBenchmarkedFileName");
        logDirectory = appProps.getProperty("logDirectory");
        resultStoringFusekiEndPoint = appProps.getProperty("resultStoringFusekiEndPoint");
        basiliskEndPoint = appProps.getProperty("basiliskEndPoint");
        iguanaJarLink = appProps.getProperty("iguanaJarLink");
        securityLogFilePath = appProps.getProperty("securityLogFilePath");
    }
}
