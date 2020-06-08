package de.upb.dss.basilisk.controllers;

import de.upb.dss.basilisk.ErrorCode.EXITCODE;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;
import de.upb.dss.basilisk.security.AdminOperation;
import de.upb.dss.basilisk.security.AuthenticateCred;
import de.upb.dss.basilisk.security.ReadAuth;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is the Basilisk's admin Rest Controller.
 *
 * @author Ranjith Krishnamurthy
 */
@RestController
public class BasiliskAPIAdminController {
    private static final String logPrefix = "Admin Controller";

    /**
     * Index of the Basilisk admin page that returns information of admin's other mappings.
     *
     * @return Returns the information of admin's other mappings.
     */
    @RequestMapping("/admin")
    public String index() {
        return "Basilisk is running...\n" +
                "For admins:\n" +
                "1. To authorize a user: " +
                "http://131.234.28.165:8080/admin/authorizeUser?adminUserName=<adminUserName>&adminPass=<adminPass>" +
                "&userName=<userName>&isApproved=<isApproved>\n";
    }

    /**
     * This method lets the admin authorize the user that are signed up and waiting for admin's approval.
     *
     * @param adminUserName      admin user name
     * @param adminPass          admin password
     * @param userName           user name to be authorized
     * @param authorizeOperation authorization operation
     *                           1. Approved
     *                           2. Rejected
     * @param role               Role
     *                           1. User
     *                           2. Admin
     * @return Exit code.
     * @see de.upb.dss.basilisk.ErrorCode.EXITCODE
     */
    @RequestMapping("/admin/authorizeUser")
    public String authorizeUser(@RequestParam(defaultValue = "null") String adminUserName,
                                @RequestParam(defaultValue = "null") String adminPass,
                                @RequestParam(defaultValue = "null") String userName,
                                @RequestParam(defaultValue = "null") String authorizeOperation,
                                @RequestParam(defaultValue = "user") String role) {

        // All the parameters are mandatory except role, role's default value id user
        if (adminUserName.equals("null") || adminPass.equals("null") || userName.equals("null") || authorizeOperation.equals("null")) {
            LoggerUtils.logForSecurity(logPrefix,
                    "Someone tried to authorize the user without one of the " +
                            "required parameter", 100);

            return "{\n" +
                    "   \"message\":\"User adminUserName, adminPass, password and isApproved all are required\"\n" +
                    "   \"exitCode\":" + EXITCODE.NULL_VALUE.getExitCode() + "\n" +
                    "}\n";
        }

        // Check for the valid user.
        if(!ReadAuth.isValidUser(adminUserName)) {
            LoggerUtils.logForSecurity(logPrefix,
                    "Invalid user name: " + adminUserName, 100);

            return "{\n" +
                    "   \"message\":\"Invalid user name\"\n" +
                    "   \"exitCode\":" + EXITCODE.INVALID_USER.getExitCode() + "\n" +
                    "}\n";
        }

        // Check the admin is performed this operation
        if (!ReadAuth.isAdmin(adminUserName)) {
            LoggerUtils.logForSecurity(logPrefix,
                    adminUserName + " tried to authorize the user without admin access", 100);

            return "{\n" +
                    "   \"message\":\"You are not authenticated to perform this operation\"\n" +
                    "   \"exitCode\":" + EXITCODE.NOT_ADMIN.getExitCode() + "\n" +
                    "}\n";
        }

        // Authenticate the admin userName and password
        EXITCODE exitcode = AuthenticateCred.isAuthenticate(adminUserName, adminPass);

        if (exitcode == EXITCODE.INVALID_USER) {
            LoggerUtils.logForBasilisk(logPrefix,
                    "Someone tried to run Basilisk CPB with invalid user name: " + userName, 100);

            return "{\n" +
                    "   \"message\":\"Invalid user name\"\n" +
                    "   \"exitCode\":" + exitcode.getExitCode() + "\n" +
                    "}\n";
        }

        if (exitcode == EXITCODE.WRONG_PASSWORD) {
            LoggerUtils.logForBasilisk(logPrefix,
                    adminUserName + " tried to run Basilisk CPB with invalid password.", 100);

            return "{\n" +
                    "   \"message\":\"Invalid password\"\n" +
                    "   \"exitCode\":" + exitcode.getExitCode() + "\n" +
                    "}\n";
        }

        if (exitcode == EXITCODE.HASH_ERROR) {
            LoggerUtils.logForBasilisk(logPrefix,
                    "Something went wrong while authenticating the password for user name: " + userName, 100);

            return "{\n" +
                    "   \"message\":\"Something went wrong while authenticating the user name and password\"\n" +
                    "   \"exitCode\":" + exitcode.getExitCode() + "\n" +
                    "}\n";
        }

        if (exitcode == EXITCODE.SUCCESS) {
            String status = "";
            if (authorizeOperation.toLowerCase().equals("approved")) {
                boolean isAdmin = false;
                if (role.toLowerCase().equals("admin"))
                    isAdmin = true;

                AdminOperation.approve(userName, isAdmin);
                status = "Approved";

                LoggerUtils.logForSecurity(logPrefix,
                        adminUserName + "successfully approved the user: " + userName, 1);

            } else if (authorizeOperation.toLowerCase().equals("rejected")) {
                AdminOperation.reject(userName);
                status = "Rejected";

                LoggerUtils.logForSecurity(logPrefix,
                        adminUserName + "successfully rejected the user: " + userName, 1);
            } else {
                LoggerUtils.logForSecurity(logPrefix,
                        adminUserName + "tried approve the user: " + userName + " with " +
                                "invalid isApproved option", 4);

                return "{\n" +
                        "   \"message\":\"Invalid authorizeOperation value. authorizeOperation value should be either approved or rejected.\"\n" +
                        "   \"exitCode\":" + EXITCODE.INVALID_PARAM_VALUE.getExitCode() + "\n" +
                        "}\n";
            }


            return "{\n" +
                    "   \"message\":\"" + status + ".\"\n" +
                    "   \"exitCode\":0\n" +
                    "}\n";
        }

        return "Unknown error";

    }
}
