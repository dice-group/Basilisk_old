package de.upb.dss.basilisk.controllers;

import de.upb.dss.basilisk.ErrorCode.EXITCODE;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;
import de.upb.dss.basilisk.bll.dockerHook.ContinuousDeliveryDockerHook;
import de.upb.dss.basilisk.bll.gitHook.ContinuousDeliveryGitHook;
import de.upb.dss.basilisk.security.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringWriter;

/**
 * This is the Basilisk's user Rest Controller.
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
                "1. To run the CPB on Git hook use: http://131.234.28.165:8080/runbenchmark?hook=1\n" +
                "2. To run the CPB on Docker hook use: http://131.234.28.165:8080/runbenchmark?hook=2\n" +
                "3. To sign up: http://131.234.28.165:8080/signUp?userName=<userName>&password=<password>&email=<email>\n" +
                "4. To change password: http://131.234.28.165:8080/changePassword?userName=<user name>&" +
                "oldPassword=<old password>&newPassword=<new password>\n";
    }

    /**
     * This runs the benchmark based on the value of the parameter hook.
     *
     * @param hook Integer parameter to indicate where to run the benchmark.
     *             1 means run on git hook
     *             2 means run on docker hook.
     * @return Exit code.
     * @see de.upb.dss.basilisk.ErrorCode.EXITCODE
     */
    @RequestMapping("/runbenchmark")
    public String runBenchmark(@RequestParam(defaultValue = "2") int hook,
                               @RequestParam(defaultValue = "null") String userName,
                               @RequestParam(defaultValue = "null") String password) throws InterruptedException {

        if (userName.equals("null") || password.equals("null")) {
            LoggerUtils.logForSecurity(logPrefix,
                    "Someone tried to run Basilisk CPB with no user name and password.", 100);

            return "{\n" +
                    "   \"message\":\"User name and password is required\"\n" +
                    "   \"exitCode\":" + EXITCODE.NULL_VALUE.getExitCode() + "\n" +
                    "}\n";
        }

        EXITCODE authenticationExitCode = AuthenticateCred.isAuthenticate(userName, password);
        if (authenticationExitCode == EXITCODE.INVALID_USER) {
            LoggerUtils.logForSecurity(logPrefix,
                    "Someone tried to run Basilisk CPB with invalid user name: " + userName, 100);

            return "{\n" +
                    "   \"message\":\"Invalid user name\"\n" +
                    "   \"exitCode\":" + authenticationExitCode.getExitCode() + "\n" +
                    "}\n";
        }

        if (authenticationExitCode == EXITCODE.WRONG_PASSWORD) {
            LoggerUtils.logForSecurity(logPrefix,
                    userName + " tried to run Basilisk CPB with invalid password.", 100);

            return "{\n" +
                    "   \"message\":\"Invalid password\"\n" +
                    "   \"exitCode\":" + authenticationExitCode.getExitCode() + "\n" +
                    "}\n";
        }

        if (authenticationExitCode == EXITCODE.HASH_ERROR) {
            LoggerUtils.logForSecurity(logPrefix,
                    "Something went wrong while authenticating the password for user name: " + userName, 100);

            return "{\n" +
                    "   \"message\":\"Something went wrong while authenticating the user name and password\"\n" +
                    "   \"exitCode\":" + authenticationExitCode.getExitCode() + "\n" +
                    "}\n";
        }

        if (authenticationExitCode == EXITCODE.SUCCESS) {
            int exitcode = -1;
            String resp = "";

            StringWriter sw = new StringWriter();

            if (hook == 2) {
                LoggerUtils.logForBasilisk(logPrefix,
                        userName + " kicked off Basilisk CPB on Docker hook ", 1);

                exitcode = new ContinuousDeliveryDockerHook()
                        .forEachStore();
                resp = "Successfully ran Basilisk on Docker hook.";
            } else if (hook == 1) {
                LoggerUtils.logForBasilisk(logPrefix,
                        userName + " kicked off Basilisk CPB on Git hook ", 1);

                exitcode = new ContinuousDeliveryGitHook()
                        .forEachStore();
                resp = "Successfully ran Basilisk on Git hook.";
            } else {
                LoggerUtils.logForBasilisk(logPrefix,
                        userName + " kicked off Basilisk CPB on invalid hook ", 1);
                resp = "Invalid value to the hook parameter. Please look at the below values to the hook parameter.\n" +
                        "1 means run CPB for github hook\n" +
                        "2 means run CPB for docker hub hook\n";
            }

            if (exitcode == 0) {
                return "{\n" +
                        "   \"message\":\"" + resp + "\"\n" +
                        "   \"exitCode\":0\n" +
                        "}\n";
            } else {
                return "{\n" +
                        "   \"message\":\"Problem encountered while running Basilisk. Please try again.\"\n" +
                        "   \"exitCode\":" + exitcode + "\n" +
                        "}\n";
            }
        }

        return "Unknown error";
    }

    /**
     * This signs up the user for Basilisk application.
     *
     * @param userName User name.
     * @param password Password
     * @param email    Email id
     * @return Exit code.
     * @see de.upb.dss.basilisk.ErrorCode.EXITCODE
     */
    @RequestMapping("/signUp")
    public String signUpNewUser(@RequestParam(defaultValue = "null") String userName,
                                @RequestParam(defaultValue = "null") String password,
                                @RequestParam(defaultValue = "null") String email) {

        if (userName.equals("null") || password.equals("null") || email.equals("null")) {
            LoggerUtils.logForSecurity(logPrefix, "Someone tried to sign up for Basilisk without passing " +
                    "userName, password and email id", 100);

            return "{\n" +
                    "   \"message\":\"User name, password and email id all are required\"\n" +
                    "   \"exitCode\":-400\n" +
                    "}\n";
        }

        EXITCODE exitCode = InsertAuth.signUp(userName, password, email, OPERATIONTYPE.PENDING);

        if (exitCode == EXITCODE.SUCCESS) {
            LoggerUtils.logForSecurity(logPrefix, userName + " signed up for Basilisk. Waiting for admin's " +
                    "approve.", 1);

            return "{\n" +
                    "   \"message\":\"Successfully signed up. Waiting for admin to approve.\"\n" +
                    "   \"exitCode\":" + exitCode.getExitCode() + "\n" +
                    "}\n";
        } else if (exitCode == EXITCODE.USER_EXIST) {
            LoggerUtils.logForSecurity(logPrefix, "Someone tried to sign up with the existing userName: "
                    + userName, 4);

            return "{\n" +
                    "   \"message\":\"User name already exist. Please use different user name.\"\n" +
                    "   \"exitCode\":" + exitCode.getExitCode() + "\n" +
                    "}\n";
        } else {
            LoggerUtils.logForSecurity(logPrefix, userName + " tried to sign up with the " +
                    "invalid email id", 4);
            return "{\n" +
                    "   \"message\":\"Invalid email id.\"\n" +
                    "   \"exitCode\":" + exitCode.getExitCode() + "\n" +
                    "}\n";
        }
    }

    /**
     * This method lets the user change their password
     *
     * @param userName    User name
     * @param oldPassword Old password for authentication
     * @param newPassword New Password
     * @return Exit code
     * @see de.upb.dss.basilisk.ErrorCode.EXITCODE
     */
    @RequestMapping("/changePassword")
    public String changePassword(@RequestParam(defaultValue = "null") String userName,
                                 @RequestParam(defaultValue = "null") String oldPassword,
                                 @RequestParam(defaultValue = "null") String newPassword) {

        if (userName.equals("null") || oldPassword.equals("null") || newPassword.equals("null")) {
            LoggerUtils.logForSecurity(logPrefix, "Someone tried to change the password without passing " +
                    "the userName, old password and new password", 100);

            return "{\n" +
                    "   \"message\":\"User name, old password and new password id all are required\"\n" +
                    "   \"exitCode\":" + EXITCODE.NULL_VALUE.getExitCode() + "\n" +
                    "}\n";
        }

        EXITCODE exitCode = UpdateAuth.updatePassword(userName, oldPassword, newPassword);

        if (exitCode == EXITCODE.SUCCESS) {
            LoggerUtils.logForSecurity(logPrefix,
                    userName + " successfully change his/her password", 1);

            return "{\n" +
                    "   \"message\":\"Successfully changed the password.\"\n" +
                    "   \"exitCode\":" + exitCode.getExitCode() + "\n" +
                    "}\n";
        } else if (exitCode == EXITCODE.INVALID_USER) {
            LoggerUtils.logForSecurity(logPrefix,
                    "Someone tried to change his/her password with invalid userName", 100);
            return "{\n" +
                    "   \"message\":\"Invalid user name.\"\n" +
                    "   \"exitCode\":" + exitCode.getExitCode() + "\n" +
                    "}\n";
        } else if (exitCode == EXITCODE.WRONG_PASSWORD) {
            LoggerUtils.logForSecurity(logPrefix,
                    userName + " tried to change his/her password with wrong password", 4);
            return "{\n" +
                    "   \"message\":\"Wrong Password.\"\n" +
                    "   \"exitCode\":" + exitCode.getExitCode() + "\n" +
                    "}\n";
        } else if (exitCode == EXITCODE.HASH_ERROR) {
            LoggerUtils.logForSecurity(logPrefix,
                    "Something went wrong in hasing the password" +
                            "while changing the password for " + userName, 100);

            return "{\n" +
                    "   \"message\":\"Something went wrong while hashing the password.\"\n" +
                    "   \"exitCode\":" + exitCode.getExitCode() + "\n" +
                    "}\n";
        }

        return "Unknown error";
    }
}
