// service/ExamService.java
package service;

import dao.ExamDAO;
import dao.QuestionDAO;
import Model.Exam;
import Model.Question;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class ExamService {

    private final ExamDAO     examDAO     = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();

    // ── Get single exam ───────────────────────────────────────────────────────────
    public ExamResult getExam(int examId) {
        try {
            Exam exam = examDAO.getExamById(examId);
            if (exam == null)
                return ExamResult.fail("Exam not found.");
            return ExamResult.success(exam);
        } catch (SQLException e) {
            e.printStackTrace();
            return ExamResult.fail("Database error loading exam.");
        }
    }

    // ── Get shuffled questions ────────────────────────────────────────────────────
    public QuestionResult getShuffledQuestions(int examId) throws SQLException {
        List<Question> questions = questionDAO.getQuestionsByExamId(examId);
		if (questions.isEmpty())
		    return QuestionResult.fail("This exam has no questions yet.");
		Collections.shuffle(questions);
		return QuestionResult.success(questions);
    }

    // ── Load exam + questions together ─────────────────────────
    public ExamLoadResult loadExamForStudent(int examId) throws SQLException {
        ExamResult examResult = getExam(examId);
        if (!examResult.success)
            return new ExamLoadResult(false, examResult.message, null, null);

        QuestionResult questionResult = getShuffledQuestions(examId);
        if (!questionResult.success)
            return new ExamLoadResult(false, questionResult.message, null, null);

        return new ExamLoadResult(true, "OK",
                examResult.exam, questionResult.questions);
    }

    // ── Get all exams (for dashboards) ────────────────────────────────────────────
    public List<Exam> getAllExams() throws SQLException {
        return examDAO.getAllExams();
    }

    // ── Add exam (admin) ──────────────────────────────────────────────────────────
    public ServiceResult addExam(String title, int durationMinutes, int totalMarks) {
        if (title == null || title.trim().isEmpty())
            return ServiceResult.fail("Exam title is required.");
        if (durationMinutes <= 0)
            return ServiceResult.fail("Duration must be greater than 0.");
        if (totalMarks <= 0)
            return ServiceResult.fail("Total marks must be greater than 0.");
        try {
            Exam exam = new Exam(title.trim(), durationMinutes, totalMarks);
            int id = examDAO.addExam(exam);
            if (id > 0) return ServiceResult.success("Exam created. ID: " + id);
            return ServiceResult.fail("Failed to create exam.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ServiceResult.fail("Database error creating exam.");
        }
    }

    // ── Add question (admin) ──────────────────────────────────────────────────────
    public ServiceResult addQuestion(int examId, String questionText,
                                     String optionA, String optionB,
                                     String optionC, String optionD,
                                     String correctOption, int marks) {
        if (isBlank(questionText))
            return ServiceResult.fail("Question text is required.");
        if (isBlank(optionA) || isBlank(optionB) ||
            isBlank(optionC) || isBlank(optionD))
            return ServiceResult.fail("All four options are required.");
        if (!java.util.Set.of("A","B","C","D").contains(
                correctOption == null ? "" : correctOption.toUpperCase()))
            return ServiceResult.fail("Correct option must be A, B, C, or D.");
        if (marks <= 0)
            return ServiceResult.fail("Marks must be greater than 0.");

        try {
            Exam exam = examDAO.getExamById(examId);
            if (exam == null)
                return ServiceResult.fail("Exam ID " + examId + " not found.");

            Question q = new Question(examId, questionText.trim(),
                    optionA.trim(), optionB.trim(),
                    optionC.trim(), optionD.trim(),
                    correctOption.toUpperCase().trim(), marks);

            int id = questionDAO.addQuestion(q);
            if (id > 0) return ServiceResult.success("Question added successfully.");
            return ServiceResult.fail("Failed to add question.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ServiceResult.fail("Database error adding question.");
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────────
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  NESTED RESULT CLASSES
    //  These are what ExamServlet imports as:
    //  import service.ExamService.ExamLoadResult;
    // ══════════════════════════════════════════════════════════════════════════════

    public static class ExamResult {
        public final boolean success;
        public final String  message;
        public final Exam    exam;

        private ExamResult(boolean s, String m, Exam e) {
            success = s; message = m; exam = e;
        }
        public static ExamResult success(Exam e)  { return new ExamResult(true,  "OK", e);   }
        public static ExamResult fail(String m)   { return new ExamResult(false, m,  null);  }
    }

    public static class QuestionResult {
        public final boolean        success;
        public final String         message;
        public final List<Question> questions;

        private QuestionResult(boolean s, String m, List<Question> q) {
            success = s; message = m; questions = q;
        }
        public static QuestionResult success(List<Question> q) {
            return new QuestionResult(true, "OK", q);
        }
        public static QuestionResult fail(String m) {
            return new QuestionResult(false, m, null);
        }
    }

    public static class ExamLoadResult {
        public final boolean        success;
        public final String         message;
        public final Exam           exam;
        public final List<Question> questions;

        public ExamLoadResult(boolean s, String m, Exam e, List<Question> q) {
            success = s; message = m; exam = e; questions = q;
        }
    }

    public static class ServiceResult {
        public final boolean success;
        public final String  message;

        private ServiceResult(boolean s, String m) {
            success = s; message = m;
        }
        public static ServiceResult success(String m) { return new ServiceResult(true,  m); }
        public static ServiceResult fail(String m)    { return new ServiceResult(false, m); }
    }
}
