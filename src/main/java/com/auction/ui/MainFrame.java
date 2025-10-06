package com.auction.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.auction.dao.BidDao;
import com.auction.dao.ItemDao;
import com.auction.model.Item;
import com.auction.model.User;
import com.auction.service.AuctionService;

public class MainFrame extends JFrame {
    private final User currentUser;
    private final ItemDao itemDao = new ItemDao();
    private final BidDao bidDao = new BidDao();
    private final AuctionService auctionService = new AuctionService();

    // Card layout components for browse view
    private final JPanel cardsContainer = new JPanel();
    private final JScrollPane cardsScroll = new JScrollPane(cardsContainer);

    public MainFrame(User currentUser) {
        super("Auction - Welcome " + currentUser.getUsername());
        this.currentUser = currentUser;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildToolbar(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Items", buildBrowsePanel());
        tabs.addTab("Add Item", buildAddItemPanel());
        add(tabs, BorderLayout.CENTER);

        refreshItems();

        new javax.swing.Timer(20_000, e -> { // periodic close + refresh
            try { auctionService.closeExpiredAuctions(); } catch (Exception ignored) {}
            refreshItems();
        }).start();
    }

    private JPanel buildBrowsePanel() {
        JPanel p = new JPanel(new BorderLayout());
        // Cards grid
        cardsContainer.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        cardsContainer.setLayout(new GridBagLayout());
        cardsScroll.getVerticalScrollBar().setUnitIncrement(16);
        cardsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        p.add(cardsScroll, BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refresh);
        p.add(bottom, BorderLayout.SOUTH);

        refresh.addActionListener(e -> refreshItems());
        return p;
    }

    private JPanel buildAddItemPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField title = new JTextField();
        JTextArea desc = new JTextArea(5, 20);
        JTextField price = new JTextField();
        JTextField imagePath = new JTextField(); imagePath.setEditable(false);
        JButton browseImg = new JButton("Select Image...");

        gbc.gridx=0; gbc.gridy=0; p.add(new JLabel("Title"), gbc);
        gbc.gridx=1; gbc.gridy=0; p.add(title, gbc);
        gbc.gridx=0; gbc.gridy=1; p.add(new JLabel("Description"), gbc);
        gbc.gridx=1; gbc.gridy=1; gbc.weightx=1; gbc.fill=GridBagConstraints.BOTH; p.add(new JScrollPane(desc), gbc);
        gbc.gridx=0; gbc.gridy=2; gbc.weightx=0; gbc.fill=GridBagConstraints.HORIZONTAL; p.add(new JLabel("Start Price"), gbc);
        gbc.gridx=1; gbc.gridy=2; p.add(price, gbc);
        gbc.gridx=0; gbc.gridy=3; p.add(new JLabel("Image"), gbc);
        JPanel imgRow = new JPanel(new BorderLayout(6,0)); imgRow.add(imagePath, BorderLayout.CENTER); imgRow.add(browseImg, BorderLayout.EAST);
        gbc.gridx=1; gbc.gridy=3; p.add(imgRow, gbc);

        JButton add = new JButton("Add Item (24h)");
        gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2; gbc.fill=GridBagConstraints.NONE; gbc.anchor=GridBagConstraints.EAST; p.add(add, gbc);

        browseImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Images", "png","jpg","jpeg","gif"));
            int r = fc.showOpenDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                imagePath.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        add.addActionListener(e -> {
            try {
                String t = title.getText().trim();
                String d = desc.getText();
                double sp = Double.parseDouble(price.getText().trim());
                String img = imagePath.getText();
                if (t.isEmpty() || sp <= 0) {
                    JOptionPane.showMessageDialog(this, "Provide title and positive price");
                    return;
                }
                String storedPath = null;
                if (img != null && !img.isEmpty()) {
                    storedPath = com.auction.util.ImageStorage.storeImage(img);
                }
                itemDao.addItem(currentUser.getId(), t, d, sp, storedPath);
                JOptionPane.showMessageDialog(this, "Item added for 24 hours.");
                title.setText(""); desc.setText(""); price.setText(""); imagePath.setText("");
                refreshItems();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage());
            }
        });

