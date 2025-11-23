package ui;

import database.DBConnection;
import models.Candidate;
import util.VotingUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class AdminDashboard extends JFrame {

    // THEME COLORS (Admin)
    private static final Color ADMIN_PRIMARY = new Color(0xE02729); // Red
    private static final Color ADMIN_SECONDARY = new Color(0x4378BC); // Blue
    private static final Color ADMIN_ACCENT = new Color(0xF4B218); // Amber
    private static final Color BACKGROUND = new Color(250, 250, 250);

    // Candidate Upload Fields
    private JTextField tfFullName, tfAcademicYear, tfSchool;
    private JTextArea taBio;
    private JComboBox<String> cbPosition;
    private JLabel lblPicture, lblManifesto;
    private File selectedPicture, selectedManifesto;
    private JButton btnUploadPicture, btnUploadManifesto, btnSave;

    // Voting Control Fields
    private JTextField tfStartTime, tfEndTime;
    private JButton btnActivate, btnDeactivate, btnLoadWindow;
    private JLabel lblWindowStatus;

    // Results Control Fields
    private JButton btnReleaseResults, btnHideResults;
    private JLabel lblResultsStatus;

    // System Maintenance Fields
    private JButton btnPrepareNewElection;

    // Centralized Constants
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] POSITIONS = {
            "Chairperson", "Vice Chairperson", "Secretary General", "Finance Rep",
            "Public Relations", "Male Academic Rep", "Female Academic Rep",
            "Male Sports Rep", "Female Sports Rep"
    };

    public AdminDashboard(String adminUsername) {
        setTitle("Admin Dashboard - Logged in as: " + adminUsername);
        setSize(950, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        // --- Header Panel with Logo ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND);
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Strathmore Voting System Console", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(ADMIN_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Logo (Top Right)
        ImageIcon logoIcon = new ImageIcon("src/logo.png");
        if (logoIcon.getImage() != null) {

            Image scaled = logoIcon.getImage().getScaledInstance(250, 100, Image.SCALE_SMOOTH);
            JLabel logoLbl = new JLabel(new ImageIcon(scaled));
            logoLbl.setBorder(new EmptyBorder(0, 10, 0, 0));
            headerPanel.add(logoLbl, BorderLayout.EAST);
        }

        add(headerPanel, BorderLayout.NORTH);
        // -----------------------------

        // --- JTabbedPane Setup ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setForeground(Color.BLACK);
        tabbedPane.setBackground(BACKGROUND);

        // Add Panels to Tabs
        tabbedPane.addTab("Upload Candidate", createCandidateUploadPanel());
        tabbedPane.addTab("Voting Window Control", createVotingControlPanel());
        tabbedPane.addTab("Election Results Control", createResultsControlPanel());
        tabbedPane.addTab("System Maintenance", createSystemControlPanel());

        add(tabbedPane, BorderLayout.CENTER);

        loadWindow();

        setVisible(true);
    }

    //Tab 1: Upload Candidate Panel

    private JPanel createCandidateUploadPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Full Name
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        panel.add(new JLabel("Full Name:", SwingConstants.RIGHT), gc);
        tfFullName = new JTextField();
        gc.gridx = 1;
        gc.gridy = row++;
        gc.weightx = 1.0;
        panel.add(tfFullName, gc);

        // Academic Year
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        panel.add(new JLabel("Academic Year:", SwingConstants.RIGHT), gc);
        tfAcademicYear = new JTextField();
        gc.gridx = 1;
        gc.gridy = row++;
        gc.weightx = 1.0;
        panel.add(tfAcademicYear, gc);

        // School
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        panel.add(new JLabel("School:", SwingConstants.RIGHT), gc);
        tfSchool = new JTextField();
        gc.gridx = 1;
        gc.gridy = row++;
        gc.weightx = 1.0;
        panel.add(tfSchool, gc);

        // Position
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        panel.add(new JLabel("Position:", SwingConstants.RIGHT), gc);
        cbPosition = new JComboBox<>(POSITIONS);
        gc.gridx = 1;
        gc.gridy = row++;
        gc.weightx = 1.0;
        panel.add(cbPosition, gc);

        // Picture label + button
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        panel.add(new JLabel("Picture:", SwingConstants.RIGHT), gc);
        JPanel pPic = new JPanel(new BorderLayout(6, 0));
        pPic.setBackground(BACKGROUND);
        lblPicture = new JLabel("No picture selected");

        btnUploadPicture = new JButton("Choose...");
        btnUploadPicture.addActionListener(e -> selectPicture());

        pPic.add(lblPicture, BorderLayout.CENTER);
        pPic.add(btnUploadPicture, BorderLayout.EAST);
        gc.gridx = 1;
        gc.gridy = row++;
        gc.weightx = 1.0;
        panel.add(pPic, gc);

        // Manifesto label + button
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        panel.add(new JLabel("Manifesto:", SwingConstants.RIGHT), gc);
        JPanel pMan = new JPanel(new BorderLayout(6, 0));
        pMan.setBackground(BACKGROUND);
        lblManifesto = new JLabel("No manifesto selected");

        btnUploadManifesto = new JButton("Choose...");
        btnUploadManifesto.addActionListener(e -> selectManifesto());

        pMan.add(lblManifesto, BorderLayout.CENTER);
        pMan.add(btnUploadManifesto, BorderLayout.EAST);
        gc.gridx = 1;
        gc.gridy = row++;
        gc.weightx = 1.0;
        panel.add(pMan, gc);

        // Bio
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        panel.add(new JLabel("Bio:", SwingConstants.RIGHT), gc);
        taBio = new JTextArea(6, 20);
        taBio.setLineWrap(true);
        taBio.setWrapStyleWord(true);
        JScrollPane spBio = new JScrollPane(taBio);
        gc.gridx = 1;
        gc.gridy = row++;
        gc.weightx = 1.0;
        panel.add(spBio, gc);

        // Save button
        btnSave = new JButton("Save Candidate");
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));
        btnSave.setBackground(ADMIN_PRIMARY);
        btnSave.setForeground(Color.BLACK); // Black Text
        btnSave.addActionListener(e -> saveCandidate());

        JPanel pSave = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pSave.setBackground(BACKGROUND);
        pSave.add(btnSave);
        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        panel.add(pSave, gc);

        return panel;
    }

    //Tab 2: Voting Window Control Panel
    private JPanel createVotingControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints rc = new GridBagConstraints();
        rc.insets = new Insets(10, 10, 10, 10);
        rc.anchor = GridBagConstraints.WEST;
        rc.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        // Current status
        rc.gridx = 0;
        rc.gridy = r;
        rc.gridwidth = 2;
        lblWindowStatus = new JLabel("Status: unknown");
        lblWindowStatus.setFont(new Font("Arial", Font.BOLD, 18));
        lblWindowStatus.setForeground(ADMIN_PRIMARY);
        panel.add(lblWindowStatus, rc);
        r++;

        // Start time
        rc.gridwidth = 1;
        rc.gridx = 0;
        rc.gridy = r;
        panel.add(new JLabel("Start (yyyy-MM-dd HH:mm:ss):", SwingConstants.RIGHT), rc);
        tfStartTime = new JTextField();
        rc.gridx = 1;
        rc.gridy = r++;
        rc.weightx = 1.0;
        panel.add(tfStartTime, rc);

        // End time
        rc.gridx = 0;
        rc.gridy = r;
        panel.add(new JLabel("End (yyyy-MM-dd HH:mm:ss):", SwingConstants.RIGHT), rc);
        tfEndTime = new JTextField();
        rc.gridx = 1;
        rc.gridy = r++;
        rc.weightx = 1.0;
        panel.add(tfEndTime, rc);

        // Hint label
        rc.gridx = 0;
        rc.gridy = r;
        rc.gridwidth = 2;
        JLabel hint = new JLabel("<html><i>Times must be in server timezone. Format: yyyy-MM-dd HH:mm:ss</i></html>");
        hint.setForeground(Color.GRAY);
        panel.add(hint, rc);
        r++;

        // Buttons: Activate / Deactivate / Load
        btnActivate = new JButton("Activate Window");
        btnActivate.setBackground(ADMIN_SECONDARY);
        btnActivate.setForeground(Color.BLACK); // Black Text
        btnActivate.addActionListener(e -> activateWindow());

        btnDeactivate = new JButton("Deactivate Window");
        btnDeactivate.setBackground(ADMIN_PRIMARY);
        btnDeactivate.setForeground(Color.BLACK); // Black Text
        btnDeactivate.addActionListener(e -> deactivateWindow());

        btnLoadWindow = new JButton("Refresh Status");
        btnLoadWindow.setBackground(ADMIN_ACCENT);
        btnLoadWindow.setForeground(Color.BLACK); // Black Text
        btnLoadWindow.addActionListener(e -> loadWindow());

        JPanel pButtons = new JPanel(new GridLayout(1, 3, 15, 15));
        pButtons.setBackground(BACKGROUND);
        pButtons.setBorder(new EmptyBorder(10, 0, 0, 0));
        pButtons.add(btnActivate);
        pButtons.add(btnDeactivate);
        pButtons.add(btnLoadWindow);

        rc.gridx = 0;
        rc.gridy = r;
        rc.gridwidth = 2;
        panel.add(pButtons, rc);

        return panel;
    }

    /**
     * Tab 3: Election Results Control Panel
     */
    private JPanel createResultsControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints rc = new GridBagConstraints();
        rc.insets = new Insets(10, 10, 10, 10);
        rc.anchor = GridBagConstraints.WEST;
        rc.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;

        // Current status
        rc.gridx = 0;
        rc.gridy = r;
        rc.gridwidth = 2;
        lblResultsStatus = new JLabel("Results: unknown");
        lblResultsStatus.setFont(new Font("Arial", Font.BOLD, 18));
        lblResultsStatus.setForeground(ADMIN_SECONDARY);
        panel.add(lblResultsStatus, rc);
        r++;

        // Buttons: Release / Hide
        btnReleaseResults = new JButton("RELEASE RESULTS");
        btnReleaseResults.setFont(new Font("Arial", Font.BOLD, 14));
        btnReleaseResults.setBackground(ADMIN_PRIMARY);
        btnReleaseResults.setForeground(Color.BLACK); // Black Text
        btnReleaseResults.addActionListener(e -> releaseResults());

        btnHideResults = new JButton("Hide Results");
        btnHideResults.setFont(new Font("Arial", Font.BOLD, 14));
        btnHideResults.setBackground(ADMIN_ACCENT);
        btnHideResults.setForeground(Color.BLACK); // Black Text
        btnHideResults.addActionListener(e -> hideResults());

        JPanel pButtons = new JPanel(new GridLayout(1, 2, 15, 15));
        pButtons.setBackground(BACKGROUND);
        pButtons.setBorder(new EmptyBorder(10, 0, 0, 0));
        pButtons.add(btnReleaseResults);
        pButtons.add(btnHideResults);

        rc.gridx = 0;
        rc.gridy = r;
        rc.gridwidth = 2;
        panel.add(pButtons, rc);

        return panel;
    }

    //Tab 4: System Maintenance Panel
    private JPanel createSystemControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints rc = new GridBagConstraints();
        rc.insets = new Insets(10, 10, 10, 10);
        rc.anchor = GridBagConstraints.WEST;
        rc.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;

        // Button: Prepare New Election
        btnPrepareNewElection = new JButton("WIPE DATA AND PREPARE NEW ELECTION");
        btnPrepareNewElection.setFont(new Font("Arial", Font.BOLD, 16));
        btnPrepareNewElection.setBackground(Color.RED.darker());
        btnPrepareNewElection.setForeground(Color.BLACK); // Black Text
        btnPrepareNewElection.addActionListener(e -> prepareNewElection());

        rc.gridx = 0;
        rc.gridy = r;
        rc.gridwidth = 1;
        rc.weightx = 1.0;
        panel.add(btnPrepareNewElection, rc);
        r++;

        rc.gridx = 0;
        rc.gridy = r;
        rc.gridwidth = 1;
        rc.weightx = 1.0;
        JLabel warning = new JLabel("<html><i style='color:#E02729;'>WARNING: This action is PERMANENT. It deletes ALL candidates, ALL votes, and resets all election status flags.</i></html>", SwingConstants.CENTER);
        panel.add(warning, rc);

        return panel;
    }

    private void selectPicture() {
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedPicture = fc.getSelectedFile();
            lblPicture.setText(selectedPicture.getName());
        }
    }

    private void selectManifesto() {
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedManifesto = fc.getSelectedFile();
            lblManifesto.setText(selectedManifesto.getName());
        }
    }

    private void saveCandidate() {
        String fullName = tfFullName.getText().trim();
        String year = tfAcademicYear.getText().trim();
        String school = tfSchool.getText().trim();
        String position = cbPosition.getSelectedItem().toString();
        String bio = taBio.getText().trim();

        if (fullName.isEmpty() || year.isEmpty() || school.isEmpty() || bio.isEmpty()
                || selectedPicture == null || selectedManifesto == null) {
            JOptionPane.showMessageDialog(this, "All fields and files are required!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String baseDir = "manifesto/" + fullName.replaceAll(" ", "_");
            new File(baseDir).mkdirs();

            String picPath = baseDir + "/" + selectedPicture.getName();
            String manifestoPath = baseDir + "/" + selectedManifesto.getName();
            Files.copy(selectedPicture.toPath(), new File(picPath).toPath());
            Files.copy(selectedManifesto.toPath(), new File(manifestoPath).toPath());

            Candidate candidate = new Candidate(0, fullName, year, school, position, picPath, manifestoPath, bio);

            String sql = "INSERT INTO candidates(full_name,academic_year,school,position,photo_path,manifesto_path,bio) VALUES(?,?,?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, candidate.getFullName());
            ps.setString(2, candidate.getAcademicYear());
            ps.setString(3, candidate.getSchool());
            ps.setString(4, candidate.getPosition());
            ps.setString(5, candidate.getPhotoPath());
            ps.setString(6, candidate.getManifestoPath());
            ps.setString(7, candidate.getBio());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Candidate uploaded successfully!");

            tfFullName.setText("");
            tfAcademicYear.setText("");
            tfSchool.setText("");
            taBio.setText("");
            cbPosition.setSelectedIndex(0);
            lblPicture.setText("No picture selected");
            lblManifesto.setText("No manifesto selected");
            selectedPicture = null;
            selectedManifesto = null;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private void loadWindow() {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT id, start_time, end_time, is_active, results_released FROM voting_window ORDER BY id LIMIT 1";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp tsStart = rs.getTimestamp("start_time");
                Timestamp tsEnd = rs.getTimestamp("end_time");
                boolean active = rs.getInt("is_active") == 1;
                boolean released = rs.getInt("results_released") == 1;

                tfStartTime.setText(tsStart.toLocalDateTime().format(DTF));
                tfEndTime.setText(tsEnd.toLocalDateTime().format(DTF));
                lblWindowStatus.setText("Status: " + (active ? "ACTIVE" : "INACTIVE"));
                lblResultsStatus.setText("Results: " + (released ? "RELEASED" : "HIDDEN"));
            } else {
                lblWindowStatus.setText("Status: not set");
                lblResultsStatus.setText("Results: not set");
            }
        } catch (Exception ex) {
            lblWindowStatus.setText("Status: error");
            lblResultsStatus.setText("Results: error");
        }
    }

    private void activateWindow() {
        String sStart = tfStartTime.getText().trim();
        String sEnd = tfEndTime.getText().trim();
        if (sStart.isEmpty() || sEnd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter start and end times in the format yyyy-MM-dd HH:mm:ss");
            return;
        }

        try {
            LocalDateTime start = LocalDateTime.parse(sStart, DTF);
            LocalDateTime end = LocalDateTime.parse(sEnd, DTF);
            if (!end.isAfter(start)) {
                JOptionPane.showMessageDialog(this, "End time must be after start time.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                String checkSql = "SELECT id FROM voting_window ORDER BY id LIMIT 1";
                PreparedStatement psCheck = con.prepareStatement(checkSql);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String upd = "UPDATE voting_window SET start_time=?, end_time=?, is_active=1 WHERE id=?";
                    PreparedStatement psUpd = con.prepareStatement(upd);
                    psUpd.setTimestamp(1, Timestamp.valueOf(start));
                    psUpd.setTimestamp(2, Timestamp.valueOf(end));
                    psUpd.setInt(3, id);
                    psUpd.executeUpdate();
                } else {
                    String ins = "INSERT INTO voting_window(start_time, end_time, is_active, results_released) VALUES(?,?,1,0)";
                    PreparedStatement psIns = con.prepareStatement(ins);
                    psIns.setTimestamp(1, Timestamp.valueOf(start));
                    psIns.setTimestamp(2, Timestamp.valueOf(end));
                    psIns.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Voting window activated.");
                loadWindow();
            }

        } catch (java.time.format.DateTimeParseException dtpe) {
            JOptionPane.showMessageDialog(this, "Bad date format. Use yyyy-MM-dd HH:mm:ss");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error activating window: " + ex.getMessage());
        }
    }

    private void deactivateWindow() {
        try (Connection con = DBConnection.getConnection()) {
            String checkSql = "SELECT id FROM voting_window ORDER BY id LIMIT 1";
            PreparedStatement psCheck = con.prepareStatement(checkSql);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String upd = "UPDATE voting_window SET is_active=0 WHERE id=?";
                PreparedStatement psUpd = con.prepareStatement(upd);
                psUpd.setInt(1, id);
                psUpd.executeUpdate();
                JOptionPane.showMessageDialog(this, "Voting window deactivated.");
                loadWindow();
            } else {
                JOptionPane.showMessageDialog(this, "No voting window to deactivate.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deactivating window: " + ex.getMessage());
        }
    }

    private void releaseResults() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to release the election results? Voters will see the winners.",
                "Confirm Release", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        if (VotingUtil.updateResultsReleaseStatus(true)) {
            JOptionPane.showMessageDialog(this, "Election results released successfully!");
            loadWindow();
        } else {
            JOptionPane.showMessageDialog(this, "Error releasing results.");
        }
    }

    private void hideResults() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to hide the election results? Voters will not see the winners.",
                "Confirm Hide", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        if (VotingUtil.updateResultsReleaseStatus(false)) {
            JOptionPane.showMessageDialog(this, "Election results hidden successfully!");
            loadWindow();
        } else {
            JOptionPane.showMessageDialog(this, "Error hiding results.");
        }
    }

    private static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) return;

        Files.walk(directory.toPath())
                .sorted(Comparator.reverseOrder())
                .map(java.nio.file.Path::toFile)
                .forEach(File::delete);
    }

    private void prepareNewElection() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "ARE YOU SURE? This will permanently delete ALL votes, ALL candidate data, and ALL manifesto folders.\n(Voter accounts will be kept intact.)",
                "CONFIRM DATA AND FOLDER DELETION",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection()) {

            // 1. Get list of directories to delete
            try (PreparedStatement ps = con.prepareStatement("SELECT photo_path FROM candidates")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String path = rs.getString("photo_path");
                    File photoFile = new File(path);
                    File candidateDir = photoFile.getParentFile();

                    if (candidateDir != null) {
                        deleteDirectory(candidateDir);
                    }
                }
            }

            //Database Cleanup
            try (PreparedStatement psDisableKeys = con.prepareStatement("SET FOREIGN_KEY_CHECKS=0")) {
                psDisableKeys.executeUpdate();
            }

            //Deletes all votes and reset auto-increment
            try (PreparedStatement psVotes = con.prepareStatement("TRUNCATE TABLE votes")) {
                psVotes.executeUpdate();
            }

            //Deletes all candidates and reset auto-increment
            try (PreparedStatement psCandidates = con.prepareStatement("TRUNCATE TABLE candidates")) {
                psCandidates.executeUpdate();
            }

            //Re-enables foreign key checks
            try (PreparedStatement psEnableKeys = con.prepareStatement("SET FOREIGN_KEY_CHECKS=1")) {
                psEnableKeys.executeUpdate();
            }

            // 5. Reset voting window status
            String resetSql = "UPDATE voting_window SET is_active=0, results_released=0";
            try (PreparedStatement psWindow = con.prepareStatement(resetSql)) {
                psWindow.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "System successfully prepared for a new election.\n(Candidates, Votes, and Folders deleted, Window reset.)");
            loadWindow();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error preparing new election: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}