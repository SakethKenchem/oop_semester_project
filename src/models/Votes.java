package models;

public class Votes {
    private int voteId;
    private int voterId;
    private String position;
    private int candidateId;

    public Votes(int voteId, int voterId, String position, int candidateId) {
        this.voteId = voteId;
        this.voterId = voterId;
        this.position = position;
        this.candidateId = candidateId;
    }

    public Votes(int voterId, String position, int candidateId) {
        this.voterId = voterId;
        this.position = position;
        this.candidateId = candidateId;
    }

    // Getters + setters
    public int getVoteId() {
        return voteId;
    }

    public int getVoterId() {
        return voterId;
    }

    public String getPosition() {
        return position;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setVoteId(int voteId) {
        this.voteId = voteId;
    }

    public void setVoterId(int voterId) {
        this.voterId = voterId;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }
}
