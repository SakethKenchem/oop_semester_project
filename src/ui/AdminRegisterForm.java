package ui;

import database.DBConnection;
import models.Admin;

import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AdminRegisterForm extends JFrame {

    JTextField tfUsername;
    JPasswordField pfPassword;
    JButton btnRegister, btnLogin;

    public AdminRegisterForm() {
        setTitle("Admin Register");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel title = new JLabel("Admin Registration", SwingConstants.CENTER);
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

        btnRegister = new JButton("Register");
        btnRegister.setBounds(70, 200, 100, 30);
        btnRegister.addActionListener(e -> doRegister());
        add(btnRegister);

        btnLogin = new JButton("Login Here");
        btnLogin.setBounds(220, 200, 100, 30);
        btnLogin.addActionListener(e -> {
            dispose();
            new AdminLoginForm();
        });
        add(btnLogin);

        setVisible(true);
    }

    private void doRegister() {
        String username = tfUsername.getText().trim();
        String password = String.valueOf(pfPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        String hashedPassword = hashPassword(password);
        Admin admin = new Admin(username, hashedPassword);

        try (Connection con = DBConnection.getConnection()) {
            String checkSql = "SELECT * FROM admins WHERE username=?";
            PreparedStatement checkStmt = con.prepareStatement(checkSql);
            checkStmt.setString(1, admin.getUsername());
            if (checkStmt.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
                return;
            }

            String sql = "INSERT INTO admins(username,password) VALUES(?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, admin.getUsername());
            ps.setString(2, admin.getPasswordHash());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Admin registered successfully!");
            dispose();
            new AdminLoginForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    //sha 256 password hashing
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
