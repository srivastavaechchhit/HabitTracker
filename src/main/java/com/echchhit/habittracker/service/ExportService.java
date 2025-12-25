package com.echchhit.habittracker.service;

import com.echchhit.habittracker.database.DatabaseManager;

import java.io.FileWriter;
import java.sql.ResultSet;

public class ExportService {

    public static void exportToCSV(String path) {
        String sql = """
            SELECT h.name, l.date, l.completed
            FROM habits h
            LEFT JOIN habit_logs l ON h.id = l.habit_id
        """;

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql);
             FileWriter fw = new FileWriter(path)) {

            fw.write("Habit,Date,Completed\n");

            while (rs.next()) {
                fw.write(
                        rs.getString(1) + "," +
                                rs.getString(2) + "," +
                                rs.getInt(3) + "\n"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
