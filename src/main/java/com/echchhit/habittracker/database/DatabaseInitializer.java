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
                    status TEXT DEFAULT 'ACTIVE',
                    sort_order INTEGER DEFAULT 0
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

            // 3. Jap Logs Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS jap_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    count INTEGER NOT NULL,
                    date TEXT NOT NULL UNIQUE
                );
            """);

            // 4. Books Table (New)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    author TEXT,
                    total_pages INTEGER NOT NULL,
                    pages_read INTEGER DEFAULT 0,
                    status TEXT DEFAULT 'READING'
                );
            """);

            // 5. Reading Logs Table (New)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reading_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    pages_read_today INTEGER NOT NULL,
                    date TEXT NOT NULL
                );
            """);

            try {
                stmt.execute("ALTER TABLE habits ADD COLUMN sort_order INTEGER DEFAULT 0");
            } catch (Exception ignored) { }

            stmt.execute("UPDATE habits SET sort_order = id WHERE sort_order = 0 OR sort_order IS NULL");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}