package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class IguanaUtils {
    private static final String logPrefix = "IguanaUtils";

    /**
     * This method runs the Iguana for the current triple store.
     *
     * @return Exit code.
     */
    /**
     * This method runs the Iguana for the current triple store.
     *
     * @param repoName  Current repository name of the Docker image.
     * @param tag       Tag of the Docker image.
     * @param port      Port number
     * @param queryFile Query File name.
     * @return Exit code.
     * @throws InterruptedException If Basilisk is interrupted.
     */
    public static int runIguana(String repoName, String tag, String port,
                                String queryFile) throws InterruptedException {
        ApplicationPropertiesUtils myAppProperties = new ApplicationPropertiesUtils();

        File iguanaPath = new File(myAppProperties.getIguanaPath());
        String logFilePath = myAppProperties.getLogFilePath();
        String configPath = myAppProperties.getConfigPath();

        String s;
        String log = "";
        String err = "";
        String cmd = "";

        //Set the Iguana configuration file respective to triple store before running it.
        FreeMarkerTemplateEngineUtils.setIguanaConfigFile(repoName, tag, port, queryFile, configPath);

        //Command to run the iguana script.
        cmd = "./start-iguana.sh benchmark.config";

        //Run the Iguana script
        Process p = null;
        try {
            LoggerUtils.logForBasilisk(logPrefix, "Running the Iguana script", 1);
            p = Runtime.getRuntime().exec(cmd, null, iguanaPath);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            while ((s = stdInput.readLine()) != null) {
                if (s.endsWith("failed: Connection refused (Connection refused)")) {
                    LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while running iguana, triple store connection is refused" +
                            " Terminating the iguana.", 4);
                    TimeUnit.SECONDS.sleep(15);
                    p.destroyForcibly();
                    p.wait();
                    return p.exitValue();
                }
//                System.out.println("Benchmarking = " + repoName + ":" + tag + " ------> "  + s);
            }

            while ((s = stdError.readLine()) != null) {
                err = err + "\n" + s;
            }

            LoggerUtils.logForBasilisk(logPrefix, "Error/Warning of the iguana script\n" + err, 4);

            p.waitFor();
        } catch (IOException e) {
            LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while running iguana script for: " + repoName + ":" + tag, 4);
            e.printStackTrace();
        }

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
