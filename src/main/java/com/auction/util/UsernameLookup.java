package com.auction.util;

import com.auction.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class UsernameLookup {
    private UsernameLookup() {}

    public static String getUsername(int userId) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT username FROM users WHERE id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception ignored) { }
        return "?";
    }
}


