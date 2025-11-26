package ui;

import database.DBConnection;
import models.Admin;

import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminLoginForm extends JFrame {

    JTextField tfUsername;
    JPasswordField pfPassword;
    JButton btnLogin, btnRegister;

    //gui for login
    public AdminLoginForm() {
        setTitle("Admin Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel title = new JLabel("Admin Login", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBounds(50, 20, 300, 30);
        add(title);

        JLabel l1 = new JLabel("Username:");
        l1.setBounds(50, 80, 100, 25);
        add(l1);

        tfUsername = new JTextField();
        tfUsername.setBounds(160, 80, 180, 25);
        add(tfUsername);

        JLabel l2 = new JLabel("Password:");
        l2.setBounds(50, 130, 100, 25);
        add(l2);

        pfPassword = new JPasswordField();
        pfPassword.setBounds(160, 130, 180, 25);
        add(pfPassword);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(70, 200, 100, 30);
        btnLogin.addActionListener(e -> doLogin());
        add(btnLogin);

        btnRegister = new JButton("Register Here");
        btnRegister.setBounds(220, 200, 100, 30);
        btnRegister.addActionListener(e -> {
            dispose();
            new AdminRegisterForm();
        });
        add(btnRegister);

        setVisible(true);
    }

    //method that handles login
    private void doLogin() {
        String username = tfUsername.getText().trim();
        String password = String.valueOf(pfPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password!");
            return;
        }

        String hashedPassword = hashPassword(password);
        Admin admin = new Admin(username, hashedPassword);

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM admins WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, admin.getUsername());
            ps.setString(2, admin.getPasswordHash());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                dispose();
                new AdminDashboard(admin.getUsername());
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    //hashes password before inserting into database
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
