package com.stockin.model;

/**
 * Akun dengan peran Staff (karyawan operasional). Staff punya akses
 * terbatas dibandingkan Owner: tidak boleh mengubah minimum stock dan
 * tidak boleh membuka Financial Report.
 */
public class Staff extends User {

    public Staff() {
        super();
        this.role = "STAFF";
    }

    public Staff(int userId, String username, String password, boolean isActive) {
        super(userId, username, password, "STAFF", isActive);
    }

    @Override
    public boolean isOwner() {
        return false;
    }

    @Override
    public boolean isStaff() {
        return true;
    }

    @Override
    public boolean canManageMinimumStock() {
        return false;
    }

    @Override
    public boolean canViewFinancialReport() {
        return false;
    }

    @Override
    public boolean canManageProducts() {
        return false;
    }

}
