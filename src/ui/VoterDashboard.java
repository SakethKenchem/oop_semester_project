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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoterDashboard extends JFrame {

    private int voterId;
    private JLabel titleLabel;
    private JPanel mainPanel;

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
        btnRefresh.addActionListener(e -> loadContent()); // Calls the new switching method
        southPanel.add(btnRefresh);

        // --- Logout Button ---
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> dispose());
        southPanel.add(btnLogout);

        add(southPanel, BorderLayout.SOUTH); // Add the button panel to the frame


        // Panel for CENTER region (Scrollable Content)
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(this.mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        loadContent();

        setVisible(true);
    }

    // NEW: Method to switch content based on results status
    private void loadContent() {
        if (VotingUtil.isResultsReleased()) {
            this.titleLabel.setText("Official Election Results");
            loadResults();
        } else {
            this.titleLabel.setText("Student Council Candidates");
            loadCandidates();
        }
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

            String[] positions = {
                    "Chairperson","Vice Chairperson","Secretary General","Finance Rep",
                    "Public Relations","Male Academic Rep","Female Academic Rep",
                    "Male Sports Rep","Female Sports Rep"
            };

            this.mainPanel.removeAll();

            boolean votingOpen = VotingUtil.isVotingOpen();

            for (String pos : positions) {
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

            this.mainPanel.revalidate();
            this.mainPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading candidates: " + e.getMessage());
        }
    }

    // RESTORED: Method to load and display results
    private void loadResults() {
        this.mainPanel.removeAll();

        try (Connection con = DBConnection.getConnection()) {

            // 1. Get all positions and the corresponding winner candidate ID and vote count
            String resultsSql = "SELECT position, candidate_id, COUNT(id) as total_votes " +
                    "FROM votes GROUP BY position, candidate_id ORDER BY position, total_votes DESC";
            ResultSet rsResults = con.createStatement().executeQuery(resultsSql);

            Map<String, List<Map.Entry<Integer, Integer>>> groupedResults = new HashMap<>();
            while (rsResults.next()) {
                String position = rsResults.getString("position");
                int candidateId = rsResults.getInt("candidate_id");
                int totalVotes = rsResults.getInt("total_votes");

                groupedResults.computeIfAbsent(position, k -> new ArrayList<>())
                        .add(Map.entry(candidateId, totalVotes));
            }

            // 2. Fetch all candidates for lookups
            Map<Integer, Candidate> candidateMap = new HashMap<>();
            ResultSet rsCandidates = con.createStatement().executeQuery("SELECT * FROM candidates");
            while(rsCandidates.next()) {
                Candidate c = new Candidate(
                        rsCandidates.getInt("id"),
                        rsCandidates.getString("full_name"),
                        rsCandidates.getString("academic_year"),
                        rsCandidates.getString("school"),
                        rsCandidates.getString("position"),
                        rsCandidates.getString("photo_path"),
                        rsCandidates.getString("manifesto_path"),
                        rsCandidates.getString("bio")
                );
                candidateMap.put(c.getId(), c);
            }

            String[] positions = {
                    "Chairperson","Vice Chairperson","Secretary General","Finance Rep",
                    "Public Relations","Male Academic Rep","Female Academic Rep",
                    "Male Sports Rep","Female Sports Rep"
            };

            for (String pos : positions) {
                if (groupedResults.containsKey(pos)) {
                    // Find the candidate(s) with the highest vote count for this position
                    List<Map.Entry<Integer, Integer>> candidatesForPos = groupedResults.get(pos);
                    // Sort to put the winner (max votes) first
                    candidatesForPos.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                    int winnerId = candidatesForPos.get(0).getKey();
                    int maxVotes = candidatesForPos.get(0).getValue();

                    Candidate winner = candidateMap.get(winnerId);
                    if (winner != null) {
                        this.mainPanel.add(createWinnerPanel(pos, winner, maxVotes));
                    }
                } else {
                    // Display for positions with no votes/candidates
                    JPanel posPanel = new JPanel();
                    posPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                    posPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.BLACK),
                            pos, TitledBorder.LEFT, TitledBorder.TOP,
                            new Font("Arial", Font.BOLD, 16)
                    ));
                    posPanel.add(new JLabel("No votes recorded for this position or no candidate."));
                    this.mainPanel.add(posPanel);
                }
            }

            this.mainPanel.revalidate();
            this.mainPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this.mainPanel, "Error loading results: " + e.getMessage());
        }
    }

    // RESTORED: Method to create a panel for the winner of a position
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

        // Left: Picture
        ImageIcon icon = new ImageIcon(c.getPhotoPath());
        Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        JLabel picLabel = new JLabel(new ImageIcon(img));
        picLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panel.add(picLabel, BorderLayout.WEST);

        // Center: Info
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
        info.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(info, BorderLayout.CENTER);

        // Right: Vote Count and Manifesto Button
        JPanel eastPanel = new JPanel(new BorderLayout());

        JLabel voteLabel = new JLabel("<html><center><b>Total Votes:<br>" + votes + "</b></center></html>", SwingConstants.CENTER);
        voteLabel.setFont(new Font("Arial", Font.BOLD, 28));
        voteLabel.setForeground(Color.BLUE.darker());
        voteLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

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

        ImageIcon icon = new ImageIcon(c.getPhotoPath());
        Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        panel.add(new JLabel(new ImageIcon(img)), BorderLayout.NORTH);

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

        JPanel buttons = new JPanel(new GridLayout(1,2,5,5));
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
        // Double-check voting window on server
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