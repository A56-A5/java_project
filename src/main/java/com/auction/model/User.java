package com.auction.model;

public class User {
    private final int id;
    private final String username;
    private final boolean admin;

    public User(int id, String username, boolean admin) {
        this.id = id;
        this.username = username;
        this.admin = admin;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public boolean isAdmin() { return admin; }
}


