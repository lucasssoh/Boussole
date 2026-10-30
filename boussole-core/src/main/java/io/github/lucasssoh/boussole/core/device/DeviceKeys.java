package io.github.lucasssoh.boussole.core.device;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/** The secrets of pairing: a device key (256 bits), its hash, short codes, and IDs. */
public final class DeviceKeys {

    private static final SecureRandom RANDOM = new SecureRandom();

    private DeviceKeys() {
    }

    public static String newKey() {
        return token(32);
    }

    /** An ID that is not guessable either: a pending request is collected by it. */
    public static String newId() {
        return token(16);
    }

    /** Six digits, easy to type with a remote: {@code 382615}. */
    public static String newCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    public static String hash(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is part of every Java runtime", e);
        }
    }

    /** Compares in a time that does not depend on where the two codes differ. */
    static boolean sameCode(String expected, String given) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), given.getBytes(StandardCharsets.UTF_8));
    }

    private static String token(int bytes) {
        byte[] random = new byte[bytes];
        RANDOM.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }
}
