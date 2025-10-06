package com.auction.model;

import java.time.LocalDateTime;

public class Bid {
    private final int id;
    private final int itemId;
    private final int bidderId;
    private final double amount;
    private final LocalDateTime bidTime;

    public Bid(int id, int itemId, int bidderId, double amount, LocalDateTime bidTime) {
        this.id = id;
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.bidTime = bidTime;
    }

    public int getId() { return id; }
    public int getItemId() { return itemId; }
    public int getBidderId() { return bidderId; }
    public double getAmount() { return amount; }
    public LocalDateTime getBidTime() { return bidTime; }
}


