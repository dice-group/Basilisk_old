package de.upb.dss.basilisk;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * The main class of the Basilisk Spring application.
 */
@SpringBootApplication
public class Basilisk {
    public static Properties applicationProperties;

    /**
     * The main method of the Basilisk Spring application.
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

        String logPath = new ApplicationPropertiesUtils().getLogFilePath();
        Logger logger = new LoggerUtils().getLogger(logPath,"BasiliskMain");
        SpringApplication app = new SpringApplication(Basilisk.class);
        app.run(args);
        printWelcomeMessage();
        logger.info("Basilisk is running");
    }

    private static void printWelcomeMessage() {
        String blankLines = "                                 ";
        String WelcomeMessage = "     Basilisk: version 1.0.0     ";

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
