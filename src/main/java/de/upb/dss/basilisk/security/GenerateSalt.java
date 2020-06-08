package de.upb.dss.basilisk.security;

import java.security.SecureRandom;

/**
 * This class provides the functionality of salt for hashing.
 */
public class GenerateSalt {
    /**
     * This generates the salt dynamically.
     *
     * @return Salt.
     */
    public static byte[] getSalt() {
        SecureRandom RANDOM = new SecureRandom();
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return salt;
    }
}
