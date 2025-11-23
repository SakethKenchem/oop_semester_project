package ui;

import database.DBConnection;
import models.Candidate;
import util.VotingUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class VoterDashboard extends JFrame {

    private int voterId;

    // Panels for the tabs
    private JPanel pCandidatesContainer;
    private JPanel pResultsContainer;

    // Theme Colors (Voter Blue Theme)
    private static final Color VOTER_PRIMARY = new Color(0x4378BC);
    private static final Color VOTER_ACCENT = new Color(0x02338D);
    private static final Color BACKGROUND = new Color(250, 250, 250);
    private static final Color WINNER_COLOR = new Color(0, 150, 0); // Green for winner

    // Centralized positions
    private static final String[] POSITIONS = {
            "Chairperson", "Vice Chairperson", "Secretary General", "Finance Rep",
            "Public Relations", "Male Academic Rep", "Female Academic Rep",
            "Male Sports Rep", "Female Sports Rep"
    };

    public VoterDashboard(int voterId) {
        this.voterId = voterId;

        setTitle("Voter Dashboard - ID: " + voterId);
        setSize(950, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        // --- Header ---
        JLabel titleLabel = new JLabel("Student Council Election", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(VOTER_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(15, 10, 15, 10));
        add(titleLabel, BorderLayout.NORTH);

        // --- Tabs Setup ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 16));
        tabbedPane.setForeground(Color.BLACK);
        tabbedPane.setBackground(BACKGROUND);

        // 1. Initialize Container Panels
        pCandidatesContainer = new JPanel();
        pCandidatesContainer.setLayout(new BoxLayout(pCandidatesContainer, BoxLayout.Y_AXIS));
        pCandidatesContainer.setBackground(BACKGROUND);

        pResultsContainer = new JPanel();
        pResultsContainer.setLayout(new BoxLayout(pResultsContainer, BoxLayout.Y_AXIS));
        pResultsContainer.setBackground(BACKGROUND);

        // 2. Add Scrolls
        JScrollPane scrollCandidates = new JScrollPane(pCandidatesContainer);
        scrollCandidates.setBorder(null);
        scrollCandidates.getVerticalScrollBar().setUnitIncrement(16);

        JScrollPane scrollResults = new JScrollPane(pResultsContainer);
        scrollResults.setBorder(null);
        scrollResults.getVerticalScrollBar().setUnitIncrement(16);

        // 3. Add Tabs
        tabbedPane.addTab("Vote for Candidates", scrollCandidates);
        tabbedPane.addTab("Election Results", scrollResults);

        add(tabbedPane, BorderLayout.CENTER);

        // --- South Panel (Buttons) ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(BACKGROUND);

        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.setBackground(VOTER_PRIMARY);
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefresh.addActionListener(e -> refreshAllData());
        southPanel.add(btnRefresh);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(Color.GRAY);
        btnLogout.setForeground(Color.BLACK);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogout.addActionListener(e -> dispose());
        southPanel.add(btnLogout);

        add(southPanel, BorderLayout.SOUTH);

        // Initial Data Load
        refreshAllData();

        setVisible(true);
    }

    // --- Data Refresh Logic ---
    private void refreshAllData() {
        loadCandidates();
        loadResults();
    }

    // --- Tab 1: Load Candidates ---
    private void loadCandidates() {
        pCandidatesContainer.removeAll();

        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM candidates ORDER BY position");
            ArrayList<Candidate> candidates = new ArrayList<>();

            while (rs.next()) {
                candidates.add(new Candidate(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("academic_year"),
                        rs.getString("school"),
                        rs.getString("position"),
                        rs.getString("photo_path"),
                        rs.getString("manifesto_path"),
                        rs.getString("bio")
                ));
            }

            boolean votingOpen = VotingUtil.isVotingOpen();

            // Add a status banner
            JLabel statusLabel = new JLabel(votingOpen ? "Voting is currently OPEN" : "Voting is currently CLOSED");
            statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
            statusLabel.setForeground(votingOpen ? new Color(0, 150, 0) : Color.RED);
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            statusLabel.setBorder(new EmptyBorder(10,0,10,0));
            pCandidatesContainer.add(statusLabel);

            for (String pos : POSITIONS) {
                JPanel posPanel = new JPanel();
                posPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                posPanel.setBackground(BACKGROUND);
                posPanel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(VOTER_PRIMARY, 1),
                        pos, TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 18), VOTER_PRIMARY
                ));

                boolean hasCandidate = false;
                for (Candidate c : candidates) {
                    if (c.getPosition().equals(pos)) {
                        posPanel.add(createCandidatePanel(c, votingOpen));
                        hasCandidate = true;
                    }
                }

                if (hasCandidate) {
                    pCandidatesContainer.add(posPanel);
                    pCandidatesContainer.add(Box.createVerticalStrut(10));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        pCandidatesContainer.revalidate();
        pCandidatesContainer.repaint();
    }

    private JPanel createCandidatePanel(Candidate c, boolean votingOpen) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(260, 400));
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setBackground(Color.WHITE);

        // Robust Image Loading
        ImageIcon icon = null;
        File photoFile = new File(c.getPhotoPath());
        if (photoFile.exists()) {
            icon = new ImageIcon(photoFile.getAbsolutePath());
        }

        if (icon != null && icon.getImage() != null) {
            Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            JLabel picLabel = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
            picLabel.setBorder(new EmptyBorder(10,0,0,0));
            panel.add(picLabel, BorderLayout.NORTH);
        } else {
            JLabel placeholder = new JLabel("Photo Unavailable", SwingConstants.CENTER);
            placeholder.setPreferredSize(new Dimension(200, 200));
            placeholder.setForeground(Color.GRAY);
            placeholder.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            panel.add(placeholder, BorderLayout.NORTH);
        }

        JTextArea info = new JTextArea(
                "Name: " + c.getFullName() +
                        "\nYear: " + c.getAcademicYear() +
                        "\nSchool: " + c.getSchool() +
                        "\n\n\"" + (c.getBio().length() > 60 ? c.getBio().substring(0, 60) + "..." : c.getBio()) + "\""
        );
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setFont(new Font("SansSerif", Font.PLAIN, 12));
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.add(info, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 5));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(new EmptyBorder(5, 5, 5, 5));

        JButton btnDownload = new JButton("Manifesto");
        btnDownload.setBackground(VOTER_ACCENT);
        btnDownload.setForeground(Color.BLACK);
        btnDownload.addActionListener(e -> downloadManifesto(c));
        buttons.add(btnDownload);

        JButton btnVote = new JButton("Vote");
        btnVote.addActionListener(e -> castVote(c));
        btnVote.setEnabled(votingOpen);
        btnVote.setBackground(votingOpen ? VOTER_PRIMARY : Color.GRAY);
        btnVote.setForeground(Color.WHITE);
        buttons.add(btnVote);

        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    // --- Tab 2: Load Results ---
    private void loadResults() {
        pResultsContainer.removeAll();

        if (!VotingUtil.isResultsReleased()) {
            // Case: Results NOT released
            JPanel msgPanel = new JPanel(new GridBagLayout());
            msgPanel.setBackground(BACKGROUND);

            JLabel lblMsg = new JLabel("Election results have not been released yet.");
            lblMsg.setFont(new Font("Arial", Font.BOLD, 20));
            lblMsg.setForeground(Color.GRAY);

            msgPanel.add(lblMsg);
            msgPanel.setPreferredSize(new Dimension(800, 100));
            pResultsContainer.add(msgPanel);

        } else {
            // Case: Results ARE released
            JLabel header = new JLabel("ELECTION RESULTS", SwingConstants.CENTER);
            header.setFont(new Font("Arial", Font.BOLD, 24));
            header.setForeground(VOTER_PRIMARY);
            header.setAlignmentX(Component.CENTER_ALIGNMENT);
            header.setBorder(new EmptyBorder(20,0,20,0));
            pResultsContainer.add(header);

            try (Connection con = DBConnection.getConnection()) {

                // Loop through each position
                for (String pos : POSITIONS) {

                    //Find the winner ID
                    int winnerId = -1;
                    String winnerSql = "SELECT candidate_id, COUNT(*) as total FROM votes WHERE position=? GROUP BY candidate_id ORDER BY total DESC LIMIT 1";
                    try (PreparedStatement psW = con.prepareStatement(winnerSql)) {
                        psW.setString(1, pos);
                        ResultSet rsW = psW.executeQuery();
                        if (rsW.next()) {
                            winnerId = rsW.getInt("candidate_id");
                        }
                    }

                    //Get ALL candidates
                    String candidatesSql = "SELECT * FROM candidates WHERE position=?";
                    try (PreparedStatement psC = con.prepareStatement(candidatesSql)) {
                        psC.setString(1, pos);
                        ResultSet rsC = psC.executeQuery();
                        ArrayList<Candidate> posCandidates = new ArrayList<>();
                        while(rsC.next()) {
                            posCandidates.add(new Candidate(
                                    rsC.getInt("id"), rsC.getString("full_name"), rsC.getString("academic_year"),
                                    rsC.getString("school"), rsC.getString("position"), rsC.getString("photo_path"),
                                    rsC.getString("manifesto_path"), rsC.getString("bio")
                            ));
                        }

                        if (!posCandidates.isEmpty()) {
                            // Create Panel for this Position
                            JPanel posPanel = new JPanel();
                            posPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
                            posPanel.setBackground(BACKGROUND);
                            posPanel.setBorder(BorderFactory.createTitledBorder(
                                    BorderFactory.createLineBorder(Color.BLACK),
                                    pos, TitledBorder.LEFT, TitledBorder.TOP,
                                    new Font("Arial", Font.BOLD, 18), Color.BLACK
                            ));

                            //Loop candidates and get their specific vote count
                            for (Candidate c : posCandidates) {
                                int votes = 0;
                                String voteSql = "SELECT COUNT(*) FROM votes WHERE candidate_id=?";
                                try(PreparedStatement psV = con.prepareStatement(voteSql)) {
                                    psV.setInt(1, c.getId());
                                    ResultSet rsV = psV.executeQuery();
                                    if(rsV.next()) votes = rsV.getInt(1);
                                }

                                boolean isWinner = (c.getId() == winnerId && votes > 0);
                                posPanel.add(createResultCard(c, votes, isWinner));
                            }

                            pResultsContainer.add(posPanel);
                            pResultsContainer.add(Box.createVerticalStrut(15));
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        pResultsContainer.revalidate();
        pResultsContainer.repaint();
    }

    private JPanel createResultCard(Candidate c, int votes, boolean isWinner) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(220, 320));
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        if (isWinner) {
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(WINNER_COLOR, 4),
                    new EmptyBorder(5,5,5,5)
            ));
        } else {
            panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        }

        // Image
        ImageIcon icon = null;
        File photoFile = new File(c.getPhotoPath());
        if (photoFile.exists()) icon = new ImageIcon(photoFile.getAbsolutePath());

        if (icon != null && icon.getImage() != null) {
            Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            JLabel picLabel = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
            picLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            panel.add(picLabel, BorderLayout.NORTH);
        } else {
            JLabel placeholder = new JLabel("No Photo", SwingConstants.CENTER);
            placeholder.setPreferredSize(new Dimension(150, 150));
            placeholder.setForeground(Color.GRAY);
            panel.add(placeholder, BorderLayout.NORTH);
        }

        // Info
        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel nameLbl = new JLabel(c.getFullName(), SwingConstants.CENTER);
        nameLbl.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(nameLbl);

        if (isWinner) {
            JLabel winnerBadge = new JLabel("WINNER", SwingConstants.CENTER);
            winnerBadge.setFont(new Font("Arial", Font.BOLD, 14));
            winnerBadge.setForeground(WINNER_COLOR);
            infoPanel.add(winnerBadge);
        } else {
            infoPanel.add(new JLabel("")); // Spacer
        }

        // Vote Count
        JLabel votesLbl = new JLabel(votes + " Votes", SwingConstants.CENTER);
        votesLbl.setFont(new Font("Arial", Font.BOLD, 18));
        votesLbl.setForeground(isWinner ? WINNER_COLOR : Color.DARK_GRAY);
        infoPanel.add(votesLbl);

        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private void downloadManifesto(Candidate c) {
        try {
            File source = new File(c.getManifestoPath());
            if (!source.exists()) {
                JOptionPane.showMessageDialog(this, "Manifesto file missing on server.");
                return;
            }
            String filename = c.getFullName().replaceAll(" ", "_") + "_Manifesto.pdf";
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(filename));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                Files.copy(source.toPath(), fc.getSelectedFile().toPath());
                JOptionPane.showMessageDialog(this, "Download Successful!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void castVote(Candidate c) {
        if (!VotingUtil.isVotingOpen()) {
            JOptionPane.showMessageDialog(this, "Voting is currently closed.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm vote for " + c.getFullName() + " as " + c.getPosition() + "?",
                "Confirm Vote", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO votes (voter_id, candidate_id, position) VALUES (?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, voterId);
            ps.setInt(2, c.getId());
            ps.setString(3, c.getPosition());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Vote Cast Successfully!");
        } catch (java.sql.SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "You have already voted for this position!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}