package models;

public class Voter {
    private int id;
    private String studentId;
    private String name;
    private String password;

    public Voter() {}

    public Voter(int id, String studentId, String name, String password) {
        this.id = id;
        this.studentId = studentId;
        this.name = name;
        this.password = password;
    }

    public int getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getPassword() { return password; }

    public void setId(int id) { this.id = id; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
}
