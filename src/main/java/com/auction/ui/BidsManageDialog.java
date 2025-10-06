package com.auction.ui;

import com.auction.dao.BidDao;
import com.auction.model.Bid;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BidsManageDialog extends JDialog {
    private final int itemId;
    private final BidDao bidDao = new BidDao();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Bidder","Amount","Time"}, 0) {
        public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable table = new JTable(model);

    public BidsManageDialog(Window owner, int itemId) {
        super(owner, "Manage Bids", ModalityType.APPLICATION_MODAL);
        this.itemId = itemId;
        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        add(new JScrollPane(table), BorderLayout.CENTER);
        JButton deleteBtn = new JButton("Delete Selected");
        JButton closeBtn = new JButton("Close");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(deleteBtn);
        south.add(closeBtn);
        add(south, BorderLayout.SOUTH);

        deleteBtn.addActionListener(e -> deleteSelected());
        closeBtn.addActionListener(e -> dispose());

        refresh();
    }

    private void refresh() {
        try {
            model.setRowCount(0);
            List<Bid> bids = bidDao.listBidsForItem(itemId);
            for (Bid b : bids) {
                String bidder = com.auction.util.UsernameLookup.getUsername(b.getBidderId());
                model.addRow(new Object[]{b.getId(), bidder, b.getAmount(), b.getBidTime()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a bid"); return; }
        int bidId = (Integer) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this bid?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            bidDao.deleteBid(bidId);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
        }
    }
}


