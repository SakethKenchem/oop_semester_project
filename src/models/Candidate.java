package models;

public class Candidate {
    private int id;
    private String fullName;
    private String academicYear;
    private String school;
    private String position;
    private String photoPath;
    private String manifestoPath;
    private String bio;

    public Candidate() {}

    public Candidate(int id, String fullName, String academicYear, String school,
                     String position, String photoPath, String manifestoPath, String bio) {
        this.id = id;
        this.fullName = fullName;
        this.academicYear = academicYear;
        this.school = school;
        this.position = position;
        this.photoPath = photoPath;
        this.manifestoPath = manifestoPath;
        this.bio = bio;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getAcademicYear() { return academicYear; }
    public String getSchool() { return school; }
    public String getPosition() { return position; }
    public String getPhotoPath() { return photoPath; }
    public String getManifestoPath() { return manifestoPath; }
    public String getBio() { return bio; }

    public void setId(int id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public void setSchool(String school) { this.school = school; }
    public void setPosition(String position) { this.position = position; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public void setManifestoPath(String manifestoPath) { this.manifestoPath = manifestoPath; }
    public void setBio(String bio) { this.bio = bio; }
}
