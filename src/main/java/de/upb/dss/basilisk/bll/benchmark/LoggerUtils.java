package de.upb.dss.basilisk.bll.benchmark;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This is a utility class for supporting the logging process.
 */
public class LoggerUtils {
    /**
     * Initialize the logger.
     *
     * @param logFilePath Log file path name.
     * @param name        Name of the log.
     * @return Logger.
     */
    public Logger getLogger(String logFilePath, String name) {
        Logger logger = Logger.getLogger(name);

        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler(logFilePath);
            logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logger;
    }
}
