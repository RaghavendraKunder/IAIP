package Model;

public class Batch {
    private int    id;
    private String name;
    private String standard;
    private String stream;
    private String division;
    private String course;
    private String collegeName;
    private int    adminId;
    private String createdAt;
    private int    studentCount; // populated by JOIN

    public Batch() {}

    public int    getId()                    { return id; }
    public void   setId(int v)               { this.id = v; }
    public String getName()                  { return name; }
    public void   setName(String v)          { this.name = v; }
    public String getStandard()              { return standard; }
    public void   setStandard(String v)      { this.standard = v; }
    public String getStream()                { return stream; }
    public void   setStream(String v)        { this.stream = v; }
    public String getDivision()              { return division; }
    public void   setDivision(String v)      { this.division = v; }
    public String getCourse()                { return course; }
    public void   setCourse(String v)        { this.course = v; }
    public String getCollegeName()           { return collegeName; }
    public void   setCollegeName(String v)   { this.collegeName = v; }
    public int    getAdminId()               { return adminId; }
    public void   setAdminId(int v)          { this.adminId = v; }
    public String getCreatedAt()             { return createdAt; }
    public void   setCreatedAt(String v)     { this.createdAt = v; }
    public int    getStudentCount()          { return studentCount; }
    public void   setStudentCount(int v)     { this.studentCount = v; }
}
