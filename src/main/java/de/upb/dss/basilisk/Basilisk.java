package de.upb.dss.basilisk;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * The main class of the Basilisk Spring application.
 * <p>
 * Basilisk is a Continuous Benchmarking Platform to benchmark the Triple Stores.
 * Currently Basilisk runs CPB on Git hook and Docker hook for the below three Triple stores.
 * <p>
 * 1. Tentris by Dice group (Both in Git and Docker hooks)
 * 2. Fuseki by Apache Jena (in Git hook)
 * 3. Virtuoso by OpenLink (in Docker hook)
 *
 * @author Ranjith Krishnamurthy
 * @author Samrat Dutta
 */
@SpringBootApplication
public class Basilisk {
    public static Properties applicationProperties;
    private static final String logPrefix = "BasiliskMain";

    /**
     * The main method of the Basilisk Spring application.
     *
     * @param args Arguments to the Basilisk application.
     */
    public static void main(String[] args) {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getFile();
        System.out.println(rootPath);
        String appConfigPath = rootPath + "application.properties";
        System.out.println(appConfigPath);

        applicationProperties = new Properties();
        try {
            applicationProperties.load(new FileInputStream(appConfigPath));
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        SpringApplication app = new SpringApplication(Basilisk.class);
        app.run(args);
        printWelcomeMessage();
        LoggerUtils.logForBasilisk(logPrefix, "Basilisk is running", 1);
    }

    /**
     * This method prints the Basilisk's welcome message once the Basilisk spring boot is up.
     */
    private static void printWelcomeMessage() {
        String basiliskVersion = new ApplicationPropertiesUtils().getBasiliskVersion();
        String blankLines = "                                 ";
        String WelcomeMessage = "     Basilisk: version " + basiliskVersion + "     ";

        final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
        final String ANSI_RESET = "\u001B[0m";

        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + WelcomeMessage + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
    }
}
