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

    /* =====================================================
       CORE WRITE OPERATIONS
       ===================================================== */

    // Mark habit as completed for TODAY
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

    // Mark habit as MISSED (❌) for a specific date
    public static void markMissed(int habitId, LocalDate date) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* =====================================================
       PHASE 5 — DISCIPLINE LOGIC
       ===================================================== */

    // Auto-cross yesterday if user missed it
    public static void autoCrossIfMissed(int habitId, LocalDate date) {

        String sql = """
            SELECT completed FROM habit_logs
            WHERE habit_id = ? AND date = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, habitId);
            ps.setString(2, date.toString());

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                // No entry → missed
                markMissed(habitId, date);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check if habit was missed TODAY
    public static boolean missedToday(int habitId) {
        String sql = """
            SELECT COUNT(*) FROM habit_logs
            WHERE habit_id = ? AND date = ? AND completed = 1
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, habitId);
            ps.setString(2, LocalDate.now().toString());
            ResultSet rs = ps.executeQuery();

            return rs.getInt(1) == 0;

        } catch (Exception e) {
            return true;
        }
    }

    /* =====================================================
       STREAKS & ANALYTICS
       ===================================================== */

    // Current streak (consecutive days completed)
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

    // Monthly completion percentage
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

    /* =====================================================
       CALENDAR & HEATMAP SUPPORT
       ===================================================== */

    // All completed dates in a month
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

    /* =====================================================
       DAILY TOTALS (BOTTOM ROW, DASHBOARD)
       ===================================================== */

    // Total habits completed today
    public static int getCompletedCountToday() {
        String sql = """
            SELECT COUNT(*) FROM habit_logs
            WHERE date = ? AND completed = 1
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, LocalDate.now().toString());
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Total habits completed on a given date
    public static int getCompletedCountByDate(LocalDate date) {
        String sql = """
            SELECT COUNT(*) FROM habit_logs
            WHERE date = ? AND completed = 1
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date.toString());
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1);

        } catch (Exception e) {
            return 0;
        }
    }
}
