package Model;


public class Question {

    private int    id;
    private int    examId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;  // "A", "B", "C", or "D"
    private int    marks;

    // ── No-arg constructor ────────────────────────────────────────────────────
    public Question() {}

    // ── 8-param (no id) — for reading from DB, id set via setId() ────────────
    public Question(int examId, String questionText,
                    String optionA, String optionB,
                    String optionC, String optionD,
                    String correctOption, int marks) {
        this.examId        = examId;
        this.questionText  = questionText;
        this.optionA       = optionA;
        this.optionB       = optionB;
        this.optionC       = optionC;
        this.optionD       = optionD;
        this.correctOption = correctOption;
        this.marks         = marks;
    }

    // ── 9-param (with id) — used by AddQuestionServlet ────────────────────────
    public Question(int id, int examId, String questionText,
                    String optionA, String optionB,
                    String optionC, String optionD,
                    String correctOption, int marks) {
        this.id            = id;
        this.examId        = examId;
        this.questionText  = questionText;
        this.optionA       = optionA;
        this.optionB       = optionB;
        this.optionC       = optionC;
        this.optionD       = optionD;
        this.correctOption = correctOption;
        this.marks         = marks;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id = id; }

    public int    getExamId()                    { return examId; }
    public void   setExamId(int v)               { this.examId = v; }

    public String getQuestionText()              { return questionText; }
    public void   setQuestionText(String v)      { this.questionText = v; }

    public String getOptionA()                   { return optionA; }
    public void   setOptionA(String v)           { this.optionA = v; }

    public String getOptionB()                   { return optionB; }
    public void   setOptionB(String v)           { this.optionB = v; }

    public String getOptionC()                   { return optionC; }
    public void   setOptionC(String v)           { this.optionC = v; }

    public String getOptionD()                   { return optionD; }
    public void   setOptionD(String v)           { this.optionD = v; }

    public String getCorrectOption()             { return correctOption; }
    public void   setCorrectOption(String v)     { this.correctOption = v; }

    public int    getMarks()                     { return marks; }
    public void   setMarks(int v)                { this.marks = v; }

    @Override
    public String toString() {
        return "Question{id=" + id + ", examId=" + examId
             + ", correct=" + correctOption + ", marks=" + marks + "}";
    }
}