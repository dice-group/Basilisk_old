package de.upb.dss.basilisk.security;

import de.upb.dss.basilisk.ErrorCode.EXITCODE;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * This class provides the functionality of inserting the new data to the password file.
 *
 * @author Ranjith Krishnamurthy
 */
public class InsertAuth {
    /**
     * This method signs up the new user.
     *
     * @param userName User name
     * @param password Password
     * @param status   User status
     *                 1: Approved user
     *                 2: Pending user
     * @return Exit code
     * @see de.upb.dss.basilisk.ErrorCode.EXITCODE
     */
    public static EXITCODE signUp(String userName, String password, String email, OPERATIONTYPE status) {
        PasswordFileUtils.creatPassPendingFile();

        if (ReadAuth.isValidUser(userName))
            return EXITCODE.USER_EXIST;

        EmailValidator emailValidator = EmailValidator.getInstance();

        if (!emailValidator.isValid(email))
            return EXITCODE.INVALID_EMAIL;

        byte[] salt = GenerateSalt.getSalt();
        String encodedSalt = Base64Utils.encode64(salt);
        String hashedPassword = BasiliskHash.hashThePassword(password, salt);

        PasswordFileUtils.addNewCred(userName, hashedPassword, encodedSalt, email, false, status);

        return EXITCODE.SUCCESS;
    }
}
