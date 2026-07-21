package com.stockin.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility hashing password memakai PBKDF2WithHmacSHA256 (bawaan JDK, tidak
 * perlu dependency tambahan seperti BCrypt). Password TIDAK PERNAH disimpan
 * plaintext lagi; format yang disimpan di kolom users.password adalah:
 * <p>
 * {@code PBKDF2:<jumlah iterasi>:<salt base64>:<hash base64>}
 * <p>
 * Setiap password punya salt acak sendiri, jadi dua user dengan password
 * sama akan tetap punya hash yang berbeda.
 */
public class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "PBKDF2";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * Membuat hash baru (dengan salt acak) dari password plaintext.
     * Dipakai saat membuat akun baru, ganti password, atau memigrasikan
     * password lama yang masih plaintext.
     */
    public static String hash(String plainPassword) {

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);

        return PREFIX + ":" + ITERATIONS + ":"
                + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);

    }

    /**
     * Mengecek apakah password plaintext yang diinput user cocok dengan
     * hash yang tersimpan di database. Mengembalikan false kalau nilai yang
     * tersimpan bukan format hash PBKDF2 kita (misalnya data lama yang
     * belum sempat dimigrasi).
     */
    public static boolean verify(String plainPassword, String storedValue) {

        if (plainPassword == null || !isHashed(storedValue)) {
            return false;
        }

        String[] parts = storedValue.split(":");

        if (parts.length != 4) {
            return false;
        }

        try {

            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            byte[] actualHash = pbkdf2(plainPassword.toCharArray(), salt, iterations, expectedHash.length * 8);

            return slowEquals(expectedHash, actualHash);

        } catch (Exception e) {
            return false;
        }

    }

    /**
     * Mengecek apakah sebuah nilai di kolom password sudah dalam format
     * hash PBKDF2 kita, atau masih plaintext lama yang perlu dimigrasi.
     */
    public static boolean isHashed(String storedValue) {
        return storedValue != null && storedValue.startsWith(PREFIX + ":");
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {

        try {

            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to hash password", e);
        }

    }

    /**
     * Perbandingan byte array dengan waktu konstan (tidak berhenti di
     * karakter pertama yang beda), supaya tidak bocor informasi lewat
     * timing attack saat membandingkan hash.
     */
    private static boolean slowEquals(byte[] a, byte[] b) {

        int diff = a.length ^ b.length;

        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }

        return diff == 0;

    }

}
