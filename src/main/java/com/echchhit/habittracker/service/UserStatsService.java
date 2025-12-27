package com.echchhit.habittracker.service;

import com.echchhit.habittracker.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserStatsService {

    // XP per habit completed
    private static final int XP_PER_HABIT = 10;
    // XP needed to reach next level (Simple formula: Level * 100)
    private static final int XP_BASE_THRESHOLD = 100;

    public static void addXp(int amount) {
        adjustXp(amount);
    }

    public static void removeXp(int amount) {
        adjustXp(-amount);
    }

    private static void adjustXp(int amount) {
        int[] current = getUserStats();
        int totalXp = Math.max(0, current[0] + amount); // Prevent negative XP
        int level = (totalXp / XP_BASE_THRESHOLD) + 1;

        updateStats(totalXp, level);
    }

    public static int[] getUserStats() {
        String sql = "SELECT total_xp, current_level FROM user_stats WHERE id = 1";
        try (Connection conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return new int[]{ rs.getInt("total_xp"), rs.getInt("current_level") };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{0, 1}; // Default
    }

    private static void updateStats(int xp, int level) {
        String sql = "UPDATE user_stats SET total_xp = ?, current_level = ? WHERE id = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, xp);
            ps.setInt(2, level);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getProgressToNextLevel() {
        int[] stats = getUserStats();
        int xp = stats[0];
        int level = stats[1];

        int currentLevelBaseXp = (level - 1) * XP_BASE_THRESHOLD;
        int xpInCurrentLevel = xp - currentLevelBaseXp;

        return (double) xpInCurrentLevel / XP_BASE_THRESHOLD;
    }
}