package com.auction.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import org.mindrot.jbcrypt.BCrypt;

public final class Database {
    private static final String DB_DIRECTORY = System.getProperty("user.home") + "/.auction-system";
    private static final String IMAGES_DIRECTORY = DB_DIRECTORY + "/images";
    private static final String DB_PATH = DB_DIRECTORY + "/auction.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_PATH;

    private Database() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    public static String getDataDirectory() { return DB_DIRECTORY; }
    public static String getImagesDirectory() { return IMAGES_DIRECTORY; }

    public static void initialize() {
        try {
            Path dir = Paths.get(DB_DIRECTORY);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path imgDir = Paths.get(IMAGES_DIRECTORY);
            if (!Files.exists(imgDir)) {
                Files.createDirectories(imgDir);
            }

            try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
                st.executeUpdate("PRAGMA foreign_keys = ON");

                st.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS users (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "username TEXT UNIQUE NOT NULL," +
                                "password_hash TEXT NOT NULL," +
                                "is_admin INTEGER DEFAULT 0," +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );

                st.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS items (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "owner_id INTEGER NOT NULL," +
                                "title TEXT NOT NULL," +
                                "description TEXT," +
                                "start_price REAL NOT NULL," +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                                "end_time DATETIME NOT NULL," +
                                "is_closed INTEGER DEFAULT 0," +
                                "FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE" +
                                ")"
                );

                // Add image_path column if missing (SQLite allows simple ADD COLUMN)
                try {
                    st.executeUpdate("ALTER TABLE items ADD COLUMN image_path TEXT");
                } catch (SQLException ignore) { /* column likely exists */ }

                // Add is_admin column if missing
                try {
                    st.executeUpdate("ALTER TABLE users ADD COLUMN is_admin INTEGER DEFAULT 0");
                } catch (SQLException ignore) { /* column likely exists */ }

                // Ensure default admin account exists
                try (PreparedStatement check = conn.prepareStatement("SELECT id FROM users WHERE username = 'admin'")) {
                    boolean exists;
                    try (var rs = check.executeQuery()) { exists = rs.next(); }
                    if (!exists) {
                        String hash = BCrypt.hashpw("admin", BCrypt.gensalt(10));
                        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO users(username, password_hash, is_admin) VALUES('admin', ?, 1)")) {
                            ins.setString(1, hash);
                            ins.executeUpdate();
                        }
                    }
                }

                st.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS bids (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "item_id INTEGER NOT NULL," +
                                "bidder_id INTEGER NOT NULL," +
                                "amount REAL NOT NULL," +
                                "bid_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                                "FOREIGN KEY(item_id) REFERENCES items(id) ON DELETE CASCADE," +
                                "FOREIGN KEY(bidder_id) REFERENCES users(id) ON DELETE CASCADE" +
                                ")"
                );

                st.executeUpdate(
                        "CREATE VIEW IF NOT EXISTS item_highest_bid AS " +
                                "SELECT i.id AS item_id, MAX(b.amount) AS highest_bid " +
                                "FROM items i LEFT JOIN bids b ON i.id = b.item_id " +
                                "GROUP BY i.id"
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}


