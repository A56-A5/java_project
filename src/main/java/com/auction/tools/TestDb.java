package com.auction.tools;

import com.auction.dao.BidDao;
import com.auction.dao.ItemDao;
import com.auction.database.Database;

public class TestDb {
    public static void main(String[] args) throws Exception {
        Database.initialize();
        ItemDao itemDao = new ItemDao();
        BidDao bidDao = new BidDao();
        var items = itemDao.listActiveItems();
        System.out.println("Items found: " + items.size());
        for (var it : items) {
            System.out.println("Item: id=" + it.getId() + " title='" + it.getTitle() + "' owner=" + it.getOwnerId());
            var bids = bidDao.listBidsForItem(it.getId());
            System.out.println("  bids: " + bids.size());
            bids.forEach(b -> System.out.println("    bid id=" + b.getId() + " bidder=" + b.getBidderId() + " amount=" + b.getAmount() + " time=" + b.getBidTime()));
        }
    }
}
