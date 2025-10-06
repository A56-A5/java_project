package com.auction.model;

import java.time.LocalDateTime;

public class Item {
    private final int id;
    private final int ownerId;
    private final String title;
    private final String description;
    private final double startPrice;
    private final LocalDateTime endTime;
    private final boolean closed;
    private final String imagePath;

    public Item(int id, int ownerId, String title, String description, double startPrice, LocalDateTime endTime, boolean closed, String imagePath) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.startPrice = startPrice;
        this.endTime = endTime;
        this.closed = closed;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public int getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getStartPrice() { return startPrice; }
    public LocalDateTime getEndTime() { return endTime; }
    public boolean isClosed() { return closed; }
    public String getImagePath() { return imagePath; }
}


