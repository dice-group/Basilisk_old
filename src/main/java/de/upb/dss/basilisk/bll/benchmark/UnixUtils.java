package de.upb.dss.basilisk.bll.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This is a utility class to support the unix commands execution.
 */
public class UnixUtils {
    private String cmdOut = "";
    private String cmdErr = "";

    /**
     * Returns the output of the last run unix command.
     *
     * @return Output of the last command.
     */
    public String getOutput() {
        return cmdOut;
    }

    /**
     * Returns the error of the last run unix command.
     *
     * @return Error of the last command.
     */
    public String getError() {
        return cmdErr;
    }

    /**
     * Runs the unix command.
     *
     * @param cmd    Command.
     * @param path   Path to run the command.
     * @param isWait Boolean to indicate that process should wait for the completion.
     * @return Status code.
     * @throws InterruptedException If the process is interrupted.
     * @throws IOException          If fails to update the log.
     */
    public int runUnixCommand(String cmd, File path, boolean isWait) throws InterruptedException, IOException {
        LoggerUtils myLoggerUtils = new LoggerUtils();

        String log = "";
        String s = "";
        String err = "";

        //Run the command through Process.
        Process p = Runtime.getRuntime().exec(cmd, null, path);

        //track the output and error to log.
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        System.out.println("Output of the command is :\n");
        while ((s = stdInput.readLine()) != null) {
            log = log + "\n" + s;
        }

        cmdOut = log;

        System.out.println("Error/Warning of the command :\n");
        while ((s = stdError.readLine()) != null) {
            err = err + "\n" + s;
        }

        cmdErr = log;

        int exitCode = -1;

        if (isWait) {
            p.waitFor();    //Wait for the process to complete.

            exitCode = p.exitValue();
        }

        stdInput.close();
        stdError.close();

        return exitCode;
    }
}
