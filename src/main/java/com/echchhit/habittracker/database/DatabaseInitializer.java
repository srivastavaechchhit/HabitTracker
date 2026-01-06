package com.echchhit.habittracker.database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Habits Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS habits (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT DEFAULT 'General',
                    created_at TEXT NOT NULL,
                    status TEXT DEFAULT 'ACTIVE'
                );
            """);

            // 2. Logs Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS habit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    habit_id INTEGER NOT NULL,
                    date TEXT NOT NULL,
                    completed INTEGER NOT NULL,
                    UNIQUE(habit_id, date)
                );
            """);

            // 3. User Stats Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_stats (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    total_xp INTEGER DEFAULT 0,
                    current_level INTEGER DEFAULT 1
                );
            """);

            // 4. Jap Logs Table (New)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS jap_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    count INTEGER NOT NULL,
                    date TEXT NOT NULL UNIQUE
                );
            """);

            stmt.execute("INSERT OR IGNORE INTO user_stats (id, total_xp, current_level) VALUES (1, 0, 1)");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}