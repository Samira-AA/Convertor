package com.sumax.pdfa.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class CredentialHash {
    private static final SecureRandom RANDOM = new SecureRandom();

    private CredentialHash() {
    }

    public static String sha256(String value) {
        return HexFormat.of().formatHex(digest().digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no disponible", exception);
        }
    }

    public static String newApiKey() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return "sxp_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
