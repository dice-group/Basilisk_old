package de.upb.dss.basilisk.controllers;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;
import de.upb.dss.basilisk.bll.dockerHook.ContinuousDeliveryDockerHook;
import de.upb.dss.basilisk.bll.gitHook.ContinuousDeliveryGitHook;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.logging.Logger;

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
        String logFilePath = myAppUtils.getLogFilePath();
        Logger logger = new LoggerUtils().getLogger(logFilePath,"BasiliskMain");

        int exitcode = -1;
        String resp = "";

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        if (hook == 2) {
            try {
                logger.info("Initiated Basilisk benchmark process on Docker hook.");
                exitcode = new ContinuousDeliveryDockerHook(
                        myAppUtils.getContinuousBmPath(),
                        myAppUtils.getDockerMetadataFileName(),
                        myAppUtils.getDockerBenchmarkedFileName(),
                        myAppUtils.getContinuousErrorLogFileName(),
                        myAppUtils.getContinuousBmPath())
                        .forEachStore();
                resp = "Successfully ran Basilisk on Docker hook.";
            } catch (InterruptedException ex) {
                logger.warning("Basilisk is interrupted.");
                ex.printStackTrace(pw);
                return sw.toString();
            }
            return "Done";
        } else if (hook == 1) {
            try {
                logger.info("Initiated Basilisk benchmark process on Git hook.");
                exitcode = new ContinuousDeliveryGitHook(
                        myAppUtils.getContinuousBmPath(),
                        myAppUtils.getGitMetaDataFileName(),
                        myAppUtils.getGitBenchmarkedFileName(),
                        myAppUtils.getContinuousErrorLogFileName(),
                        myAppUtils.getBmWorkSpace())
                        .forEachStore();
                resp = "Successfully ran Basilisk on Git hook.";
            } catch (InterruptedException ex) {
                logger.warning("Basilisk is interrupted.");
                ex.printStackTrace(pw);
                return sw.toString();
            } catch (IOException e) {
                logger.warning("Basilisk is interrupted.");
                e.printStackTrace(pw);
                return sw.toString();
            }
        } else {
            logger.info("Initiated Basilisk benchmark process on invalid hook.");
            resp = "Invalid value to the hook parameter. Please look at the below values to the hook parameter.\n" +
                    "1 means run CPB for github hook\n" +
                    "2 means run CPB for docker hub hook\n";
        }

        if(exitcode == 0) {
            return resp;
        } else {
            return "Problem encountered while running Basilisk. Please try again.";
        }

    }
}
