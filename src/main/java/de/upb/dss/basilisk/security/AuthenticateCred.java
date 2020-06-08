package de.upb.dss.basilisk.security;

import de.upb.dss.basilisk.ErrorCode.EXITCODE;
import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class provides the authentication functionality
 *
 * @author Ranjith Krishnamurthy
 */
public class AuthenticateCred {
    /**
     * This method authenticates the username and password.
     *
     * @param userName User name
     * @param password Password
     * @return Exit code.
     * @see de.upb.dss.basilisk.ErrorCode.EXITCODE
     */
    public static EXITCODE isAuthenticate(String userName, String password) {
        String passFile = new ApplicationPropertiesUtils().getPassFile();

        if (!PasswordFileUtils.isPasswordFileExist())
            return EXITCODE.INVALID_USER;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(passFile));

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] arr = currentLine.split(":");
                if (arr[0].equals(userName)) {
                    String encodeSalt = arr[2];
                    byte[] salt = Base64Utils.decode64(encodeSalt);

                    String hashedPass = BasiliskHash.hashThePassword(password, salt);

                    if (hashedPass == null)
                        return EXITCODE.HASH_ERROR;

                    if (hashedPass.equals(arr[1]))
                        return EXITCODE.SUCCESS;
                    else
                        return EXITCODE.WRONG_PASSWORD;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EXITCODE.INVALID_USER;
    }
}
