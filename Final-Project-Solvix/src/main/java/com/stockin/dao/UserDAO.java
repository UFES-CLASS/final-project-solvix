package com.stockin.dao;

import com.stockin.config.DatabaseConnection;
import com.stockin.model.User;
import com.stockin.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    /**
     * Mengecek username, password, dan role terhadap tabel users.
     * Password TIDAK dibandingkan lewat query SQL (karena kolomnya berisi
     * hash, bukan plaintext) - query hanya mengambil user berdasarkan
     * username+role+status aktif, lalu hash-nya diverifikasi di sisi Java
     * memakai PasswordUtil. Mengembalikan objek User jika cocok, atau null
     * jika tidak.
     */
    public User authenticate(String username, String password, String role) {

        String sql = "SELECT * FROM users WHERE username = ? "
                + "AND role = ? AND isActive = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, role);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = mapRow(rs);

                    if (PasswordUtil.verify(password, user.getPassword())) {
                        return user;
                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Mengganti password user dengan yang baru (di-hash otomatis sebelum
     * disimpan).
     */
    public boolean updatePassword(int userId, String newPlainPassword) {

        String sql = "UPDATE users SET password = ? WHERE userId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PasswordUtil.hash(newPlainPassword));
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public User findByUsername(String username) {

        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapRow(rs);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    private User mapRow(ResultSet rs) throws Exception {

        User user = new User();

        user.setUserId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getInt("isActive") == 1);

        return user;

    }

}
