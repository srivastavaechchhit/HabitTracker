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
        String selectSql = "SELECT pages_read, total_pages FROM books WHERE id = ?";
        String updateBook = "UPDATE books SET pages_read = ?, status = ? WHERE id = ?";
        String logProgress = "INSERT INTO reading_logs(book_id, pages_read_today, date) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            int currentRead = 0;
            int totalPages = 0;

            // 1. Fetch current status
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, bookId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentRead = rs.getInt("pages_read");
                    totalPages = rs.getInt("total_pages");
                }
            }

            // 2. Calculate new total and cap it
            int newTotal = Math.min(currentRead + additionalPages, totalPages);
            int pagesActuallyLogged = newTotal - currentRead;
            String newStatus = (newTotal >= totalPages) ? "COMPLETED" : "READING";

            if (pagesActuallyLogged <= 0) return; // No progress to log

            // 3. Update Book
            try (PreparedStatement ps1 = conn.prepareStatement(updateBook)) {
                ps1.setInt(1, newTotal);
                ps1.setString(2, newStatus);
                ps1.setInt(3, bookId);
                ps1.executeUpdate();
            }

            // 4. Log Daily Progress
            try (PreparedStatement ps2 = conn.prepareStatement(logProgress)) {
                ps2.setInt(1, bookId);
                ps2.setInt(2, pagesActuallyLogged);
                ps2.setString(3, LocalDate.now().toString());
                ps2.executeUpdate();
            }
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

    // Add this method to the BookService class
    public static void deleteBook(int bookId) {
        String selectXp = "SELECT pages_read FROM books WHERE id = ?";
        String deleteLogs = "DELETE FROM reading_logs WHERE book_id = ?";
        String deleteBook = "DELETE FROM books WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Calculate XP to remove
                int xpToRemove = 0;
                try (PreparedStatement psXp = conn.prepareStatement(selectXp)) {
                    psXp.setInt(1, bookId);
                    ResultSet rs = psXp.executeQuery();
                    if (rs.next()) {
                        xpToRemove = rs.getInt("pages_read");
                    }
                }

                // 2. Delete Logs
                try (PreparedStatement ps1 = conn.prepareStatement(deleteLogs)) {
                    ps1.setInt(1, bookId);
                    ps1.executeUpdate();
                }

                // 3. Delete Book
                try (PreparedStatement ps2 = conn.prepareStatement(deleteBook)) {
                    ps2.setInt(1, bookId);
                    ps2.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}