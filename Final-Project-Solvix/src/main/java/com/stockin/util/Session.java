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

}
