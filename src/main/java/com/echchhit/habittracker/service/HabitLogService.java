package com.echchhit.habittracker.service;

import com.echchhit.habittracker.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

public class HabitLogService {

    public static void markCompleted(int habitId) {
        String sql = """
            INSERT INTO habit_logs(habit_id, date, completed)
            VALUES (?, ?, 1)
            ON CONFLICT(habit_id, date)
            DO UPDATE SET completed = 1
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, habitId);
            ps.setString(2, LocalDate.now().toString());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getCurrentStreak(int habitId) {
        String sql = """
            SELECT date FROM habit_logs
            WHERE habit_id = ? AND completed = 1
            ORDER BY date DESC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, habitId);
            ResultSet rs = ps.executeQuery();

            int streak = 0;
            LocalDate today = LocalDate.now();

            while (rs.next()) {
                LocalDate date = LocalDate.parse(rs.getString("date"));
                if (date.equals(today.minusDays(streak))) {
                    streak++;
                } else {
                    break;
                }
            }
            return streak;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getMonthlyCompletionPercent(int habitId, YearMonth month) {
        String sql = """
            SELECT COUNT(*) FROM habit_logs
            WHERE habit_id = ?
            AND date LIKE ?
            AND completed = 1
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, habitId);
            ps.setString(2, month.toString() + "%");

            ResultSet rs = ps.executeQuery();
            int completedDays = rs.getInt(1);
            int totalDays = month.lengthOfMonth();

            return (completedDays * 100.0) / totalDays;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Set<LocalDate> getCompletedDatesForMonth(int habitId, YearMonth month) {
        Set<LocalDate> completed = new HashSet<>();

        String sql = """
            SELECT date FROM habit_logs
            WHERE habit_id = ?
            AND date LIKE ?
            AND completed = 1
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, habitId);
            ps.setString(2, month.toString() + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                completed.add(LocalDate.parse(rs.getString("date")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return completed;
    }


    public static boolean missedToday(int habitId) {
        String sql = """
        SELECT COUNT(*) FROM habit_logs
        WHERE habit_id = ? AND date = ? AND completed = 1
    """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, habitId);
            ps.setString(2, java.time.LocalDate.now().toString());
            var rs = ps.executeQuery();
            return rs.getInt(1) == 0;

        } catch (Exception e) {
            return true;
        }
    }

    public static int getCompletedCountToday() {

        String sql = """
        SELECT COUNT(*)
        FROM habit_logs
        WHERE date = ? AND completed = 1
    """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, java.time.LocalDate.now().toString());
            var rs = ps.executeQuery();
            return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public static int getCompletedCountByDate(java.time.LocalDate date) {

        String sql = """
        SELECT COUNT(*)
        FROM habit_logs
        WHERE date = ? AND completed = 1
    """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, date.toString());
            var rs = ps.executeQuery();
            return rs.getInt(1);

        } catch (Exception e) {
            return 0;
        }
    }
}
