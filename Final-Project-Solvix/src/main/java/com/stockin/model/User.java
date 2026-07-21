package com.stockin.model;

/**
 * Kelas dasar (abstract) untuk akun pengguna STOCKIN.
 * Sebelumnya Owner dan Staff sama-sama direpresentasikan oleh satu
 * class User yang sama dan dibedakan lewat pengecekan String role
 * ("OWNER"/"STAFF") tersebar di berbagai Controller. Sekarang Owner dan
 * Staff masing-masing punya class sendiri (lihat {@link Owner} dan
 * {@link Staff}) yang mewarisi class ini, sehingga hak akses tiap peran
 * ditentukan lewat polymorphism (override method), bukan lewat
 * perbandingan String yang mudah salah ketik / tidak konsisten.
 */
public abstract class User {

    protected int userId;
    protected String username;
    protected String password;
    protected String role;
    protected boolean isActive;

    public User() {
    }

    public User(int userId, String username, String password, String role, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
    }

    /**
     * Factory method: membuat instance Owner atau Staff yang tepat
     * berdasarkan nilai kolom "role" yang tersimpan di database.
     * Dipakai oleh UserDAO supaya seluruh aplikasi selalu bekerja dengan
     * subclass yang benar, bukan dengan class User polos.
     */
    public static User createByRole(String role) {

        if (role != null && role.equalsIgnoreCase("OWNER")) {
            return new Owner();
        }

        return new Staff();

    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public abstract boolean isOwner();

    public abstract boolean isStaff();

    /**
     * Owner boleh menentukan/mengubah ambang batas minimum stock material,
     * Staff tidak. Menggantikan pengecekan Session.isStaff() yang tadinya
     * tersebar di Controller.
     */
    public abstract boolean canManageMinimumStock();

    /**
     * Hanya Owner yang boleh membuka halaman Financial Report.
     */
    public abstract boolean canViewFinancialReport();

    /**
     * Owner boleh menambah produk baru dan menghapus produk dari katalog.
     * Staff tetap boleh membuka & meng-update produk yang sudah ada
     * (termasuk mengatur Bill of Materials / resepnya), tapi tidak boleh
     * membuat produk baru dari nol atau menghapus produk.
     */
    public abstract boolean canManageProducts();

}
