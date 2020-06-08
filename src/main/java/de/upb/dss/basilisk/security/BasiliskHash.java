package de.upb.dss.basilisk.security;

import de.upb.dss.basilisk.bll.benchmark.LoggerUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * This class provides the functionality of Hashing the password.
 *
 * @author Ranjith Krishnamurthy
 */
public class BasiliskHash {
    /**
     * This method hash the given password with the given salt.
     *
     * @param pass Password
     * @param salt Salt
     * @return Hashed password
     */
    public static String hashThePassword(String pass, byte[] salt) {
        KeySpec keySpec = new PBEKeySpec(
                pass.toCharArray(),
                salt,
                7000,
                256
        );

        SecretKeyFactory secretKeyFactory = null;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hashedPass = secretKeyFactory.generateSecret(keySpec).getEncoded();

            return Base64.getEncoder().encodeToString(hashedPass);
        } catch (NoSuchAlgorithmException e) {
            LoggerUtils.logForBasilisk("BasiliskHash", "Invalid algorithm PBKDF2WithHmacSHA1", 100);
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            LoggerUtils.logForBasilisk("BasiliskHash", "Invalid KeySpec", 100);
            e.printStackTrace();
        }

        return null;
    }
}
