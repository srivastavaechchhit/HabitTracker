package com.echchhit.habittracker.service;

import com.echchhit.habittracker.database.DatabaseManager;
import com.echchhit.habittracker.model.Book;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    public static void addBook(String title, String author, int totalPages) {
        String sql = "INSERT INTO books(title, author, total_pages) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setInt(3, totalPages);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void updateReadingProgress(int bookId, int additionalPages) {
        String updateBook = "UPDATE books SET pages_read = pages_read + ? WHERE id = ?";
        String logProgress = "INSERT INTO reading_logs(book_id, pages_read_today, date) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(updateBook)) {
                ps1.setInt(1, additionalPages);
                ps1.setInt(2, bookId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(logProgress)) {
                ps2.setInt(1, bookId);
                ps2.setInt(2, additionalPages);
                ps2.setString(3, LocalDate.now().toString());
                ps2.executeUpdate();
            }
            UserStatsService.addXp(additionalPages); // Award XP per page read
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Book(rs.getInt("id"), rs.getString("title"),
                        rs.getString("author"), rs.getInt("total_pages"), rs.getInt("pages_read")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return books;
    }

    public static int getFinishedBooksCount() {
        String sql = "SELECT COUNT(*) FROM books WHERE pages_read >= total_pages";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }
}