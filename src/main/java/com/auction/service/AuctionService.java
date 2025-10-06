package com.auction.service;

import com.auction.dao.BidDao;
import com.auction.dao.ItemDao;
import com.auction.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuctionService {
    private final ItemDao itemDao = new ItemDao();
    private final BidDao bidDao = new BidDao();

    public double getCurrentPrice(int itemId, double startPrice) throws SQLException {
        Double highest = bidDao.getHighestBidAmount(itemId);
        return highest == null ? startPrice : Math.max(startPrice, highest);
    }

    public boolean canBid(int itemId, double startPrice, double newAmount) throws SQLException {
        double current = getCurrentPrice(itemId, startPrice);
        return newAmount > current;
    }

    public void closeExpiredAuctions() throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id FROM items WHERE is_closed = 0 AND datetime(end_time) <= datetime('now')")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int itemId = rs.getInt("id");
                    itemDao.markClosed(itemId);
                }
            }
        }
    }
}


