package de.upb.dss.basilisk.security;

import de.upb.dss.basilisk.ErrorCode.EXITCODE;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;

/**
 * This class provides the authentication functionality
 *
 * @author Ranjith Krishnamurthy
 */
public class AuthenticateCred {
    /**
     * This method authenticates the username and password.
     *
     * @param userName  User name
     * @param adminPass Password
     * @return Exit code.
     * @see de.upb.dss.basilisk.ErrorCode.EXITCODE
     */
    public static EXITCODE isAuthenticate(String userName, String adminPass) {

        if ((BasiliskAdmin.getAdminUserName() == null) ||
                (BasiliskAdmin.getAdminPassword() == null) ||
                (BasiliskAdmin.getSalt() == null)) {
            LoggerUtils.logForSecurity(
                    "Basilisk Authentication",
                    "Admin account is terminated. Please re-start Basilisk",
                    100);

            return EXITCODE.NULL_VALUE;
        }

        byte[] salt = Base64Utils.decode64(BasiliskAdmin.getSalt());
        String hashedPassword = BasiliskHash.hashThePassword(adminPass, salt);

        if (BasiliskAdmin.getAdminUserName().equals(userName)) {
            if (BasiliskAdmin.getAdminPassword().equals(hashedPassword)) {
                return EXITCODE.SUCCESS;
            } else {
                return EXITCODE.WRONG_PASSWORD;
            }
        } else {
            return EXITCODE.INVALID_USER;
        }
    }
}
