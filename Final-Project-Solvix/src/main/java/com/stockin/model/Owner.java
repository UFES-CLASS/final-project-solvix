package com.stockin.model;

public class Owner extends User {

    public Owner() {
        super();
    }

    public Owner(int userId, String username, String password,
                 String role, boolean isActive) {

        super(userId, username, password, role, isActive);
    }
}