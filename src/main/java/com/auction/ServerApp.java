package com.auction;

import static spark.Spark.*;

import com.auction.database.Database;
import com.auction.dao.ItemDao;
import com.auction.dao.BidDao;
import com.auction.model.Item;
import com.google.gson.Gson;

import java.util.List;

public class ServerApp {
    public static void main(String[] args) {
        int port = 0;
        try { port = Integer.parseInt(System.getenv().getOrDefault("PORT", "0")); } catch (Exception ignored) {}
        if (port == 0) port = 4567;
        port(port);

        Database.initialize();
        ItemDao itemDao = new ItemDao();
        BidDao bidDao = new BidDao();
        Gson gson = new Gson();

        // Simple CORS
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });
        before((req, res) -> res.header("Access-Control-Allow-Origin", "*"));

        get("/items", (req, res) -> {
            res.type("application/json");
            List<Item> items = itemDao.listActiveItems();
            return gson.toJson(items);
        });

        post("/items/:id/bid", (req, res) -> {
            res.type("application/json");
            int itemId = Integer.parseInt(req.params(":id"));
            var body = gson.fromJson(req.body(), java.util.Map.class);
            Double amount = null;
            try { amount = Double.parseDouble(body.get("amount").toString()); } catch (Exception e) { }
            Integer bidderId = null;
            try { bidderId = (int) Double.parseDouble(body.get("bidderId").toString()); } catch (Exception e) { }
            if (amount == null || bidderId == null) {
                res.status(400);
                return gson.toJson(java.util.Map.of("error", "invalid payload, need amount and bidderId"));
            }
            try {
                bidDao.placeBid(itemId, bidderId, amount);
                return gson.toJson(java.util.Map.of("status", "ok"));
            } catch (Exception ex) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", ex.getMessage()));
            }
        });
    }
}
