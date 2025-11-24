package util;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class VotingUtil {

    // Defines a structure to hold the current election status retrieved from the database.
    public static class VotingWindow {
        public final LocalDateTime start;
        public final LocalDateTime end;
        public final boolean active;
        public final boolean resultsReleased;

        public VotingWindow(LocalDateTime start, LocalDateTime end, boolean active, boolean resultsReleased) {
            this.start = start;
            this.end = end;
            this.active = active;
            this.resultsReleased = resultsReleased;
        }
    }

    // Fetches the single status row from the 'voting_window' table.
    public static VotingWindow getActiveWindow() {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT start_time, end_time, is_active, results_released FROM voting_window LIMIT 1";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            Timestamp tsStart = rs.getTimestamp("start_time");
            Timestamp tsEnd = rs.getTimestamp("end_time");
            boolean active = rs.getInt("is_active") == 1;
            boolean released = rs.getInt("results_released") == 1;

            if (tsStart == null || tsEnd == null) return null;

            return new VotingWindow(tsStart.toLocalDateTime(), tsEnd.toLocalDateTime(), active, released);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Checks if the current time is within the active voting window.
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

    // Checks if the election results have been released by the administrator.
    public static boolean isResultsReleased() {
        try {
            VotingWindow w = getActiveWindow();
            return w != null && w.resultsReleased;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Updates the 'results_released' flag in the database.
    public static boolean updateResultsReleaseStatus(boolean released) {
        try (Connection con = DBConnection.getConnection()) {
            String checkSql = "SELECT id FROM voting_window LIMIT 1";
            PreparedStatement psCheck = con.prepareStatement(checkSql);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String upd = "UPDATE voting_window SET results_released=? WHERE id=?";
                PreparedStatement psUpd = con.prepareStatement(upd);
                psUpd.setInt(1, released ? 1 : 0);
                psUpd.setInt(2, id);
                psUpd.executeUpdate();
                return true;
            } else {
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