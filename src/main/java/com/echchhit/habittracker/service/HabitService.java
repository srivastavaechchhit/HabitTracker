package com.echchhit.habittracker.service;

import com.echchhit.habittracker.database.DatabaseManager;
import com.echchhit.habittracker.model.Habit;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitService {

    public static void addHabit(String name) {
        String sql = "INSERT INTO habits(name, created_at) VALUES(?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, LocalDate.now().toString());
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
}
