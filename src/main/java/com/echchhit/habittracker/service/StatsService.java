package com.echchhit.habittracker.service;

import com.echchhit.habittracker.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatsService {

    // In StatsService.java
    public static java.util.Map<String, Integer> getWeeklyCompletion() {
        java.util.Map<String, Integer> data = new java.util.LinkedHashMap<>();

        // Updated SQL to fetch from the start of the current month
        String sql = """
        SELECT date, COUNT(*) AS completed
        FROM habit_logs
        WHERE completed = 1
        AND date >= date('now', 'start of month')
        GROUP BY date
        ORDER BY date
    """;

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("date"), rs.getInt("completed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


    // Habit-wise total completions
    public static Map<String, Integer> getHabitCompletionCount() {
        Map<String, Integer> data = new LinkedHashMap<>();

        String sql = """
            SELECT h.name, COUNT(l.id) as cnt
            FROM habits h
            LEFT JOIN habit_logs l
            ON h.id = l.habit_id AND l.completed = 1
            GROUP BY h.id
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.put(rs.getString("name"), rs.getInt("cnt"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
    public static int getBestStreak() {

        String sql = """
        SELECT habit_id, COUNT(*) AS streak
        FROM habit_logs
        WHERE completed = 1
        GROUP BY habit_id
        ORDER BY streak DESC
        LIMIT 1
    """;

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("streak");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

}
