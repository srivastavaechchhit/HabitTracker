package com.echchhit.habittracker.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:habittracker.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(DB_URL);
    }
}
