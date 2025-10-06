package com.auction.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.auction.database.Database;
import com.auction.model.Bid;

public class BidDao {
    public Double getHighestBidAmount(int itemId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT MAX(amount) AS max_amount FROM bids WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double val = rs.getDouble("max_amount");
                    return rs.wasNull() ? null : val;
                }
            }
        }
        return null;
    }

    public Bid placeBid(int itemId, int bidderId, double amount) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO bids(item_id, bidder_id, amount) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, itemId);
            ps.setInt(2, bidderId);
            ps.setDouble(3, amount);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Bid(rs.getInt(1), itemId, bidderId, amount, LocalDateTime.now());
                }
            }
        }
        throw new SQLException("Failed to place bid");
    }

    public List<Bid> listBidsForItem(int itemId) throws SQLException {
        List<Bid> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM bids WHERE item_id = ? ORDER BY amount DESC, bid_time DESC")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp ts = rs.getTimestamp("bid_time");
                    LocalDateTime when = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                    list.add(new Bid(
                            rs.getInt("id"),
                            rs.getInt("item_id"),
                            rs.getInt("bidder_id"),
                            rs.getDouble("amount"),
                            when
                    ));
                }
            }
        }
        return list;
    }

    public void deleteAllBidsForItem(int itemId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM bids WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            ps.executeUpdate();
        }
    }

    public void deleteBid(int bidId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM bids WHERE id = ?")) {
            ps.setInt(1, bidId);
            ps.executeUpdate();
        }
    }
}


