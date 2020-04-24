package de.upb.dss.basilisk.controllers;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import de.upb.dss.basilisk.bll.dockerHook.ContinuousDeliveryDockerHook;
import de.upb.dss.basilisk.bll.gitHook.ContinuousDeliveryGitHook;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

/**
 * This is the Basilisk Controller.
 */
@RestController
public class BasiliskAPIController {
    /**
     * Index root of the Basilisk API.
     *
     * @return Returns the String Basilisk is running...
     */
    @RequestMapping("/")
    public String index() {
        return "Basilisk is running...";
    }

    /**
     * This runs the benchmark based on the parameter hook
     *
     * @param hook Integer parameter to indicate where to run the benchmark.
     *             1 means run on git hook 2 means run on docker hook.
     * @return Returns the status of the benchmark as a string.
     */
    @RequestMapping("/runbenchmark")
    public String runBenchmark(@RequestParam(defaultValue = "2") int hook) {
        ApplicationPropertiesUtils myAppUtils = new ApplicationPropertiesUtils();

        int exitcode = -1;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        if (hook == 2) {
            try {
                exitcode = new ContinuousDeliveryDockerHook(
                        myAppUtils.getContinuousBmPath(),
                        myAppUtils.getDockerMetadataFileName(),
                        myAppUtils.getDockerBenchmarkedFileName(),
                        myAppUtils.getContinuousErrorLogFileName(),
                        myAppUtils.getContinuousBmPath())
                        .forEachStore();
            } catch (InterruptedException ex) {
                ex.printStackTrace(pw);
                return sw.toString();
            }
            return "Done";
        } else if (hook == 1) {
            try {
                exitcode = new ContinuousDeliveryGitHook(
                        myAppUtils.getContinuousBmPath(),
                        myAppUtils.getGitMetaDataFileName(),
                        myAppUtils.getGitBenchmarkedFileName(),
                        myAppUtils.getContinuousErrorLogFileName(),
                        myAppUtils.getBmWorkSpace())
                        .forEachStore();
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace(pw);
                return sw.toString();
            }
        } else {
            return "Invalid value to the hook parameter. Please look at the below values to the hook parameter.\n" +
                    "1 means run CPB for github hook\n" +
                    "2 means run CPB for docker hub hook\n";
        }

        try {
            File file = new File("/home/dss/continuousBM/log/start-benchmarking.err");
            BufferedReader br = new BufferedReader(new FileReader(file));

            String message = " ";
            String msg;
            while ((msg = br.readLine()) != null) {
                message += msg + "\n";
            }

            return message;
        } catch (Exception e) {
            if (exitcode == 0) {
                return "Ran successfully\n";
            } else {
                return "Something went wrong.\n";
            }
        }
    }
}
