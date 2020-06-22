package de.upb.dss.basilisk.security;

/**
 * This class initialize the admin account for Basilisk
 */
public class BasiliskAdminInitializer {
    /**
     * Initialize the Basilisk admin account
     *
     * @param adminUserName Admin user name
     * @param adminPassword Admin password
     */
    public static void initializeBasiliskAdmin(String adminUserName, String adminPassword) {
        byte[] salt = GenerateSalt.getSalt();

        String hashedPassword = BasiliskHash.hashThePassword(adminPassword, salt);

        BasiliskAdmin.setAdminUserName(adminUserName);
        BasiliskAdmin.setAdminPassword(hashedPassword);
        BasiliskAdmin.setSalt(Base64Utils.encode64(salt));
    }
}
