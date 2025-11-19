package ui;

import database.DBConnection;
import models.Candidate;
import util.VotingUtil;

import javax.swing.*;
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

    private JTextField tfFullName, tfAcademicYear, tfSchool;
    private JTextArea taBio;
    private JComboBox<String> cbPosition;
    private JLabel lblPicture, lblManifesto;
    private File selectedPicture, selectedManifesto;
    private JButton btnUploadPicture, btnUploadManifesto, btnSave;

    private JTextField tfStartTime, tfEndTime;
    private JButton btnActivate, btnDeactivate, btnLoadWindow;
    private JLabel lblWindowStatus;

    private JButton btnReleaseResults, btnHideResults;
    private JLabel lblResultsStatus;

    private JButton btnPrepareNewElection;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] POSITIONS = {
            "Chairperson","Vice Chairperson","Secretary General","Finance Rep",
            "Public Relations","Male Academic Rep","Female Academic Rep",
            "Male Sports Rep","Female Sports Rep"
    };

    public AdminDashboard(String adminUsername) {
        setTitle("Admin Dashboard - Logged in as: " + adminUsername);
        setSize(950, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel header = new JLabel("Upload Candidate, Voting & Results Control", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.65);
        add(split, BorderLayout.CENTER);

        JPanel left = createCandidateUploadPanel();
        split.setLeftComponent(left);

        JPanel rightContainer = new JPanel();
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));

        JPanel votingControl = createVotingControlPanel();
        rightContainer.add(votingControl);

        JPanel resultsControl = createResultsControlPanel();
        rightContainer.add(Box.createVerticalStrut(15));
        rightContainer.add(resultsControl);

        JPanel systemControl = createSystemControlPanel();
        rightContainer.add(Box.createVerticalStrut(15));
        rightContainer.add(systemControl);

        split.setRightComponent(rightContainer);

        loadWindow();

        setVisible(true);
    }

    private JPanel createCandidateUploadPanel() {
        JPanel left = new JPanel(new GridBagLayout());
        left.setBorder(BorderFactory.createTitledBorder("Upload Candidate"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        left.add(new JLabel("Full Name:"), gc);
        tfFullName = new JTextField();
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1.0;
        left.add(tfFullName, gc);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        left.add(new JLabel("Academic Year:"), gc);
        tfAcademicYear = new JTextField();
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1.0;
        left.add(tfAcademicYear, gc);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        left.add(new JLabel("School:"), gc);
        tfSchool = new JTextField();
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1.0;
        left.add(tfSchool, gc);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        left.add(new JLabel("Position:"), gc);
        cbPosition = new JComboBox<>(POSITIONS);
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1.0;
        left.add(cbPosition, gc);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        left.add(new JLabel("Picture:"), gc);
        JPanel pPic = new JPanel(new BorderLayout(6,0));
        lblPicture = new JLabel("No picture selected");
        btnUploadPicture = new JButton("Choose...");
        btnUploadPicture.addActionListener(e -> selectPicture());
        pPic.add(lblPicture, BorderLayout.CENTER);
        pPic.add(btnUploadPicture, BorderLayout.EAST);
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1.0;
        left.add(pPic, gc);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        left.add(new JLabel("Manifesto:"), gc);
        JPanel pMan = new JPanel(new BorderLayout(6,0));
        lblManifesto = new JLabel("No manifesto selected");
        btnUploadManifesto = new JButton("Choose...");
        btnUploadManifesto.addActionListener(e -> selectManifesto());
        pMan.add(lblManifesto, BorderLayout.CENTER);
        pMan.add(btnUploadManifesto, BorderLayout.EAST);
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1.0;
        left.add(pMan, gc);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        left.add(new JLabel("Bio:"), gc);
        taBio = new JTextArea(6, 20);
        taBio.setLineWrap(true);
        taBio.setWrapStyleWord(true);
        JScrollPane spBio = new JScrollPane(taBio);
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1.0;
        left.add(spBio, gc);

        btnSave = new JButton("Save Candidate");
        btnSave.addActionListener(e -> saveCandidate());
        JPanel pSave = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pSave.add(btnSave);
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        left.add(pSave, gc);

        return left;
    }

    private JPanel createVotingControlPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBorder(BorderFactory.createTitledBorder("Voting Window Control"));
        GridBagConstraints rc = new GridBagConstraints();
        rc.insets = new Insets(8,8,8,8);
        rc.anchor = GridBagConstraints.WEST;
        rc.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        rc.gridx = 0; rc.gridy = r; rc.gridwidth = 2;
        lblWindowStatus = new JLabel("Status: unknown");
        lblWindowStatus.setFont(new Font("Arial", Font.BOLD, 14));
        right.add(lblWindowStatus, rc);
        r++;

        rc.gridwidth = 1;
        rc.gridx = 0; rc.gridy = r;
        right.add(new JLabel("Start (yyyy-MM-dd HH:mm:ss):"), rc);
        tfStartTime = new JTextField();
        rc.gridx = 1; rc.gridy = r++; rc.weightx = 1.0;
        right.add(tfStartTime, rc);

        rc.gridx = 0; rc.gridy = r;
        right.add(new JLabel("End (yyyy-MM-dd HH:mm:ss):"), rc);
        tfEndTime = new JTextField();
        rc.gridx = 1; rc.gridy = r++; rc.weightx = 1.0;
        right.add(tfEndTime, rc);

        btnActivate = new JButton("Activate Window");
        btnActivate.addActionListener(e -> activateWindow());
        btnDeactivate = new JButton("Deactivate Window");
        btnDeactivate.addActionListener(e -> deactivateWindow());
        btnLoadWindow = new JButton("Refresh Status");
        btnLoadWindow.addActionListener(e -> loadWindow());

        JPanel pButtons = new JPanel(new GridLayout(1,3,8,8));
        pButtons.add(btnActivate);
        pButtons.add(btnDeactivate);
        pButtons.add(btnLoadWindow);

        rc.gridx = 0; rc.gridy = r; rc.gridwidth = 2;
        right.add(pButtons, rc);
        r++;

        rc.gridx = 0; rc.gridy = r; rc.gridwidth = 2;
        JLabel hint = new JLabel("<html><i>Note: times must be in server timezone.<br/>Format: yyyy-MM-dd HH:mm:ss</i></html>");
        right.add(hint, rc);

        return right;
    }

    private JPanel createResultsControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Election Results Control"));
        GridBagConstraints rc = new GridBagConstraints();
        rc.insets = new Insets(8,8,8,8);
        rc.anchor = GridBagConstraints.WEST;
        rc.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;

        rc.gridx = 0; rc.gridy = r; rc.gridwidth = 2;
        lblResultsStatus = new JLabel("Results: unknown");
        lblResultsStatus.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblResultsStatus, rc);
        r++;

        btnReleaseResults = new JButton("Release Results");
        btnReleaseResults.addActionListener(e -> releaseResults());
        btnHideResults = new JButton("Hide Results");
        btnHideResults.addActionListener(e -> hideResults());

        JPanel pButtons = new JPanel(new GridLayout(1,2,8,8));
        pButtons.add(btnReleaseResults);
        pButtons.add(btnHideResults);

        rc.gridx = 0; rc.gridy = r; rc.gridwidth = 2;
        panel.add(pButtons, rc);
        r++;

        return panel;
    }

    private JPanel createSystemControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("System Maintenance"));
        GridBagConstraints rc = new GridBagConstraints();
        rc.insets = new Insets(8,8,8,8);
        rc.anchor = GridBagConstraints.WEST;
        rc.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;

        btnPrepareNewElection = new JButton("PREPARE NEW ELECTION");
        btnPrepareNewElection.setBackground(Color.RED);
        btnPrepareNewElection.setForeground(Color.WHITE);
        btnPrepareNewElection.setFont(new Font("Arial", Font.BOLD, 14));
        btnPrepareNewElection.addActionListener(e -> prepareNewElection());

        rc.gridx = 0; rc.gridy = r; rc.gridwidth = 1; rc.weightx = 1.0;
        panel.add(btnPrepareNewElection, rc);
        r++;

        rc.gridx = 0; rc.gridy = r; rc.gridwidth = 1; rc.weightx = 1.0;
        JLabel warning = new JLabel("<html><i style='color:red;'>WARNING: This deletes ALL candidates/votes and resets the window.</i></html>", SwingConstants.CENTER);
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
            ex.printStackTrace();
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

            // 1. Get list of directories to delete before truncating the table
            try (PreparedStatement ps = con.prepareStatement("SELECT photo_path FROM candidates")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String path = rs.getString("photo_path");
                    // Assuming photo_path is "manifesto/Candidate_Name/photo.jpg"
                    File photoFile = new File(path);
                    File candidateDir = photoFile.getParentFile();

                    if (candidateDir != null) {
                        deleteDirectory(candidateDir); // Call recursive delete
                    }
                }
            }

            // 2. Delete all votes and reset auto-increment
            try (PreparedStatement psVotes = con.prepareStatement("TRUNCATE TABLE votes")) {
                psVotes.executeUpdate();
            }

            // 3. Delete all candidates and reset auto-increment
            try (PreparedStatement psCandidates = con.prepareStatement("TRUNCATE TABLE candidates")) {
                psCandidates.executeUpdate();
            }

            // 4. Reset voting window status
            String resetSql = "UPDATE voting_window SET is_active=0, results_released=0";
            try (PreparedStatement psWindow = con.prepareStatement(resetSql)) {
                psWindow.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "System successfully prepared for a new election.\n(Candidates, Votes, and Folders deleted, Window reset.)");
            loadWindow();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error preparing new election: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}