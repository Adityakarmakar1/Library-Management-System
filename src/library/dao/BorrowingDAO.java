package library.dao;

import library.model.Borrowing;
import library.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowingDAO {

    public List<Borrowing> getAllBorrowings() throws SQLException {
        List<Borrowing> list = new ArrayList<>();
        String sql =
            "SELECT br.*, b.title as book_title, m.name as member_name " +
            "FROM borrowings br " +
            "JOIN books b ON br.book_id = b.id " +
            "JOIN members m ON br.member_id = m.id " +
            "ORDER BY br.borrow_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Borrowing> getActiveBorrowings() throws SQLException {
        List<Borrowing> list = new ArrayList<>();
        String sql =
            "SELECT br.*, b.title as book_title, m.name as member_name " +
            "FROM borrowings br " +
            "JOIN books b ON br.book_id = b.id " +
            "JOIN members m ON br.member_id = m.id " +
            "WHERE br.status = 'Borrowed' " +
            "ORDER BY br.due_date ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Borrowing> getOverdueBorrowings() throws SQLException {
        List<Borrowing> list = new ArrayList<>();
        String sql =
            "SELECT br.*, b.title as book_title, m.name as member_name " +
            "FROM borrowings br " +
            "JOIN books b ON br.book_id = b.id " +
            "JOIN members m ON br.member_id = m.id " +
            "WHERE br.status = 'Borrowed' AND br.due_date < date('now') " +
            "ORDER BY br.due_date ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void borrowBook(int bookId, int memberId, String dueDate) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        try {
            conn.setAutoCommit(false);

            String insertSQL = "INSERT INTO borrowings (book_id, member_id, due_date, status) VALUES (?, ?, ?, 'Borrowed')";
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setInt(1, bookId);
                ps.setInt(2, memberId);
                ps.setString(3, dueDate);
                ps.executeUpdate();
            }

            String updateSQL = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0";
            try (PreparedStatement ps = conn.prepareStatement(updateSQL)) {
                ps.setInt(1, bookId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    throw new SQLException("No available copies for this book.");
                }
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void returnBook(int borrowingId, double fineAmount) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        try {
            conn.setAutoCommit(false);

            String bookIdSQL = "SELECT book_id FROM borrowings WHERE id = ?";
            int bookId;
            try (PreparedStatement ps = conn.prepareStatement(bookIdSQL)) {
                ps.setInt(1, borrowingId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new SQLException("Borrowing record not found.");
                bookId = rs.getInt("book_id");
            }

            String updateBorrow = "UPDATE borrowings SET return_date = date('now'), fine_amount = ?, status = 'Returned' WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateBorrow)) {
                ps.setDouble(1, fineAmount);
                ps.setInt(2, borrowingId);
                ps.executeUpdate();
            }

            String updateBook = "UPDATE books SET available_copies = available_copies + 1 WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateBook)) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public int getActiveBorrowingsCount() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM borrowings WHERE status='Borrowed'")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getOverdueCount() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM borrowings WHERE status='Borrowed' AND due_date < date('now')")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Borrowing mapRow(ResultSet rs) throws SQLException {
        Borrowing b = new Borrowing();
        b.setId(rs.getInt("id"));
        b.setBookId(rs.getInt("book_id"));
        b.setMemberId(rs.getInt("member_id"));
        b.setBookTitle(rs.getString("book_title"));
        b.setMemberName(rs.getString("member_name"));
        b.setBorrowDate(rs.getString("borrow_date"));
        b.setDueDate(rs.getString("due_date"));
        b.setReturnDate(rs.getString("return_date"));
        b.setFineAmount(rs.getDouble("fine_amount"));
        b.setStatus(rs.getString("status"));
        return b;
    }
}
