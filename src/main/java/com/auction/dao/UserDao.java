package com.auction.dao;

import com.auction.database.Database;
import com.auction.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
    public User register(String username, String plainPassword) throws SQLException {
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO users(username, password_hash) VALUES(?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, hashed);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return new User(rs.getInt(1), username, false);
                    }
                }
            }
        }
        throw new SQLException("Failed to create user");
    }

    public User login(String username, String plainPassword) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, password_hash, is_admin FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (BCrypt.checkpw(plainPassword, hash)) {
                        boolean admin = rs.getInt("is_admin") == 1;
                        return new User(rs.getInt("id"), username, admin);
                    }
                }
            }
        }
        return null;
    }
}


