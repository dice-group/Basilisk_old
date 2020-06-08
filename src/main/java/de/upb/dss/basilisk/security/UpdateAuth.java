package de.upb.dss.basilisk.security;

import de.upb.dss.basilisk.ErrorCode.EXITCODE;
import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class provides the functionality of changing the password.
 *
 * @author Ranjith Krishnamurthy
 */
public class UpdateAuth {
    /**
     * This method authenticates the username and old password. If authentication successfull then it updates the password to
     * given new password.
     *
     * @param userName    User name.
     * @param oldPassword Old Password
     * @param newPassword New password
     * @return Exit code.
     * @see de.upb.dss.basilisk.ErrorCode.EXITCODE
     */
    public static EXITCODE updatePassword(String userName, String oldPassword, String newPassword) {
        String passFile = new ApplicationPropertiesUtils().getPassFile();

        EXITCODE exitCode = AuthenticateCred.isAuthenticate(userName, oldPassword);
        if (exitCode == EXITCODE.SUCCESS) {
            if (ReadAuth.isValidUser(userName)) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(passFile));
                    String updatedPassTable = "";
                    String currentLine;

                    while ((currentLine = reader.readLine()) != null) {
                        String[] arr = currentLine.split(":");
                        if (arr[0].equals(userName)) {
                            String email = arr[3];
                            String role = arr[4];
                            byte[] salt = GenerateSalt.getSalt();
                            String encodedSalt = Base64Utils.encode64(salt);

                            String hashedPass = BasiliskHash.hashThePassword(newPassword, salt);

                            updatedPassTable += userName + ":" +
                                    hashedPass + ":" +
                                    encodedSalt + ":" +
                                    email + ":" +
                                    role + "\n";
                            continue;
                        }

                        updatedPassTable += currentLine + "\n";
                    }

                    reader.close();

                    PasswordFileUtils.updateCompletePassFile(updatedPassTable, true);
                } catch (IOException e) {
                    LoggerUtils.logForBasilisk("UpdateAuth", "Something went wrong while updating the password", 100);
                    e.printStackTrace();
                }
            }
        } else {
            return exitCode;
        }

        return EXITCODE.SUCCESS;
    }
}
