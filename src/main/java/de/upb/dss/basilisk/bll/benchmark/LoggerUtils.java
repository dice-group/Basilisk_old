package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This is a utility class for supporting the logging process.
 *
 * @author Ranjith Krishnamurthy
 */
public class LoggerUtils {
    private static Logger logger = null;

    /**
     * This method instantiate all the necessary objects for the logging process.
     */
    private static void setLogger() {
        FileHandler fileHandler = null;
        logger = Logger.getLogger("Basilisk");

        try {
            fileHandler = new FileHandler(
                    new ApplicationPropertiesUtils().getLogFilePath()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.addHandler(fileHandler);

        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
    }

    /**
     * Logs the given message.
     *
     * @param prefix  Prefix to the log message.
     * @param message Message to log
     * @param level   int value to indicate the level of log.
     *                0. fine
     *                1. info
     *                4. warning
     *                100. severe
     */
    public static void logForBasilisk(String prefix, String message, int level) {
        if (logger == null) {
            setLogger();
        }

        if (level == 0)
            logger.fine(prefix + " ---> " + message);
        else if (level == 1)
            logger.info(prefix + " ---> " + message);
        else if (level == 4)
            logger.warning(prefix + " ---> " + message);
        else if (level == 100)
            logger.severe(prefix + " ---> " + message);
    }
}
