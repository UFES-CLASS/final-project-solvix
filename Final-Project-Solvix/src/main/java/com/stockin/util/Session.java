package com.stockin.util;

import com.stockin.model.User;


public class Session {

    private static User currentUser;

    private Session() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isOwner() {
        return currentUser != null && currentUser.isOwner();
    }

    public static boolean isStaff() {
        return currentUser != null && currentUser.isStaff();
    }

    public static void clear() {
        currentUser = null;
    }

    /**
     * Nama user yang sedang login dalam format Kapital-di-awal, contoh
     * "owner" -> "Owner". Dipakai sebagai label aktor pada Recent Activity
     * Log di Dashboard. Mengembalikan "User" kalau tidak ada sesi aktif.
     */
    public static String getCurrentUserLabel() {

        if (currentUser == null || currentUser.getUsername() == null || currentUser.getUsername().isEmpty()) {
            return "User";
        }

        String username = currentUser.getUsername();

        return Character.toUpperCase(username.charAt(0)) + username.substring(1).toLowerCase();

    }

}
