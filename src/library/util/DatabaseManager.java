package library.util;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC Driver not found", e);
            }
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Books table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS books (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    title TEXT NOT NULL," +
                "    author TEXT NOT NULL," +
                "    isbn TEXT UNIQUE," +
                "    genre TEXT," +
                "    publisher TEXT," +
                "    year INTEGER," +
                "    total_copies INTEGER DEFAULT 1," +
                "    available_copies INTEGER DEFAULT 1," +
                "    description TEXT," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Members table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS members (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    name TEXT NOT NULL," +
                "    email TEXT UNIQUE," +
                "    phone TEXT," +
                "    address TEXT," +
                "    membership_type TEXT DEFAULT 'Standard'," +
                "    membership_date DATE DEFAULT CURRENT_DATE," +
                "    expiry_date DATE," +
                "    status TEXT DEFAULT 'Active'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Borrowings table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS borrowings (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    book_id INTEGER NOT NULL," +
                "    member_id INTEGER NOT NULL," +
                "    borrow_date DATE DEFAULT CURRENT_DATE," +
                "    due_date DATE," +
                "    return_date DATE," +
                "    fine_amount REAL DEFAULT 0.0," +
                "    status TEXT DEFAULT 'Borrowed'," +
                "    FOREIGN KEY (book_id) REFERENCES books(id)," +
                "    FOREIGN KEY (member_id) REFERENCES members(id)" +
                ")"
            );

            // Insert sample data if tables are empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            if (rs.next() && rs.getInt(1) == 0) {
                insertSampleData(conn);
            }

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void insertSampleData(Connection conn) throws SQLException {
        String bookSQL = "INSERT INTO books (title, author, isbn, genre, publisher, year, total_copies, available_copies, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(bookSQL)) {
            Object[][] books = {
                {"The Great Gatsby", "F. Scott Fitzgerald", "978-0-7432-7356-5", "Fiction", "Scribner", 1925, 3, 3, "A novel of the Jazz Age"},
                {"To Kill a Mockingbird", "Harper Lee", "978-0-06-112008-4", "Fiction", "J. B. Lippincott & Co.", 1960, 2, 2, "A story of racial injustice"},
                {"1984", "George Orwell", "978-0-452-28423-4", "Dystopian", "Secker & Warburg", 1949, 4, 4, "A dystopian social science fiction novel"},
                {"Clean Code", "Robert C. Martin", "978-0-13-235088-4", "Technology", "Prentice Hall", 2008, 2, 2, "A handbook of agile software craftsmanship"},
                {"The Pragmatic Programmer", "David Thomas", "978-0-13-595705-9", "Technology", "Addison-Wesley", 1999, 2, 2, "Your journey to mastery"},
                {"Dune", "Frank Herbert", "978-0-441-17271-9", "Science Fiction", "Chilton Books", 1965, 3, 3, "A science fiction epic"},
                {"The Alchemist", "Paulo Coelho", "978-0-06-112241-5", "Fiction", "HarperOne", 1988, 2, 2, "A philosophical novel"},
                {"Sapiens", "Yuval Noah Harari", "978-0-06-231609-7", "Non-Fiction", "Harper", 2011, 3, 3, "A brief history of humankind"},
                {"Atomic Habits", "James Clear", "978-0-7352-1129-2", "Self-Help", "Avery", 2018, 4, 4, "An easy and proven way to build good habits"},
                {"The Design of Everyday Things", "Don Norman", "978-0-465-06710-7", "Design", "Basic Books", 2013, 2, 2, "A must-read for designers"}
            };
            for (Object[] book : books) {
                for (int i = 0; i < book.length; i++) ps.setObject(i + 1, book[i]);
                ps.addBatch();
            }
            ps.executeBatch();
        }

        String memberSQL = "INSERT INTO members (name, email, phone, address, membership_type, expiry_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(memberSQL)) {
            Object[][] members = {
                {"Alice Johnson", "alice@email.com", "555-0101", "123 Main St", "Premium", "2026-12-31", "Active"},
                {"Bob Smith", "bob@email.com", "555-0102", "456 Oak Ave", "Standard", "2026-06-30", "Active"},
                {"Carol Davis", "carol@email.com", "555-0103", "789 Pine Rd", "Standard", "2025-12-31", "Active"},
                {"David Wilson", "david@email.com", "555-0104", "321 Elm St", "Premium", "2026-09-30", "Active"},
                {"Emma Brown", "emma@email.com", "555-0105", "654 Maple Dr", "Standard", "2026-03-31", "Active"}
            };
            for (Object[] member : members) {
                for (int i = 0; i < member.length; i++) ps.setObject(i + 1, member[i]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
