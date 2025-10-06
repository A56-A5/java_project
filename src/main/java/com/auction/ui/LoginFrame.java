package com.auction.ui;

import com.auction.dao.UserDao;
import com.auction.database.Database;
import com.auction.model.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final UserDao userDao = new UserDao();

    public interface LoginListener { void onLogin(User user); }

    public LoginFrame(LoginListener listener) {
        super("Login - Auction System");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);

        Database.initialize();

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Username"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Password"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(passwordField, gbc);

        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Sign up");
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(signupBtn);
        btns.add(loginBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; panel.add(btns, gbc);

        loginBtn.addActionListener(e -> doLogin(listener));
        signupBtn.addActionListener(e -> doSignup());

        setContentPane(panel);
    }

    private void doLogin(LoginListener listener) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password");
            return;
        }
        try {
            User user = userDao.login(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid credentials");
                return;
            }
            listener.onLogin(user);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
        }
    }

    private void doSignup() {
        String username = JOptionPane.showInputDialog(this, "Choose a username");
        if (username == null || username.trim().isEmpty()) return;
        JPanel p = new JPanel(new GridLayout(2,2,8,8));
        JPasswordField p1 = new JPasswordField();
        JPasswordField p2 = new JPasswordField();
        p.add(new JLabel("Password")); p.add(p1);
        p.add(new JLabel("Confirm")); p.add(p2);
        int res = JOptionPane.showConfirmDialog(this, p, "Set password", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        String pass1 = new String(p1.getPassword());
        String pass2 = new String(p2.getPassword());
        if (!pass1.equals(pass2) || pass1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Passwords must match and not be empty");
            return;
        }
        try {
            userDao.register(username.trim(), pass1);
            JOptionPane.showMessageDialog(this, "Signup successful. You can login now.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Signup failed: " + ex.getMessage());
        }
    }
}


