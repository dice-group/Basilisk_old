package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the utils for Freemarker java template engine to create an actual file from the template.
 *
 * @author Ranjith Krishnamurthy
 * @author Rahul sethi
 */
public class FreeMarkerTemplateEngineUtils {
    private static final String logPrefix = "FreeMarker Utils";

    /**
     * This method creates the benchmark configuration for the Iguana for the currently running
     * triple store.
     *
     * @return Exit code.
     */
    public static int setIguanaConfigFile(String repoName, String tag, String port,
                                          String queryFile, String configPath) {

        //Get the freemarker configuration
        LoggerUtils.logForBasilisk(logPrefix, "Creating the configuration file for the iguana.", 1);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

        try {
            //Set up the free marker configuration, with template loading class and path.
            cfg.setClassForTemplateLoading(FreeMarkerTemplateEngineUtils.class, "/");
            cfg.setDefaultEncoding("UTF-8");

            //Get the Iguana configuration template.
            Template template = cfg.getTemplate("iguanaConfig.ftl");

            String connName = repoName + "$" + tag;
            String datasetName = repoName + "$" + tag + "$DB";

            //Port number and query file to insert into benchmark template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("port", port);
            templateData.put("queryFile", queryFile);
            templateData.put("connName", connName);
            templateData.put("datasetName", datasetName);

            //Write port number and query file in to template
            StringWriter out = new StringWriter();
            template.process(templateData, out);
            out.flush();

            //Dump that configuration into a configuration file called benchmark.config

            File configFile = new File(configPath);

            if (configFile.exists()) {
                if (!configFile.delete()) {
                    LoggerUtils.logForBasilisk(logPrefix, "Could not generate config file returning....", 4);
                    return -1;
                }
            }

            if (configFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(configPath);
                fos.write(out.toString().getBytes());
                fos.flush();
                fos.close();
                LoggerUtils.logForBasilisk(logPrefix, configPath + " file Created", 1);
            } else {
                LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while creating the file " + configPath, 1);
                return -1;
            }
        } catch (Exception e) {
            LoggerUtils.logForBasilisk(logPrefix, "Something went wrong", 4);
            System.err.println(e);
            return -1;
        }
        return 0;
    }

    /**
     * This method creates the script to load test data into virtuoso tripe store.
     *
     * @return Exit code.
     */
    public static int setLoadingScript(String testDataset, String containerId, String port) {

        //Get the freemarker configuration
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
        LoggerUtils.logForBasilisk(logPrefix, "Creating the loadTestData.sh file for loading testdata " +
                "into virtuoso.", 1);

        try {
            //Set up the free marker configuration, with template loading class and path.
            cfg.setClassForTemplateLoading(FreeMarkerTemplateEngineUtils.class, "/");
            cfg.setDefaultEncoding("UTF-8");

            //Get the Iguana configuration template.
            Template template = cfg.getTemplate("loadTestDataIntoVirtuoso.ftl");

            //Port number and query file to insert into benchmark template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("port", port);
            templateData.put("testDataset", testDataset);
            templateData.put("containerId", containerId);

            //Write port number and query file in to template
            StringWriter out = new StringWriter();
            template.process(templateData, out);
            out.flush();

            //Dump that configuration into a configuration file called benchmark.config

            File scriptFile = new File("loadTestData.sh");

            if (scriptFile.exists()) {
                if (!scriptFile.delete()) {
                    LoggerUtils.logForBasilisk(logPrefix, "Could not generate the script file returning....", 4);
                    return -1;
                }
            }

            if (scriptFile.createNewFile()) {
                scriptFile.setExecutable(true, false);
                FileOutputStream fos = new FileOutputStream(scriptFile);
                fos.write(out.toString().getBytes());
                fos.flush();
                fos.close();
                LoggerUtils.logForBasilisk(logPrefix, scriptFile + " file Created", 1);
            } else {
                LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while creating the file " + scriptFile, 1);
                return -1;
            }
        } catch (Exception e) {
            LoggerUtils.logForBasilisk(logPrefix, "Something went wrong", 4);
            System.err.println(e);
            return -1;
        }
        return 0;
    }

    /**
     * This method creates the Dockerfile for fuseki triple store.
     *
     * @return Exit code.
     */
    public static int setDockerfileForFuseki(String port) {

        //Get the freemarker configuration
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
        LoggerUtils.logForBasilisk(logPrefix, "Creating the Dockerfile for fuseki triple store", 1);

        try {
            //Set up the free marker configuration, with template loading class and path.
            cfg.setClassForTemplateLoading(FreeMarkerTemplateEngineUtils.class, "/");
            cfg.setDefaultEncoding("UTF-8");

            //Get the Iguana configuration template.
            Template template = cfg.getTemplate("dockerfileForFuseki.ftl");

            //Port number and query file to insert into benchmark template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("port", port);

            //Write port number and query file in to template
            StringWriter out = new StringWriter();
            template.process(templateData, out);
            out.flush();

            //Dump that configuration into a configuration file called benchmark.config

            File dockerfile = new File(new ApplicationPropertiesUtils().getBmWorkSpace() + "Dockerfile");

            if (dockerfile.exists()) {
                if (!dockerfile.delete()) {
                    LoggerUtils.logForBasilisk(logPrefix, "Could not generate the Dockerfile file returning....", 4);
                    return -1;
                }
            }

            if (dockerfile.createNewFile()) {
                dockerfile.setExecutable(true, false);
                FileOutputStream fos = new FileOutputStream(dockerfile);
                fos.write(out.toString().getBytes());
                fos.flush();
                fos.close();
                LoggerUtils.logForBasilisk(logPrefix, dockerfile + " file Created", 1);
            } else {
                LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while creating the file " + dockerfile, 1);
                return -1;
            }
        } catch (Exception e) {
            LoggerUtils.logForBasilisk(logPrefix, "Something went wrong", 4);
            System.err.println(e);
            return -1;
        }
        return 0;
    }
}
