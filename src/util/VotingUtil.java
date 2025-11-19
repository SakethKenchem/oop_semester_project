package util;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class VotingUtil {

    public static class VotingWindow {
        public final LocalDateTime start;
        public final LocalDateTime end;
        public final boolean active;
        public final boolean resultsReleased; // Re-added field

        public VotingWindow(LocalDateTime start, LocalDateTime end, boolean active, boolean resultsReleased) {
            this.start = start;
            this.end = end;
            this.active = active;
            this.resultsReleased = resultsReleased;
        }
    }

    /**
     * Returns the active voting window and control status (is_active, results_released).
     */
    public static VotingWindow getActiveWindow() {
        try (Connection con = DBConnection.getConnection()) {
            // Updated SQL to fetch results_released
            String sql = "SELECT start_time, end_time, is_active, results_released FROM voting_window LIMIT 1";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            Timestamp tsStart = rs.getTimestamp("start_time");
            Timestamp tsEnd = rs.getTimestamp("end_time");
            boolean active = rs.getInt("is_active") == 1;
            boolean released = rs.getInt("results_released") == 1; // Re-added fetch

            if (tsStart == null || tsEnd == null) return null;

            return new VotingWindow(tsStart.toLocalDateTime(), tsEnd.toLocalDateTime(), active, released); // Updated constructor call
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Quick check: is current time inside the active window?
     */
    public static boolean isVotingOpen() {
        try {
            VotingWindow w = getActiveWindow();
            if (w == null || !w.active) return false;
            LocalDateTime now = LocalDateTime.now();
            return (now.isEqual(w.start) || now.isAfter(w.start)) && now.isBefore(w.end);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * New check: are the election results released?
     */
    public static boolean isResultsReleased() {
        try {
            VotingWindow w = getActiveWindow();
            return w != null && w.resultsReleased;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * New method to update the results_released status.
     * @param released true to release, false to hide.
     */
    public static boolean updateResultsReleaseStatus(boolean released) {
        try (Connection con = DBConnection.getConnection()) {
            String checkSql = "SELECT id FROM voting_window LIMIT 1";
            PreparedStatement psCheck = con.prepareStatement(checkSql);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                // Update existing row
                String upd = "UPDATE voting_window SET results_released=? WHERE id=?";
                PreparedStatement psUpd = con.prepareStatement(upd);
                psUpd.setInt(1, released ? 1 : 0);
                psUpd.setInt(2, id);
                psUpd.executeUpdate();
                return true;
            } else {
                // Insert a default row if it doesn't exist
                String ins = "INSERT INTO voting_window(start_time, end_time, is_active, results_released) VALUES(NOW(),NOW(),0,?)";
                PreparedStatement psIns = con.prepareStatement(ins);
                psIns.setInt(1, released ? 1 : 0);
                psIns.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}