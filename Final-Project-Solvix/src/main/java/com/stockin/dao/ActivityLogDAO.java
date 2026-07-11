package com.stockin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.stockin.config.DatabaseConnection;
import com.stockin.model.ActivityLog;

/**
 * DAO untuk tabel activity_log, sumber data bagi panel
 * "Recent Activity Log" pada Dashboard.
 */
public class ActivityLogDAO {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Mencatat satu aktivitas baru dengan timestamp saat ini.
     */
    public boolean log(String actor, String action, String type) {

        String sql = "INSERT INTO activity_log(actor, action, type, activityDate, activityTime) "
                + "VALUES(?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, actor);
            ps.setString(2, action);
            ps.setString(3, type);
            ps.setString(4, LocalDate.now().toString());
            ps.setString(5, LocalTime.now().format(TIME_FORMAT));

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    /**
     * Mengambil N aktivitas paling baru, diurutkan dari yang paling
     * terakhir terjadi.
     */
    public List<ActivityLog> getRecent(int limit) {

        List<ActivityLog> list = new ArrayList<>();

        String sql = "SELECT * FROM activity_log ORDER BY activityId DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(mapRow(rs));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    private ActivityLog mapRow(ResultSet rs) throws Exception {

        ActivityLog log = new ActivityLog();

        log.setActivityId(rs.getInt("activityId"));
        log.setActor(rs.getString("actor"));
        log.setAction(rs.getString("action"));
        log.setType(rs.getString("type"));
        log.setActivityDate(rs.getString("activityDate"));
        log.setActivityTime(rs.getString("activityTime"));

        return log;

    }

}
