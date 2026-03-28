package Model;

/**
 * Student.java
 * ------------
 * Plain Java object mirroring the `students` table.
 * All fields map 1-to-1 with the DB columns.
 */
public class Student {

    private int    id;
    private String fullName;
    private String rollNo;
    private String email;
    private String password;      // BCrypt hash — never plain-text
    private String collegeName;
    private String classLevel;    // "1"-"12", "UG", "PG"
    private String stream;        // null for Class 1-10
    private String course;        // null unless UG
    private String createdAt;
	private int collegeId;

    // ── Constructors ────────────────────────────────────────────────────────────

    public Student() {}

    /** Constructor used when registering a new student. */
    public Student(String fullName, String rollNo, String email,
                   String password, String collegeName,
                   String classLevel, String stream, String course) {
        this.fullName    = fullName;
        this.rollNo      = rollNo;
        this.email       = email;
        this.password    = password;
        this.collegeName = collegeName;
        this.classLevel  = classLevel;
        this.stream      = stream;
        this.course      = course;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────────

    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public String getFullName()               { return fullName; }
    public void   setFullName(String v)       { this.fullName = v; }

    public String getRollNo()                 { return rollNo; }
    public void   setRollNo(String v)         { this.rollNo = v; }

    public String getEmail()                  { return email; }
    public void   setEmail(String v)          { this.email = v; }

    public String getPassword()               { return password; }
    public void   setPassword(String v)       { this.password = v; }

    public String getCollegeName()            { return collegeName; }
    public void   setCollegeName(String v)    { this.collegeName = v; }

    public int    getCollegeId()              { return collegeId; }
    public void   setCollegeId(int v)         { this.collegeId = v; }

    public String getClassLevel()             { return classLevel; }
    public void   setClassLevel(String v)     { this.classLevel = v; }

    public String getStream()                 { return stream; }
    public void   setStream(String v)         { this.stream = v; }

    public String getCourse()                 { return course; }
    public void   setCourse(String v)         { this.course = v; }

    public String getCreatedAt()              { return createdAt; }
    public void   setCreatedAt(String v)      { this.createdAt = v; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", rollNo='" + rollNo
             + "', email='" + email + "', class='" + classLevel + "'}";
    }
}