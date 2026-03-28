package Model;

public class Result {
    private int    id;
    private int    studentId;
    private int    examId;
    private int    score;
    private int    totalMarks;
    private String attemptedAt;
    // Extra fields populated by joins
    private String studentName;
    private String rollNo;
    private String examTitle;

    public Result() {}

    public Result(int studentId, int examId, int score, int totalMarks) {
        this.studentId  = studentId;
        this.examId     = examId;
        this.score      = score;
        this.totalMarks = totalMarks;
    }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id = id; }

    public int    getStudentId()                 { return studentId; }
    public void   setStudentId(int v)            { this.studentId = v; }

    public int    getExamId()                    { return examId; }
    public void   setExamId(int v)               { this.examId = v; }

    public int    getScore()                     { return score; }
    public void   setScore(int v)                { this.score = v; }

    public int    getTotalMarks()                { return totalMarks; }
    public void   setTotalMarks(int v)           { this.totalMarks = v; }

    public String getAttemptedAt()               { return attemptedAt; }
    public void   setAttemptedAt(String v)       { this.attemptedAt = v; }

    public String getStudentName()               { return studentName; }
    public void   setStudentName(String v)       { this.studentName = v; }

    public String getRollNo()                    { return rollNo; }
    public void   setRollNo(String v)            { this.rollNo = v; }

    public String getExamTitle()                 { return examTitle; }
    public void   setExamTitle(String v)         { this.examTitle = v; }
}