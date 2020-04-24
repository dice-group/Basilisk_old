package de.upb.dss.basilisk.controllers;

import de.upb.dss.basilisk.Basilisk;
import de.upb.dss.basilisk.bll.ContinuousDelivery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Properties;

@RestController
public class BasiliskAPIController {
    @RequestMapping("/")
    public String index() {
        return "Basilisk is running...";
    }

    @RequestMapping("/runbenchmark")
    public String runBenchmark() {
        Properties appProps = Basilisk.applicationProperties;

        String continuousBmPath = appProps.getProperty("continuousBmPath");
        String metadataFileName = appProps.getProperty("metadataFileName");
        String benchmarkedFileName = appProps.getProperty("benchmarkedFileName");
        String continuousErrorLogFileName = appProps.getProperty("continuousErrorLogFileName");
        String bmWorkspacePath = appProps.getProperty("bmWorkSpace");

        int exitcode = -1;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        ContinuousDelivery obj = new ContinuousDelivery(continuousBmPath, metadataFileName, benchmarkedFileName, continuousErrorLogFileName, bmWorkspacePath);
        try {
            exitcode = obj.forEachStore();
        } catch (Exception ex) {
            ex.printStackTrace(pw);
            return sw.toString();
        }

	try
	{
		File file = new File("/home/dss/continuousBM/log/start-benchmarking.err");
		BufferedReader br = new BufferedReader(new FileReader(file));

		String message = " ";
		String msg;
		while ((msg = br.readLine()) != null)
		{
			message += msg + "\n";
		}

            	return message;
	}catch (Exception e)
	{
		if(exitcode == 0) {
			return "Ran successfully\n";
		}else{
			return "Something went wrong.\n";
		}
	}
    }
}
