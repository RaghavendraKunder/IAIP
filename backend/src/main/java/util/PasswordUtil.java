package util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil.java
 * -----------------
 * Wraps BCrypt hashing so no plain-text passwords are ever stored.
 *
 * Dependency: add jBCrypt-0.4.jar to WEB-INF/lib/
 * Download  : https://www.mindrot.org/projects/jBCrypt/
 */
public class PasswordUtil {

    private static final int WORK_FACTOR = 12; // BCrypt cost (higher = slower = safer)

    /** Hash a plain-text password. Store the returned string in the DB. */
    public static String hash(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(WORK_FACTOR));
    }

    /** Return true if the plain-text matches the stored BCrypt hash. */
    public static boolean verify(String plainText, String storedHash) {
        return BCrypt.checkpw(plainText, storedHash);
    }
}

