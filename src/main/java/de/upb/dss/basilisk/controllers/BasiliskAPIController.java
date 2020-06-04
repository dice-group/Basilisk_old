package de.upb.dss.basilisk.controllers;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;
import de.upb.dss.basilisk.bll.dockerHook.ContinuousDeliveryDockerHook;
import de.upb.dss.basilisk.bll.gitHook.ContinuousDeliveryGitHook;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This is the Basilisk's Rest Controller.
 *
 * @author Ranjith Krishnamurthy
 * @author Samrat Dutta
 */
@RestController
public class BasiliskAPIController {
    private static final String logPrefix = "BasiliskMain";

    /**
     * Index of the Basilisk that returns information of other mappings.
     *
     * @return Returns the information of other mappings.
     */
    @RequestMapping("/")
    public String index() {
        return "Basilisk is running...\n" +
                "To run the CPB on Git hook use: http://131.234.28.165:8080/runbenchmark?hook=1\n" +
                "To run the CPB on Docker hook use: http://131.234.28.165:8080/runbenchmark?hook=2\n";
    }

    /**
     * This runs the benchmark based on the value of the parameter hook.
     *
     * @param hook Integer parameter to indicate where to run the benchmark.
     *             1 means run on git hook
     *             2 means run on docker hook.
     * @return Returns the status of the benchmarking process as a string.
     */
    @RequestMapping("/runbenchmark")
    public String runBenchmark(@RequestParam(defaultValue = "2") int hook) throws InterruptedException {
        ApplicationPropertiesUtils myAppUtils = new ApplicationPropertiesUtils();
        String logFilePath = myAppUtils.getLogFilePath();

        int exitcode = -1;
        String resp = "";

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        if (hook == 2) {
            LoggerUtils.logForBasilisk(logPrefix, "Initiated Basilisk benchmark process on Docker hook.", 1);
            exitcode = new ContinuousDeliveryDockerHook()
                    .forEachStore();
            resp = "Successfully ran Basilisk on Docker hook.";
        } else if (hook == 1) {
            LoggerUtils.logForBasilisk(logPrefix, "Initiated Basilisk benchmark process on Git hook.", 1);
            exitcode = new ContinuousDeliveryGitHook()
                    .forEachStore();
            resp = "Successfully ran Basilisk on Git hook.";
        } else {
            LoggerUtils.logForBasilisk(logPrefix, "Initiated Basilisk benchmark process on invalid hook.", 1);
            resp = "Invalid value to the hook parameter. Please look at the below values to the hook parameter.\n" +
                    "1 means run CPB for github hook\n" +
                    "2 means run CPB for docker hub hook\n";
        }

        if (exitcode == 0) {
            return resp;
        } else {
            return "Problem encountered while running Basilisk. Please try again.";
        }

    }
}
