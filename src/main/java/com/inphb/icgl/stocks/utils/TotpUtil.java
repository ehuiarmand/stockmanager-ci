package com.inphb.icgl.stocks.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

public final class TotpUtil {
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_SIZE = 20;
    private static final int TIME_STEP_SECONDS = 30;

    private TotpUtil() {
    }

    public static String generateSecret() {
        byte[] buffer = new byte[SECRET_SIZE];
        new SecureRandom().nextBytes(buffer);
        return encodeBase32(buffer);
    }

    public static boolean verifyCode(String secret, String code) {
        if (secret == null || secret.isBlank() || code == null || !code.matches("\\d{6}")) {
            return false;
        }
        long timeWindow = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS;
        for (int offset = -1; offset <= 1; offset++) {
            if (generateCode(secret, timeWindow + offset).equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static String buildOtpAuthUrl(String applicationName, String login, String secret) {
        String issuer = urlEncode(applicationName);
        String account = urlEncode(applicationName + ":" + login);
        return "otpauth://totp/" + account + "?secret=" + secret + "&issuer=" + issuer;
    }

    private static String generateCode(String secret, long timeWindow) {
        byte[] secretBytes = decodeBase32(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(timeWindow).array();
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % 1_000_000;
            return String.format(Locale.ROOT, "%06d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Impossible de generer le code TOTP.", e);
        }
    }

    private static String encodeBase32(byte[] data) {
        StringBuilder builder = new StringBuilder();
        int current = 0;
        int bitsRemaining = 0;
        for (byte value : data) {
            current = (current << 8) | (value & 0xFF);
            bitsRemaining += 8;
            while (bitsRemaining >= 5) {
                builder.append(BASE32_ALPHABET.charAt((current >> (bitsRemaining - 5)) & 0x1F));
                bitsRemaining -= 5;
            }
        }
        if (bitsRemaining > 0) {
            builder.append(BASE32_ALPHABET.charAt((current << (5 - bitsRemaining)) & 0x1F));
        }
        return builder.toString();
    }

    private static byte[] decodeBase32(String value) {
        String normalized = value.replace("=", "").replace(" ", "").toUpperCase(Locale.ROOT);
        byte[] output = new byte[(normalized.length() * 5) / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;
        for (char c : normalized.toCharArray()) {
            int val = BASE32_ALPHABET.indexOf(c);
            if (val < 0) {
                throw new IllegalArgumentException("Cle 2FA invalide.");
            }
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                output[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return output;
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
