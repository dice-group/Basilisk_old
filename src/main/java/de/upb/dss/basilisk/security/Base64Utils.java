package de.upb.dss.basilisk.security;

import java.util.Base64;

/**
 * This class provides the functionality of encoding/decoding into/from Base64.
 *
 * @author Ranjith Krishnamurthy
 */
public class Base64Utils {
    /**
     * This method encodes the given message into Base64
     *
     * @param message Message in array of byte
     * @return Encoded string
     */
    public static String encode64(byte[] message) {
        return Base64.getEncoder().encodeToString(message);
    }

    /**
     * This method decodes the given encoded message into array of bytes
     *
     * @param message Encoded string
     * @return Decoded message
     */
    public static byte[] decode64(String message) {
        return Base64.getDecoder().decode(message);
    }
}
