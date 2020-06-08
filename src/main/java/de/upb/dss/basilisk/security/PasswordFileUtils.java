package de.upb.dss.basilisk.security;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * This is the utility class to maintain the password file.
 *
 * @author Ranjith Krishnamurthy
 */
public class PasswordFileUtils {
    private static final ApplicationPropertiesUtils applicationPropertiesUtils = new ApplicationPropertiesUtils();

    /**
     * This checks that the password file exist or not.
     *
     * @return Boolean to indicate file exist or not
     */
    public static boolean isPasswordFileExist() {
        return new File(
                applicationPropertiesUtils.getPassFile()
        ).exists();
    }

    /**
     * This checks that the password pending file exist or not.
     *
     * @return Boolean to indicate file exist or not
     */
    public static boolean isPasswordPendingFileExist() {
        return new File(
                applicationPropertiesUtils.getPassPendingFile()
        ).exists();
    }

    /**
     * This method adds the new credentials to the password file.
     *
     * @param userName       User name
     * @param hashedPassword Hashed password
     * @param encodedSalt    encoded salt
     */
    public static void addNewCred(String userName, String hashedPassword, String encodedSalt, String email,
                                  boolean isAdmin, OPERATIONTYPE status) {
        File passFile;
        creatPassFile();
        creatPassPendingFile();

        if (status == OPERATIONTYPE.APPROVE) // New user is approved by the admin
            passFile = new File(applicationPropertiesUtils.getPassFile());
        else if (status == OPERATIONTYPE.PENDING)// New user put it in password pending file
            passFile = new File(applicationPropertiesUtils.getPassPendingFile());
        else
            return;

        String role = "";

        if (isAdmin)
            role = "admin";
        else
            role = "user";

        try {
            Files.write(passFile.toPath(),
                    (userName + ":" +
                            hashedPassword + ":" +
                            encodedSalt + ":" +
                            email + ":" +
                            role + "\n").getBytes(),
                    StandardOpenOption.APPEND);
            //Todo: Send an email saying its in waiting list
        } catch (IOException e) {
            LoggerUtils.logForBasilisk("PasswordFileUtils", "Could not add new cred", 4);
            e.printStackTrace();
        }
    }


    /**
     * This method updates the complete password file.
     *
     * @param newCredData New complete coeducational data.
     */
    public static void updateCompletePassFile(String newCredData, boolean isPassFile) {
        File passFile;

        if (isPassFile)
            passFile = new File(applicationPropertiesUtils.getPassFile());
        else
            passFile = new File(applicationPropertiesUtils.getPassPendingFile());

        try {
            Files.write(passFile.toPath(),
                    (newCredData).getBytes(),
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            LoggerUtils.logForBasilisk("PasswordFileUtils", "Could not update passwd file", 4);
            e.printStackTrace();
        }
    }

    /**
     * This method creates a password file
     */
    public static void creatPassFile() {
        File passFile = new File(applicationPropertiesUtils.getPassFile());

        if (isPasswordFileExist())
            return;

        try {
            passFile.createNewFile();
        } catch (IOException e) {
            LoggerUtils.logForBasilisk("PasswordFileUtils", "Something went wrong while creating password file", 4);
            e.printStackTrace();
        }
    }

    /**
     * This method creates a password pending file
     */
    public static void creatPassPendingFile() {
        File passFile = new File(applicationPropertiesUtils.getPassPendingFile());

        if (isPasswordPendingFileExist())
            return;

        try {
            passFile.createNewFile();
        } catch (IOException e) {
            LoggerUtils.logForBasilisk("PasswordFileUtils", "Something went wrong while creating password file", 4);
            e.printStackTrace();
        }
    }
}
