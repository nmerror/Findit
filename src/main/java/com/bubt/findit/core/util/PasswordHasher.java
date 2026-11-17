package com.bubt.findit.core.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing for Segment 1.
 *
 * <p>Uses PBKDF2WithHmacSHA256 (part of the JDK - no extra library needed),
 * a random 16-byte salt per user and 120,000 iterations.
 *
 * <p>Never store the plain password. Never store SHA-256 of the password
 * either - it is too fast to brute force.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /** The salt + hash pair that gets stored in the users table. */
    public static final class HashedPassword {

        private final String hash;
        private final String salt;

        public HashedPassword(String hash, String salt) {
            this.hash = hash;
            this.salt = salt;
        }

        public String getHash() {
            return hash;
        }

        public String getSalt() {
            return salt;
        }
    }

    public static HashedPassword hash(char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hash = hash(password, saltBase64);
        return new HashedPassword(hash, saltBase64);
    }

    public static String hash(char[] password, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (Exception ex) {
            throw new IllegalStateException("Password hashing failed", ex);
        }
    }

    /**
     * Constant-time check. Uses a fixed-length comparison so that the answer
     * does not leak how much of the hash matched.
     */
    public static boolean verify(char[] password, String saltBase64, String expectedHash) {
        if (password == null || saltBase64 == null || expectedHash == null) {
            return false;
        }
        String actual = hash(password, saltBase64);
        return MessageDigest.isEqual(actual.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                expectedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
