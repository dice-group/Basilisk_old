package de.upb.dss.basilisk.ErrorCode;

/**
 * ENUM that represents the EXIT CODE for security purpose.
 *
 * @author Ranjith Krishnamurthy
 */
public enum EXITCODE {
    SUCCESS(0),
    USER_EXIST(-150),
    INVALID_EMAIL(-50),
    INVALID_USER(-100),
    WRONG_PASSWORD(-200),
    HASH_ERROR(-300),
    NULL_VALUE(-400),
    NOT_ADMIN(-800),
    INVALID_PARAM_VALUE(-500);

    private final int exitCode;

    EXITCODE(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
