package de.upb.dss.basilisk.security;

/**
 * This is the Basilisk admin user data.
 */
class BasiliskAdmin {
    private static String adminUserName = null;
    private static String adminPassword = null;
    private static String salt = null;

    /**
     * Setter for Admin user name
     *
     * @param userName Admin user name
     */
    public static void setAdminUserName(String userName) {
        if (BasiliskAdmin.adminUserName == null)
            BasiliskAdmin.adminUserName = userName;
    }

    /**
     * Setter for Admin password
     *
     * @param password Admin password
     */
    public static void setAdminPassword(String password) {
        if (BasiliskAdmin.adminPassword == null)
            BasiliskAdmin.adminPassword = password;
    }

    /**
     * Setter for salt
     *
     * @param saltInByte Salt in array of bytes.
     */
    public static void setSalt(String saltInByte) {
        if (BasiliskAdmin.salt == null)
            BasiliskAdmin.salt = saltInByte;
    }

    /**
     * Getter for Admin user name
     *
     * @return Admin user name
     */
    public static String getAdminUserName() {
        return adminUserName;
    }

    /**
     * Getter for Admin password
     *
     * @return Admin password
     */
    public static String getAdminPassword() {
        return adminPassword;
    }

    /**
     * Getter for salt
     *
     * @return Salt
     */
    public static String getSalt() {
        return salt;
    }
}
