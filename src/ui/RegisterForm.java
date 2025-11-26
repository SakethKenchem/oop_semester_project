package ui;

import database.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.security.MessageDigest;

public class RegisterForm extends JFrame {

    JTextField tfStudentID, tfName;
    JPasswordField pfPassword;
    JButton btnRegister, btnLogin;

    public RegisterForm() {
        setTitle("Register - Student Voting System");
        setSize(430, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(70, 20, 250, 40);
        add(title);

        JLabel l1 = new JLabel("Student ID:");
        l1.setBounds(30, 90, 100, 25);
        add(l1);

        tfStudentID = new JTextField();
        tfStudentID.setBounds(140, 90, 200, 25);
        add(tfStudentID);

        JLabel l2 = new JLabel("Full Name:");
        l2.setBounds(30, 140, 100, 25);
        add(l2);

        tfName = new JTextField();
        tfName.setBounds(140, 140, 200, 25);
        add(tfName);

        JLabel l4 = new JLabel("Password:");
        l4.setBounds(30, 190, 100, 25);
        add(l4);

        pfPassword = new JPasswordField();
        pfPassword.setBounds(140, 190, 200, 25);
        add(pfPassword);

        btnRegister = new JButton("Register");
        btnRegister.setBounds(70, 250, 100, 35);
        btnRegister.addActionListener(this::doRegister);
        add(btnRegister);

        btnLogin = new JButton("Have an account already? Login");
        btnLogin.setBounds(200, 250, 200, 35);
        btnLogin.addActionListener(e -> {
            dispose();
            new LoginForm();
        });
        add(btnLogin);

        setVisible(true);
    }

    private void doRegister(ActionEvent e) {
        String sid = tfStudentID.getText().trim();
        String name = tfName.getText().trim();
        String pass = String.valueOf(pfPassword.getPassword()).trim();

        if (sid.isEmpty() || name.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        String hashedPass = hashPassword(pass); // hash password

        try (Connection con = DBConnection.getConnection()) {
            String checkSql = "SELECT * FROM voters WHERE student_id=?";
            PreparedStatement checkStmt = con.prepareStatement(checkSql);
            checkStmt.setString(1, sid);
            if (checkStmt.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "This Student ID already exists.");
                return;
            }

            String sql = "INSERT INTO voters(student_id, name, password) VALUES (?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sid);
            ps.setString(2, name);
            ps.setString(3, hashedPass);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Registration Successful!");
            dispose();
            new LoginForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }


    //sha 256 password hashing
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


}
