package ui;

import database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.security.MessageDigest;

public class LoginForm extends JFrame {

    JTextField tfStudentID;
    JPasswordField pfPassword;
    JButton btnLogin, btnRegister;

    public LoginForm() {
        setTitle("Login - Student Voting");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel title = new JLabel("Login", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(80, 20, 200, 40);
        add(title);

        JLabel l1 = new JLabel("Student ID:");
        l1.setBounds(30, 90, 100, 25);
        add(l1);

        tfStudentID = new JTextField();
        tfStudentID.setBounds(140, 90, 160, 25);
        add(tfStudentID);

        JLabel l2 = new JLabel("Password:");
        l2.setBounds(30, 140, 100, 25);
        add(l2);

        pfPassword = new JPasswordField();
        pfPassword.setBounds(140, 140, 160, 25);
        add(pfPassword);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(50, 200, 100, 30);
        btnLogin.addActionListener(e -> doLogin());
        add(btnLogin);

        btnRegister = new JButton("Don't have an Account? Register");
        btnRegister.setBounds(180, 200, 180, 30);
        btnRegister.addActionListener(e -> {
            dispose();
            new RegisterForm(); // assumes you have a RegisterForm class
        });
        add(btnRegister);

        setVisible(true);
    }

    // Hash password using SHA-256
    public static String hashPassword(String password) {
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

    private void doLogin() {
        String sid = tfStudentID.getText().trim();
        String pass = String.valueOf(pfPassword.getPassword()).trim();

        if (sid.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter ID and Password");
            return;
        }

        String hashedPass = hashPassword(pass);

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM voters WHERE student_id=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sid);
            ps.setString(2, hashedPass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int voterId = rs.getInt("id");
                JOptionPane.showMessageDialog(this, "Login Successful!");
                dispose();
                new VoterDashboard(voterId);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored){}
        new LoginForm();
    }
}
