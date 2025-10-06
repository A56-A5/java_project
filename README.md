# Auction System (Java Swing + SQLite)

This is a small desktop auction application written in Java (Swing) that uses a local SQLite database to store users, items and bids. It was built for learning and small internal use-cases where a lightweight, single-file database and simple desktop UI are preferred.

This README documents the project's purpose, architecture, schema, important classes, developer and run instructions, troubleshooting tips, and recommended next steps.

## Table of contents
- Project overview
- Architecture and key classes
- Database schema (tables & relationships)
- How the UI shows bids per-item
- Build & run
- Local development and debugging
- Tests
- Packaging and distribution
- Contributing
- License

## Project overview

Features:
- User signup and login (passwords hashed with BCrypt)
- Add an auction item (default duration is 24 hours)
- Browse active auctions, view per-item bids, and place bids
- Periodic background check to close auctions when end time passes
- Owners and admins can delete their items (deletion cascades bids)

The app is intentionally small and organizes behavior in a few packages:
- `com.auction.ui` — Swing user interface (frames, dialogs)
- `com.auction.dao` — Data access objects for items, bids, users
- `com.auction.database` — SQLite initialization and connection helper
- `com.auction.service` — Business logic such as checking current price and closing auctions
- `com.auction.model` — Simple POJOs: `User`, `Item`, `Bid`
- `com.auction.util` — small helpers (image storage / username lookup)

## Architecture and key classes

- `com.auction.App` — program entry point; sets up look-and-feel and shows `LoginFrame`.
- `com.auction.ui.LoginFrame` — login view and signup dialog.
- `com.auction.ui.MainFrame` — main application window with two tabs:
  - "Items" (browse active items)
  - "Add Item" (form to create a new auction item; stores images to `~/.auction-system/images`)
- `com.auction.ui.BidsManageDialog` — modal dialog to list and delete individual bids for an item.
- `com.auction.dao.ItemDao` — add/list/find/delete items; maps result sets into `Item` model.
- `com.auction.dao.BidDao` — place bids, list bids for an item, delete bids. Uses SQL queries against `bids` table.
- `com.auction.dao.UserDao` — register and authenticate users using BCrypt hash checks.
- `com.auction.database.Database` — ensures DB files & directories exist and creates tables/views on first run.

## Database schema (important columns)

The SQLite DB is created at `~/.auction-system/auction.db`.

Tables (simplified):

- `users`
  - `id` INTEGER PRIMARY KEY
  - `username` TEXT UNIQUE
  - `password_hash` TEXT
  - `is_admin` INTEGER (0/1)

- `items`
  - `id` INTEGER PRIMARY KEY
  - `owner_id` INTEGER REFERENCES users(id) ON DELETE CASCADE
  - `title`, `description`, `start_price` (REAL)
  - `end_time` DATETIME
  - `is_closed` INTEGER
  - `image_path` TEXT

- `bids`
  - `id` INTEGER PRIMARY KEY
  - `item_id` INTEGER REFERENCES items(id) ON DELETE CASCADE
  - `bidder_id` INTEGER REFERENCES users(id) ON DELETE CASCADE
  - `amount` REAL
  - `bid_time` DATETIME DEFAULT CURRENT_TIMESTAMP

There is also a view:
- `item_highest_bid` — precomputes the highest bid per item (used for convenience).

Notes on schema decisions:
- The app uses a single `bids` table (with an `item_id` foreign key) — this is the standard normalized design. It gives a separate bid list per item in queries without creating separate physical tables per item.

## How bids are shown (UI behavior)

- In `MainFrame`, each item card contains a titled `Bids` panel. The code queries `BidDao.listBidsForItem(itemId)` and populates a small JTable with `User` and `Amount` columns. If there are no bids, the UI shows a centered "No bids" message.
- Owners (and admins) can delete their items; deleting an item removes its bids thanks to the DB ON DELETE CASCADE clause.
- The dedicated `BidsManageDialog` can be used to view and delete individual bids.

## Build

Prerequisites:
- Java 17 (JDK) installed and on PATH
- Maven (for building from source)

Build steps:

```powershell
cd auciton-system
mvn -DskipTests package
```

The packaging creates two important artifacts under `target/`:
- `auction-system-1.0.0.jar` (normal jar)
- `auction-system-1.0.0-jar-with-dependencies.jar` (fat jar including dependencies)

## Run

Run the fat jar (recommended):

```powershell
java -jar target/auction-system-1.0.0-jar-with-dependencies.jar
```

On first run the app will initialize the data directory at `%USERPROFILE%/.auction-system` (Windows) or `~/.auction-system` (Linux/macOS) and create the SQLite DB. A default admin user is created automatically: username `admin` password `admin` (change/remove for production use).

## Local development & debugging

- Database path: `System.getProperty("user.home") + "/.auction-system/auction.db"`.
- If the UI displays "Bids unavailable" or an error, check console output — the app prints stack traces for DB issues.
- Quick DB inspection: open the SQLite file with any SQLite client (for example the `sqlite3` CLI):

```powershell
sqlite3 %USERPROFILE%/.auction-system/auction.db
SELECT * FROM items;
SELECT * FROM bids;
```

- A small helper `com.auction.tools.TestDb` is included to dump items and bids to the console during development. Run it with:

```powershell
mvn -DskipTests package
java -cp target/auction-system-1.0.0-jar-with-dependencies.jar com.auction.tools.TestDb
```

## Tests

There are no automated unit tests included at this time. Adding tests around DAOs and the `AuctionService` would be recommended:
- Dao tests can use an in-memory SQLite database or a temporary file.
- Business logic tests should validate bidding rules and auction closing behavior.

## Packaging & distribution

- The fat JAR is portable and can be distributed. The app stores runtime data under the user's home directory.
- For multi-user or production scenarios, consider moving to a server (Spring Boot + remote DB) or adding authentication/session hardening.

## Contributing

If you want to add features or fix bugs:

1. Fork the repository and create a topic branch.
2. Run and expand existing DAOs and UI tests.
3. Open a pull request describing the change.

Recommended small improvements you might add:
- Add unit tests for DAOs and auction logic.
- Add pagination or a separate list view for bids.
- Add role-based UI (admin-only features) and better permissions in `BidsManageDialog`.

## Security & notes

- Current default admin: `admin` / `admin`. Delete or change this before sharing the machine.
- Passwords use BCrypt with work factor 10. That's reasonable for a small app but tune as needed.
- The app loads native libraries for the UI; running in some restricted Java environments may require enabling native access.

## Project structure (important files)

- `src/main/java/com/auction/App.java` — entry point
- `src/main/java/com/auction/database/Database.java` — DB initialization
- `src/main/java/com/auction/dao/` — DAO implementations (`ItemDao`, `BidDao`, `UserDao`)
- `src/main/java/com/auction/model/` — model classes
- `src/main/java/com/auction/ui/` — Swing UI (`MainFrame`, `LoginFrame`, `BidsManageDialog`)
- `src/main/java/com/auction/util/` — `ImageStorage`, `UsernameLookup`
- `pom.xml` — Maven build descriptor

## License

This repository contains no explicit license file. Add a license (MIT, Apache-2.0, etc.) if you want to allow others to reuse the code.

---

If you want, I can also:
- Add examples (screenshots) and inline class diagrams to the README.
- Add unit tests and a CI workflow (GitHub Actions) to run build and tests on push.


