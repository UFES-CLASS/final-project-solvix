package com.stockin.dao;

import com.stockin.config.DatabaseConnection;
import com.stockin.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    /**
     * Mengecek username, password, dan role terhadap tabel users.
     * Mengembalikan objek User jika cocok dan akun aktif, atau null jika tidak.
     */
    public User authenticate(String username, String password, String role) {

        String sql = "SELECT * FROM users WHERE username = ? AND password = ? "
                + "AND role = ? AND isActive = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

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
