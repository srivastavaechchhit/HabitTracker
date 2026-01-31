package com.echchhit.habittracker.service;

import com.echchhit.habittracker.database.DatabaseManager;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HabitService {

    public static void addHabit(String name) {
        String sql = "INSERT INTO habits(name, created_at, status) VALUES(?, ?, 'ACTIVE')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, LocalDate.now().toString());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ... inside HabitService class ...

    public static void deleteHabit(int id) {
        String sqlLogs = "DELETE FROM habit_logs WHERE habit_id = ?";
        String sqlHabit = "DELETE FROM habits WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            // 1. Delete associated logs first
            try (PreparedStatement ps = conn.prepareStatement(sqlLogs)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            // 2. Delete the habit itself
            try (PreparedStatement ps = conn.prepareStatement(sqlHabit)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateHabitName(int id, String newName) {
        String sql = "UPDATE habits SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. UPDATE: Fetch by 'sort_order' and use LinkedHashMap to keep that order
    public static Map<Integer, String> getAllHabitsWithIds() {
        String sql = "SELECT id, name FROM habits WHERE status = 'ACTIVE' ORDER BY sort_order ASC";
        Map<Integer, String> map = new LinkedHashMap<>(); // <--- Must be LinkedHashMap
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("id"), rs.getString("name"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // 2. NEW: Save the new order to the database
    public static void updateHabitOrder(List<Integer> habitIds) {
        String sql = "UPDATE habits SET sort_order = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Speed up with batching
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < habitIds.size(); i++) {
                    ps.setInt(1, i + 1);      // Order starts at 1
                    ps.setInt(2, habitIds.get(i)); // The Habit ID
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void markHabitCompleted(int habitId) {
        String sql = "UPDATE habits SET status = 'COMPLETED' WHERE id = ?";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllHabitsAndLogs() {
        try (Connection con = DatabaseManager.getConnection();
             Statement st = con.createStatement()) {
            st.execute("DELETE FROM habit_logs");
            st.execute("DELETE FROM habits");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}