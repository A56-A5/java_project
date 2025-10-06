package com.auction;

import com.auction.model.User;
import com.auction.ui.LoginFrame;
import com.auction.ui.MainFrame;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { FlatLaf.setup(new FlatMacDarkLaf()); } catch (Exception ignored) {}

            LoginFrame lf = new LoginFrame(new LoginFrame.LoginListener() {
                @Override public void onLogin(User user) {
                    new MainFrame(user).setVisible(true);
                }
            });
            lf.setVisible(true);
        });
    }
}


