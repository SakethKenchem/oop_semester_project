package ui;

import database.DBConnection;
import models.Candidate;
import util.VotingUtil;

import javax.swing.*;
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
    private JLabel titleLabel;
    private JPanel mainPanel;

    // Centralized constant for all positions
    private static final String[] POSITIONS = {
            "Chairperson", "Vice Chairperson", "Secretary General", "Finance Rep",
            "Public Relations", "Male Academic Rep", "Female Academic Rep",
            "Male Sports Rep", "Female Sports Rep"
    };

    public VoterDashboard(int voterId) {
        this.voterId = voterId;

        setTitle("Voter Dashboard - ID: " + voterId);
        setSize(950, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel for NORTH region (Title)
        this.titleLabel = new JLabel("Student Council Candidates", SwingConstants.CENTER);
        this.titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(this.titleLabel, BorderLayout.NORTH);

        // Panel for SOUTH region (Logout + Refresh)
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // --- Refresh Button ---
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadContent());
        southPanel.add(btnRefresh);

        // --- Logout Button ---
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> dispose());
        southPanel.add(btnLogout);

        add(southPanel, BorderLayout.SOUTH);

        // Panel for CENTER region (Scrollable Content)
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(this.mainPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadContent(); // Initial content load

        setVisible(true);
    }

    // method for content loading and UI refresh
    private void loadContent() {
        this.mainPanel.removeAll();

        if (VotingUtil.isResultsReleased()) {
            this.titleLabel.setText("Official Election Results");
            loadResults();
        } else {
            this.titleLabel.setText("Student Council Candidates");
            loadCandidates();
        }

        this.mainPanel.revalidate();
        this.mainPanel.repaint();
    }

    private void loadCandidates() {
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

            for (String pos : POSITIONS) {
                JPanel posPanel = new JPanel();
                posPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                posPanel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.BLACK),
                        pos, TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 16)
                ));

                boolean hasCandidate = false;
                for (Candidate c : candidates) {
                    if (c.getPosition().equals(pos)) {
                        posPanel.add(createCandidatePanel(c, votingOpen));
                        hasCandidate = true;
                    }
                }

                if (hasCandidate) this.mainPanel.add(posPanel);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading candidates: " + e.getMessage());
        }
    }

    //Loads the election results and displays the winners in each position

    private void loadResults() {

        try (Connection con = DBConnection.getConnection()) {

            // Finds the winner's candidate_id and total_votes for a specific position
            String winnerSql = "SELECT candidate_id, COUNT(id) AS total_votes " +
                    "FROM votes WHERE position = ? " +
                    "GROUP BY candidate_id ORDER BY total_votes DESC LIMIT 1";

            //Gets the full candidate details based on the winner's ID
            String candidateSql = "SELECT * FROM candidates WHERE id = ?";

            PreparedStatement psWinner = con.prepareStatement(winnerSql);
            PreparedStatement psCandidate = con.prepareStatement(candidateSql);

            for (String pos : POSITIONS) { // Use static constant
                psWinner.setString(1, pos);

                try (ResultSet rsWinner = psWinner.executeQuery()) {
                    if (rsWinner.next()) {
                        int winnerId = rsWinner.getInt("candidate_id");
                        int maxVotes = rsWinner.getInt("total_votes");

                        // Fetches Candidate details
                        psCandidate.setInt(1, winnerId);
                        try (ResultSet rsCandidate = psCandidate.executeQuery()) {
                            if (rsCandidate.next()) {
                                Candidate winner = new Candidate(
                                        rsCandidate.getInt("id"),
                                        rsCandidate.getString("full_name"),
                                        rsCandidate.getString("academic_year"),
                                        rsCandidate.getString("school"),
                                        rsCandidate.getString("position"),
                                        rsCandidate.getString("photo_path"),
                                        rsCandidate.getString("manifesto_path"),
                                        rsCandidate.getString("bio")
                                );
                                this.mainPanel.add(createWinnerPanel(pos, winner, maxVotes));
                            }
                        }
                    } else {
                        //
                        JPanel posPanel = new JPanel();
                        posPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                        posPanel.setBorder(BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(Color.BLACK),
                                pos, TitledBorder.LEFT, TitledBorder.TOP,
                                new Font("Arial", Font.BOLD, 16)
                        ));
                        posPanel.add(new JLabel("No votes recorded for this position."));
                        this.mainPanel.add(posPanel);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this.mainPanel, "Error loading results: " + e.getMessage());
        }
    }

    // A panel for the winner of a position
    private JPanel createWinnerPanel(String position, Candidate c, int votes) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GREEN.darker(), 2), // Highlight winner
                position + " - WINNER", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18), Color.GREEN.darker()
        ));
        panel.setMaximumSize(new Dimension(850, 300));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);


        ImageIcon icon = null;
        File photoFile = new File(c.getPhotoPath());

        if (photoFile.exists()) {
            icon = new ImageIcon(photoFile.getAbsolutePath());
        }

        if (icon != null && icon.getImage() != null) {
            Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            JLabel picLabel = new JLabel(new ImageIcon(img));
            picLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(picLabel, BorderLayout.WEST);
        } else {
            JLabel placeholder = new JLabel("Image Missing", SwingConstants.CENTER);
            placeholder.setPreferredSize(new Dimension(170, 170));
            placeholder.setBorder(BorderFactory.createLineBorder(Color.RED));
            panel.add(placeholder, BorderLayout.WEST);
        }

        JTextArea info = new JTextArea(
                "Name: " + c.getFullName() +
                        "\nSchool: " + c.getSchool() +
                        "\nYear: " + c.getAcademicYear() +
                        "\nBio: " + c.getBio()
        );
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setBackground(null);
        panel.add(info, BorderLayout.CENTER);

        // Right: Vote Count and Manifesto Button
        JPanel eastPanel = new JPanel(new BorderLayout());

        JLabel voteLabel = new JLabel("<html><center><b>Total Votes:<br>" + votes + "</b></center></html>", SwingConstants.CENTER);
        voteLabel.setFont(new Font("Arial", Font.BOLD, 28));
        voteLabel.setForeground(Color.BLUE.darker());
        voteLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnDownload = new JButton("Download Manifesto");
        btnDownload.addActionListener(e -> downloadManifesto(c));

        eastPanel.add(voteLabel, BorderLayout.NORTH);
        eastPanel.add(btnDownload, BorderLayout.SOUTH);
        eastPanel.setPreferredSize(new Dimension(200, 150));

        panel.add(eastPanel, BorderLayout.EAST);

        return panel;
    }


    private JPanel createCandidatePanel(Candidate c, boolean votingOpen) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(250, 380));
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        ImageIcon icon = null;
        File photoFile = new File(c.getPhotoPath());

        if (photoFile.exists()) {
            icon = new ImageIcon(photoFile.getAbsolutePath());
        }

        if (icon != null && icon.getImage() != null) {
            Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            panel.add(new JLabel(new ImageIcon(img)), BorderLayout.NORTH);
        } else {
            JLabel placeholder = new JLabel("Image Missing", SwingConstants.CENTER);
            placeholder.setPreferredSize(new Dimension(200, 200));
            placeholder.setBorder(BorderFactory.createLineBorder(Color.RED));
            panel.add(placeholder, BorderLayout.NORTH);
        }

        JTextArea info = new JTextArea(
                "Name: " + c.getFullName() +
                        "\nSchool: " + c.getSchool() +
                        "\nYear: " + c.getAcademicYear() +
                        "\nBio: " + c.getBio()
        );
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setBackground(null);
        panel.add(info, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 5));
        JButton btnDownload = new JButton("Download Manifesto");
        btnDownload.addActionListener(e -> downloadManifesto(c));
        buttons.add(btnDownload);

        JButton btnVote = new JButton("Vote");
        btnVote.addActionListener(e -> castVote(c));
        btnVote.setEnabled(votingOpen);
        if (!votingOpen) btnVote.setToolTipText("Voting is currently closed.");
        buttons.add(btnVote);

        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private void downloadManifesto(Candidate c) {
        try {
            File source = new File(c.getManifestoPath());
            if (!source.exists()) {
                JOptionPane.showMessageDialog(this, "Manifesto not found!");
                return;
            }

            String filename = c.getFullName().replaceAll(" ", "_") + "_" +
                    c.getPosition().replaceAll(" ", "_") + "_manifesto.pdf";

            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(filename));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File dest = fc.getSelectedFile();
                Files.copy(source.toPath(), dest.toPath());
                JOptionPane.showMessageDialog(this, "Manifesto saved as " + dest.getName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void castVote(Candidate c) {
        // checks the voting window on server
        if (!VotingUtil.isVotingOpen()) {
            JOptionPane.showMessageDialog(this, "Voting is currently closed.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to vote for " + c.getFullName() + " as " + c.getPosition() + "?\nYou cannot retract your vote.",
                "Confirm Vote", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO votes (voter_id, candidate_id, position) VALUES (?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, voterId);
            ps.setInt(2, c.getId());
            ps.setString(3, c.getPosition());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Vote cast successfully for " + c.getFullName() + "!");
        } catch (java.sql.SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "You have already voted for " + c.getPosition() + "!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error casting vote: " + ex.getMessage());
        }
    }
}