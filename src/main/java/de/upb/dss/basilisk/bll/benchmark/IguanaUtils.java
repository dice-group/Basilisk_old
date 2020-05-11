package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class IguanaUtils {
    private static Logger logger;

    /**
     * This method runs the Iguana for the current triple store.
     *
     * @return Status code.
     * @throws Exception If fails in the process of benchmarking.
     */
    public static int runIguana(String repoName, String tag, String port,
                                   String queryFile) throws Exception {
        ApplicationPropertiesUtils myAppProperties = new ApplicationPropertiesUtils();

        File iguanaPath = new File(myAppProperties.getIguanaPath());
        String logFilePath = myAppProperties.getLogFilePath();
        String configPath = myAppProperties.getConfigPath();

        logger = new LoggerUtils().getLogger(logFilePath, "IguanaUtils");
        String s = "";
        String log = "";
        String err = "";
        String cmd = "";

        //Set the Iguana configuration file respective to triple store before running it.
        setIguanaConfigFile(repoName,tag,port,queryFile,configPath);

        //Command to run the iguana script.
        cmd = "./start-iguana.sh benchmark.config";

        //Run the Iguana script
        Process p = Runtime.getRuntime().exec(cmd, null, iguanaPath);

        //Track the output and error
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        System.out.println("Output of the command is :\n");
        logger.info("Output of the command is :\n");
        while ((s = stdInput.readLine()) != null) {
            log = log + "\n" + s;
            System.out.println(s);
        }

        logger.info(log);

        System.out.println("Error/Warning of the command :\n");
        logger.info("Error/Warning of the command :\n");
        while ((s = stdError.readLine()) != null) {
            err = err + "\n" + s;
            System.err.println(s);
        }

        //Wait for process to complete.
        p.waitFor();
        int exitCode = p.exitValue();

        if (exitCode != 0) {
            System.out.println("Something went wrong while while running iguana");
            System.out.println("Exit code = " + exitCode);
            System.out.println("Error message = \n" + err);
            return exitCode;
        }
        return 0;
    }

    /**
     * This method creates the benchmark configuration for the Iguana for the currently running
     * triple store.
     *
     * @return Status code.
     */
    private static int setIguanaConfigFile(String repoName, String tag, String port,
                                           String queryFile, String configPath) {

        //Get the freemarker configuration
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

        try {
            //Set up the free marker configuration, with template loading class and path.
            cfg.setClassForTemplateLoading(BenchmarkForGitHook.class, "/");
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
            String fileSeparator = System.getProperty("file.separator");

            System.out.println("Config is : " + configPath);
            File configFile = new File(configPath);

            if (configFile.exists()) {
                if (!configFile.delete()) {
                    System.err.println("Could not gnerate config file");
                    return -1;
                }
            }

            if (configFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(configPath);
                fos.write(out.toString().getBytes());
                fos.flush();
                fos.close();
                System.out.println(configPath + " File Created");
                logger.info(configPath + " File Created");
            } else {
                System.out.println("Something went wrong while creating the file " + configPath);
                logger.info("Something went wrong while creating the file " + configPath);
                return -1;
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return 0;
    }
}
