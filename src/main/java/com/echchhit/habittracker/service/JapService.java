package com.echchhit.habittracker.service;

import com.echchhit.habittracker.database.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JapService {
    public static void updateJapCount(int count) {
        String sql = """
            INSERT INTO jap_logs(count, date) VALUES(?, ?)
            ON CONFLICT(date) DO UPDATE SET count = ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, count);
            ps.setString(2, java.time.LocalDate.now().toString());
            ps.setInt(3, count);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static int getTodayCount() {
        String sql = "SELECT count FROM jap_logs WHERE date = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, java.time.LocalDate.now().toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    public static List<String> getAllJapLogs() {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT date, count FROM jap_logs ORDER BY date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // IMPORTANT: The " : " separator must match exactly what the Controller expects
                logs.add(rs.getString("date") + " : " + rs.getInt("count") + " Chants");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return logs;
    }

    // Add to JapService.java
    public static void deleteJapLog(String date) {
        String sql = "DELETE FROM jap_logs WHERE date = ?";
        try (Connection conn = com.echchhit.habittracker.database.DatabaseManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Add this method to JapService.java
    public static int getTotalChants() {
        String sql = "SELECT SUM(count) FROM jap_logs";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}