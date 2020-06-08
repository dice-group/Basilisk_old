package de.upb.dss.basilisk.security;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This class provides the functionality of reading the password file.
 *
 * @author Ranjith Krishnamurthy
 */
public class ReadAuth {
    /**
     * This method checks whether given user name is valid or not.
     *
     * @param userName User name
     * @return Returns boolean, true indicates valid user name.
     */
    public static boolean isValidUser(String userName) {
        File passFile = new File(new ApplicationPropertiesUtils().getPassFile());

        if (!PasswordFileUtils.isPasswordFileExist())
            return false;

        try {
            Scanner scanner = new Scanner(passFile);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                String[] arr = line.split(":");

                if (arr[0].equals(userName))
                    return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This method checks whether given user name is admin or not.
     *
     * @param userName User name
     * @return Returns boolean, true indicates user name is admin.
     */
    public static boolean isAdmin(String userName) {
        File passFile = new File(new ApplicationPropertiesUtils().getPassFile());

        if (!PasswordFileUtils.isPasswordFileExist())
            return false;

        try {
            Scanner scanner = new Scanner(passFile);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                String[] arr = line.split(":");

                if (arr[0].equals(userName))
                    if (arr[4].equals("admin"))
                        return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
