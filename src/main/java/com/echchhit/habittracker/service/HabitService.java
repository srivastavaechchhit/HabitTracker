package com.echchhit.habittracker.service;

import com.echchhit.habittracker.database.DatabaseManager;
import com.echchhit.habittracker.model.Habit;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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

    public static List<Habit> getAllHabits() {
        List<Habit> habits = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM habits")) {
            while (rs.next()) {
                habits.add(new Habit(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("created_at")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return habits;
    }

    public static Map<Integer, String> getAllHabitsWithIds() {
        String sql = "SELECT id, name FROM habits WHERE status = 'ACTIVE' ORDER BY id";
        Map<Integer, String> map = new HashMap<>();
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("id"), rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
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