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

    public static void markCompleted(int habitId, LocalDate date) {
        String sql = """
            INSERT INTO habit_logs(habit_id, date, completed)
            VALUES (?, ?, 1)
            ON CONFLICT(habit_id, date)
            DO UPDATE SET completed = 1
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ps.setString(2, date.toString());
            int rows = ps.executeUpdate();
//            if (rows > 0 && date.equals(LocalDate.now())) {
//                UserStatsService.addXp(10);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void markMissed(int habitId, LocalDate date) {
        boolean wasCompleted = isDateCompleted(habitId, date);
        String sql = """
            INSERT INTO habit_logs(habit_id, date, completed)
            VALUES (?, ?, 0)
            ON CONFLICT(habit_id, date)
            DO UPDATE SET completed = 0
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ps.setString(2, date.toString());
            ps.executeUpdate();
//            if (wasCompleted && date.equals(LocalDate.now())) {
//                UserStatsService.removeXp(10);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isDateCompleted(int habitId, LocalDate date) {
        String sql = "SELECT completed FROM habit_logs WHERE habit_id = ? AND date = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ps.setString(2, date.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt("completed") == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public static void autoCrossIfMissed(int habitId, LocalDate date) {
        String sql = "SELECT completed FROM habit_logs WHERE habit_id = ? AND date = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ps.setString(2, date.toString());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                markMissed(habitId, date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Set<LocalDate> getCompletedDatesForMonth(int habitId, YearMonth month) {
        Set<LocalDate> completed = new HashSet<>();
        String sql = "SELECT date FROM habit_logs WHERE habit_id = ? AND date LIKE ? AND completed = 1";
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

    public static int getCompletedCountByDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM habit_logs WHERE date = ? AND completed = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getCompletedCountToday() {
        return getCompletedCountByDate(LocalDate.now());
    }
}