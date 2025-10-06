package com.auction.dao;

import com.auction.database.Database;
import com.auction.model.Item;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ItemDao {
    public Item addItem(int ownerId, String title, String description, double startPrice, String imagePath) throws SQLException {
        try (Connection c = Database.getConnection()) {
            LocalDateTime endTime = LocalDateTime.now().plusHours(24);
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO items(owner_id, title, description, start_price, end_time, image_path) VALUES(?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, ownerId);
                ps.setString(2, title);
                ps.setString(3, description);
                ps.setDouble(4, startPrice);
                ps.setString(5, endTime.toString());
                ps.setString(6, imagePath);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return new Item(rs.getInt(1), ownerId, title, description, startPrice, endTime, false, imagePath);
                    }
                }
            }
        }
        throw new SQLException("Failed to add item");
    }

    public List<Item> listActiveItems() throws SQLException {
        List<Item> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM items WHERE is_closed = 0 AND datetime(end_time) > datetime('now') ORDER BY datetime(end_time) ASC")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Item findById(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM items WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void markClosed(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE items SET is_closed = 1 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteItem(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM items WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Item map(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int ownerId = rs.getInt("owner_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        double startPrice = rs.getDouble("start_price");
        LocalDateTime endTime = LocalDateTime.parse(rs.getString("end_time"));
        boolean closed = rs.getInt("is_closed") == 1;
        String imagePath = rs.getString("image_path");
        return new Item(id, ownerId, title, description, startPrice, endTime, closed, imagePath);
    }
}


