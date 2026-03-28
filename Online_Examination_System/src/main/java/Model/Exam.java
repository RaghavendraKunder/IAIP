package Model;

/**
 * Exam.java
 * ---------
 * Mirrors the `exams` table including deadline + published flag.
 */
public class Exam {

    private int     id;
    private String  title;
    private int     durationMinutes;
    private int     totalMarks;
    private String  createdAt;
    private String  deadline;
    private boolean isPublished;
    private String  collegeName;

    // ── No-arg constructor ────────────────────────────────────────────────────
    public Exam() {}

    // ── 3-param constructor (used by AddExamServlet) ──────────────────────────
    public Exam(String title, int durationMinutes, int totalMarks) {
        this.title           = title;
        this.durationMinutes = durationMinutes;
        this.totalMarks      = totalMarks;
    }

    // ── 5-param constructor (with deadline + published) ───────────────────────
    public Exam(String title, int durationMinutes, int totalMarks,
                String deadline, boolean isPublished) {
        this.title           = title;
        this.durationMinutes = durationMinutes;
        this.totalMarks      = totalMarks;
        this.deadline        = deadline;
        this.isPublished     = isPublished;
    }

    // ── Full constructor (all fields) ─────────────────────────────────────────
    public Exam(int id, String title, int durationMinutes, int totalMarks,
                String createdAt, String deadline, boolean isPublished) {
        this.id              = id;
        this.title           = title;
        this.durationMinutes = durationMinutes;
        this.totalMarks      = totalMarks;
        this.createdAt       = createdAt;
        this.deadline        = deadline;
        this.isPublished     = isPublished;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int     getId()                      { return id; }
    public void    setId(int id)                { this.id = id; }

    public String  getTitle()                   { return title; }
    public void    setTitle(String v)           { this.title = v; }

    public int     getDurationMinutes()         { return durationMinutes; }
    public void    setDurationMinutes(int v)    { this.durationMinutes = v; }

    public int     getTotalMarks()              { return totalMarks; }
    public void    setTotalMarks(int v)         { this.totalMarks = v; }

    public String  getCreatedAt()               { return createdAt; }
    public void    setCreatedAt(String v)       { this.createdAt = v; }

    public String  getDeadline()                { return deadline; }
    public void    setDeadline(String v)        { this.deadline = v; }

    public boolean isPublished()                { return isPublished; }
    public void    setPublished(boolean v)      { this.isPublished = v; }

    public String  getCollegeName()             { return collegeName; }
    public void    setCollegeName(String v)     { this.collegeName = v; }

    // ── Helper: is this exam past its deadline? ───────────────────────────────
    public boolean isExpired() {
        if (deadline == null || deadline.isEmpty()) return false;
        try {
            java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date dl = sdf.parse(deadline);
            return new java.util.Date().after(dl);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Exam{id=" + id + ", title=" + title
             + ", deadline=" + deadline
             + ", published=" + isPublished + "}";
    }
}