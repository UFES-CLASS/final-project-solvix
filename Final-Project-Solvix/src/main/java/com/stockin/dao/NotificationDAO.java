package com.stockin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.stockin.config.DatabaseConnection;
import com.stockin.model.Notification;

public class NotificationDAO {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean addNotification(Notification notification) {

        String sql = "INSERT INTO notifications(materialId, message, createdAt, isRead) VALUES(?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, notification.getMaterialId());
            ps.setString(2, notification.getMessage());
            ps.setString(3, notification.getCreatedAt());
            ps.setInt(4, notification.isRead() ? 1 : 0);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        notification.setNotificationId(keys.getInt(1));
                    }
                }

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public void createLowStockNotificationIfNeeded(int materialId, String materialName, int stock, int minimumStock) {

        String checkSql = "SELECT COUNT(*) AS total FROM notifications "
                + "WHERE materialId = ? AND isRead = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {

            ps.setInt(1, materialId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next() && rs.getInt("Total") > 0) {
                    return; // sudah ada notifikasi belum dibaca, tidak perlu duplikat
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Notification notification = new Notification();
        notification.setMaterialId(materialId);
        notification.setMessage("Stock of " + materialName + " is low (" + stock
                + " remaining, minimum " + minimumStock + ")");
        notification.setCreatedAt(LocalDateTime.now().format(FORMAT));
        notification.setRead(false);

        addNotification(notification);

    }

    public boolean markAsRead(int notificationId) {

        String sql = "UPDATE notifications SET isRead=1 WHERE notificationId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean markAllAsRead() {

        String sql = "UPDATE notifications SET isRead=1 WHERE isRead=0";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            return stmt.executeUpdate(sql) >= 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean deleteNotification(int notificationId) {

        String sql = "DELETE FROM notifications WHERE notificationId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public List<Notification> getAllNotifications() {

        List<Notification> list = new ArrayList<>();

        String sql = "SELECT n.*, m.materialName FROM notifications n "
                + "LEFT JOIN materials m ON n.materialId = m.materialId "
                + "ORDER BY n.createdAt DESC, n.notificationId DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    public int countUnread() {

        String sql = "SELECT COUNT(*) AS total FROM notifications WHERE isRead=0";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }

    private Notification mapRow(ResultSet rs) throws Exception {

        Notification notification = new Notification();

        notification.setNotificationId(rs.getInt("notificationId"));
        notification.setMaterialId(rs.getInt("materialId"));
        notification.setMessage(rs.getString("message"));
        notification.setCreatedAt(rs.getString("createdAt"));
        notification.setRead(rs.getInt("isRead") == 1);
        notification.setMaterialName(rs.getString("materialName"));

        return notification;

    }

}
