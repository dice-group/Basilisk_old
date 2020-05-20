package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FreeMarkerTemplateEngineUtils {
    /**
     * This method creates the benchmark configuration for the Iguana for the currently running
     * triple store.
     *
     * @return Status code.
     */
    public static int setIguanaConfigFile(String repoName, String tag, String port,
                                           String queryFile, String configPath) {

        Logger logger = new LoggerUtils().getLogger(new ApplicationPropertiesUtils().getLogFilePath(),
                "FreeMarker Utils");
        //Get the freemarker configuration
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