        return p;
    }

    private void refreshItems() {
        try {
            cardsContainer.removeAll();
            List<Item> list = itemDao.listActiveItems();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6,6,6,6);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0.5;

            int col = 0;
            int row = 0;
            for (Item it : list) {
                double price = new AuctionService().getCurrentPrice(it.getId(), it.getStartPrice());
                String owner = com.auction.util.UsernameLookup.getUsername(it.getOwnerId());
                JPanel card = buildItemCard(it, price, owner, df);
                gbc.gridx = col;
                gbc.gridy = row;
                cardsContainer.add(card, gbc);
                col++;
                if (col >= 2) { col = 0; row++; }
            }
            // push remainder to top
            gbc.gridx = 0; gbc.gridy = row + 1; gbc.weighty = 1; gbc.gridwidth = 2;
            cardsContainer.add(Box.createVerticalGlue(), gbc);
            cardsContainer.revalidate();
            cardsContainer.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage());
        }
    }

    private void placeBid() {
        // No-op (card buttons handle bidding per-item)
    }

    private JToolBar buildToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        JLabel title = new JLabel("  Online Auction");
        JButton logout = new JButton("Logout");
        tb.add(title);
        tb.add(Box.createHorizontalGlue());
        tb.add(new JLabel("Signed in as: " + currentUser.getUsername() + "  "));
        tb.add(logout);
        logout.addActionListener(e -> doLogout());
        return tb;
    }

    private void doLogout() {
        dispose();
        new com.auction.ui.LoginFrame(user -> new MainFrame(user).setVisible(true)).setVisible(true);
    }

    private void updatePreview() { /* removed in card layout */ }

    private JPanel buildItemCard(Item it, double price, String owner, DateTimeFormatter df) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60,60,60)),
                BorderFactory.createEmptyBorder(10,10,10,10)));

        // Image
        JLabel img = new JLabel("", JLabel.CENTER);
        img.setAlignmentX(Component.LEFT_ALIGNMENT);
        img.setPreferredSize(new Dimension(360, 200));
        try {
            if (it.getImagePath() != null) {
                img.setIcon(com.auction.util.ImageStorage.loadScaled(it.getImagePath(), 360, 200));
            }
        } catch (Exception ignored) { }
        card.add(img);

        // Title and meta
        JPanel head = new JPanel(new GridLayout(0, 1));
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(new JLabel(it.getTitle()));
        head.add(new JLabel("Owner: " + owner + "   Ends: " + humanizeRemaining(it)));
        head.add(new JLabel("Current Price: " + price));
        card.add(head);

        // Description
        if (it.getDescription() != null && !it.getDescription().isEmpty()) {
            JTextArea desc = new JTextArea(it.getDescription());
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            desc.setEditable(false);
            desc.setOpaque(false);
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(desc);
        }

        // Actions
        JButton bidBtn = new JButton("Bid");
        bidBtn.addActionListener(e -> handleBid(it, price));
        JButton deleteItem = new JButton("Delete Item");
        // allow owners to delete their own items as well as admins
        boolean canDelete = currentUser.isAdmin() || currentUser.getId() == it.getOwnerId();
        deleteItem.setVisible(canDelete);
        deleteItem.addActionListener(e -> handleDeleteItem(it));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(deleteItem);
        actions.add(bidBtn);
        card.add(actions);

        // Bids area below (sorted highest first)
        JPanel bidsPanel = new JPanel(new BorderLayout());
        bidsPanel.setBorder(BorderFactory.createTitledBorder("Bids"));
        bidsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        javax.swing.table.DefaultTableModel bidsModel = new javax.swing.table.DefaultTableModel(new Object[]{"User","Amount"}, 0) {
            @Override
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable bidsTable = new JTable(bidsModel);
        bidsTable.setShowGrid(false);
        bidsTable.setRowHeight(20);
        bidsTable.setFillsViewportHeight(true);
        bidsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bidsTable.setPreferredScrollableViewportSize(new Dimension(340, 100));
        // set reasonable preferred widths for the two columns
        try {
            bidsTable.getColumnModel().getColumn(0).setPreferredWidth(220);
            bidsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        } catch (Exception ignore) {}

        JScrollPane bidsScroll = new JScrollPane(bidsTable);
        bidsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        bidsScroll.setPreferredSize(new Dimension(360, 120));
        bidsScroll.setMaximumSize(new Dimension(360, 200));

        boolean added = false;
        try {
            var list = new com.auction.dao.BidDao().listBidsForItem(it.getId());
            java.text.DecimalFormat dfMoney = new java.text.DecimalFormat("#,##0.00");
            if (list.isEmpty()) {
                // show a friendly placeholder when no bids
                JLabel none = new JLabel("No bids");
                none.setHorizontalAlignment(JLabel.CENTER);
                bidsPanel.add(none, BorderLayout.CENTER);
                added = true;
            } else {
                for (var b : list) {
                    String userName = com.auction.util.UsernameLookup.getUsername(b.getBidderId());
                    bidsModel.addRow(new Object[]{userName, "$" + dfMoney.format(b.getAmount())});
                }
                bidsPanel.add(bidsScroll, BorderLayout.CENTER);
                added = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            bidsPanel.removeAll();
            JLabel err = new JLabel("Bids error: " + ex.getMessage());
            err.setHorizontalAlignment(JLabel.CENTER);
            bidsPanel.add(err, BorderLayout.CENTER);
            added = true;
        }

        if (!added) {
            // fallback if something went wrong and no exception (shouldn't happen)
            bidsPanel.add(new JLabel("Bids unavailable"), BorderLayout.CENTER);
        }

        card.add(bidsPanel);

        return card;
    }

    private void handleBid(Item it, double current) {
        if (it.getOwnerId() == currentUser.getId()) {
            JOptionPane.showMessageDialog(this, "You cannot bid on your own item");
            return;
        }
        String s = JOptionPane.showInputDialog(this, "Your bid (> " + current + "):");
        if (s == null) return;
        try {
            double amount = Double.parseDouble(s.trim());
            if (amount <= current) {
                JOptionPane.showMessageDialog(this, "Bid must be higher than current price");
                return;
            }
            bidDao.placeBid(it.getId(), currentUser.getId(), amount);
            refreshItems();
            JOptionPane.showMessageDialog(this, "Bid placed");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Bid failed: " + ex.getMessage());
        }
    }

    private void handleDeleteItem(Item it) {
        // only admin or owner may delete the item
        if (!(currentUser.isAdmin() || currentUser.getId() == it.getOwnerId())) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this item (and its bids)?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            itemDao.deleteItem(it.getId());
            refreshItems();
            JOptionPane.showMessageDialog(this, "Item deleted");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage());
        }
    }

    private String humanizeRemaining(Item it) {
        try {
            java.time.LocalDateTime end = it.getEndTime();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (end.isBefore(now)) return "ended";
            Duration d = Duration.between(now, end);
            long hours = d.toHours();
            long minutes = d.minusHours(hours).toMinutes();
            long days = hours / 24;
            hours = hours % 24;
            if (days > 0) {
                return days + "d " + hours + "h " + minutes + "m";
            }
            return hours + "h " + minutes + "m";
        } catch (Exception e) {
            return "--";
        }
    }
}


