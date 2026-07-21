package com.stockin.model;

/**
 * Akun dengan peran Owner (pemilik usaha). Owner punya akses penuh,
 * termasuk mengatur minimum stock dan melihat Financial Report.
 */
public class Owner extends User {

    public Owner() {
        super();
        this.role = "OWNER";
    }

    public Owner(int userId, String username, String password, boolean isActive) {
        super(userId, username, password, "OWNER", isActive);
    }

    @Override
    public boolean isOwner() {
        return true;
    }

    @Override
    public boolean isStaff() {
        return false;
    }

    @Override
    public boolean canManageMinimumStock() {
        return true;
    }

    @Override
    public boolean canViewFinancialReport() {
        return true;
    }

    @Override
    public boolean canManageProducts() {
        return true;
    }

}
