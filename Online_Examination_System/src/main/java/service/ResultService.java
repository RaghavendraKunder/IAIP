package service;

import dao.ExamDAO;
import dao.QuestionDAO;
import dao.ResultDAO;
import Model.Exam;
import Model.Question;
import Model.Result;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ResultService {

    private final ResultDAO   resultDAO   = new ResultDAO();
    private final ExamDAO     examDAO     = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();

    /**
     * Calculates score from answers, saves to DB, returns Result.
     */
    public Result evaluateAndSave(int studentId, int examId,
                                   Map<Integer, String> answers)
            throws SQLException {

        Exam           exam      = examDAO.getExamById(examId);
        List<Question> questions = questionDAO.getQuestionsByExamId(examId);

        if (exam == null || questions.isEmpty()) return null;

        // Calculate score
        int score = 0;
        for (Question q : questions) {
            String submitted = answers.get(q.getId());
            if (submitted != null &&
                submitted.equalsIgnoreCase(q.getCorrectOption())) {
                score += q.getMarks();
            }
        }

        // Save result
        Result result = new Result(studentId, examId, score, exam.getTotalMarks());
        int id = resultDAO.saveResult(result);
        result.setId(id);
        return result;
    }

    public Result getResult(int studentId, int examId) throws SQLException {
        return resultDAO.findByStudentAndExam(studentId, examId);
    }
}