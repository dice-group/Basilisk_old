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
        FreeMarkerTemplateEngineUtils.setIguanaConfigFile(repoName,tag,port,queryFile,configPath);

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
}
