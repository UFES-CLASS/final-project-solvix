package com.stockin.model;

public class Staff extends User {

    public Staff() {
        super();
    }

    public Staff(int userId, String username, String password,
                 String role, boolean isActive) {

        super(userId, username, password, role, isActive);
    }
}