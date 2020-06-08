package de.upb.dss.basilisk.security;

import de.upb.dss.basilisk.ErrorCode.EXITCODE;
import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class provides the functionality of admin operation on password file
 *
 * @author Ranjith Krishnamurthy
 */
public class AdminOperation {
    /**
     * This method updates the password file based on approved ot rejected.
     *
     * @param userName Username
     * @param status   Approved or rejected
     *                 1: Approved
     *                 2: rejected
     */
    private static void update(String userName, boolean isAdmin, OPERATIONTYPE status) {
        String pendingPassFile = new ApplicationPropertiesUtils().getPassPendingFile();

        String updatePendingPass = "";

        PasswordFileUtils.creatPassFile();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(pendingPassFile));

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] arr = currentLine.split(":");
                if (arr[0].equals(userName)) {
                    String hashedPass = arr[1];
                    String encodeSalt = arr[2];
                    String email = arr[3];

                    if (status == OPERATIONTYPE.APPROVE) { // Approved
                        PasswordFileUtils.addNewCred(userName, hashedPass, encodeSalt, email, isAdmin, status);
                        //Todo: Send email with approved message
                    } else if (status == OPERATIONTYPE.REJECT) { // Reject
                        //Todo: Send email with rejected message
                    }


                } else {
                    updatePendingPass += currentLine + "\n";
                }
            }
            reader.close();
            PasswordFileUtils.updateCompletePassFile(updatePendingPass, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method approves the newly signed up user.
     *
     * @param userName User name
     */
    public static void approve(String userName, boolean isAdmin) {
        update(userName, isAdmin, OPERATIONTYPE.APPROVE);
    }

    /**
     * This method rejects the newly signed up user
     *
     * @param userName User name
     */
    public static void reject(String userName) {
        update(userName, false, OPERATIONTYPE.REJECT);
    }

    /**
     * This method gives the admin access to a user.
     *
     * @param userName User name
     * @return Exit code
     * @see de.upb.dss.basilisk.ErrorCode.EXITCODE
     */
    public static EXITCODE giveAdminAccess(String userName) {
        String passFile = new ApplicationPropertiesUtils().getPassFile();

        if (!ReadAuth.isValidUser(userName))
            return EXITCODE.INVALID_USER;

        String updatePass = "";

        PasswordFileUtils.creatPassFile();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(passFile));

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] arr = currentLine.split(":");
                if (arr[0].equals(userName)) {
                    String hashedPass = arr[1];
                    String encodeSalt = arr[2];
                    String email = arr[3];

                    updatePass += userName + ":" +
                            hashedPass + ":" +
                            encodeSalt + ":" +
                            email + ":" +
                            "admin" + "\n";

                } else {
                    updatePass += currentLine + "\n";
                }
            }
            reader.close();
            PasswordFileUtils.updateCompletePassFile(updatePass, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EXITCODE.SUCCESS;
    }
}
