package de.upb.dss.basilisk;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
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
    private static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    private static final String ANSI_RESET = "\u001B[0m";

    /**
     * The main method of the Basilisk Spring application.
     *
     * @param args Arguments to the Basilisk application.
     */
    public static void main(String[] args) throws IOException {

        if (!checkArguments(args)) {
            System.out.println("Invalid option. Available option is:\n" +
                    "--admin-user-name <admin user name>\n" +
                    "--admin-pass <admin password>\n");
            return;
        }

        applicationProperties = new Properties();
        try {
            applicationProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        System.out.println("\n\n###################################################################################\n");
        System.out.println("\t--> Setting up Basilisk. Please wait....");
        InitialSetup.setup(args);
        System.out.println("\n###################################################################################\n\n");

        SpringApplication app = new SpringApplication(Basilisk.class);
        app.run(args);

        System.out.println("Basilisk is ready and up.\n\n");
        printWelcomeMessage();
        LoggerUtils.logForBasilisk(logPrefix, "Basilisk is running", 1);
    }

    /**
     * This method checks for the valid arguments provided to Basilisk
     *
     * @param args Arguments
     * @return Boolean, True if arguments are valid
     */
    private static boolean checkArguments(String[] args) {
        if (args.length != 4) {
            System.out.println("Got too many arguments or too few arguments.");
            return false;
        }


        if (args[0].equals(args[2]))
            return false;

        if (!("--admin-pass".equals(args[2]) || "--admin-user-name".equals(args[2])))
            return false;


        if (!("--admin-pass".equals(args[0]) || "--admin-user-name".equals(args[0])))
            return false;


        return true;
    }

    /**
     * This method prints the Basilisk's welcome message once the Basilisk spring boot is up.
     */
    private static void printWelcomeMessage() {
        String basiliskVersion = new ApplicationPropertiesUtils().getBasiliskVersion();
        String blankLines = "                                 ";
        String WelcomeMessage = "     Basilisk: version " + basiliskVersion + "     ";


        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + WelcomeMessage + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
        System.out.println(ANSI_GREEN_BACKGROUND + blankLines + ANSI_RESET);
        System.out.println("\n\n");
    }
}
