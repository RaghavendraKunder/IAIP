package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dao.EnrollmentDAO;
import dao.ExamDAO;
import dao.QuestionDAO;
import Model.Exam;
import Model.Question;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * ExamDataServlet.java
 * --------------------
 * GET /exam/data?examId=1&studentId=1
 * Returns JSON { exam, questions } for the static student-exam.html page.
 */
@WebServlet("/exam/data")
public class ExamDataServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ExamDAO       examDAO       = new ExamDAO();
    private final QuestionDAO   questionDAO   = new QuestionDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final Gson          gson          = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        setCors(res); res.setStatus(200);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        setCors(res);
        res.setContentType("application/json;charset=UTF-8");

        String examIdStr  = req.getParameter("examId");
        String studentStr = req.getParameter("studentId");

        int examId, studentId;
        try {
            examId    = Integer.parseInt(examIdStr);
            studentId = Integer.parseInt(studentStr);
        } catch (Exception e) {
            writeError(res, "Invalid parameters."); return;
        }

        try {
            // Load exam
            Exam exam = examDAO.getExamById(examId);
            if (exam == null) { writeError(res, "Exam not found."); return; }

            // Check enrollment
            if (!enrollmentDAO.isEnrolled(examId, studentId)) {
                writeError(res, "You are not enrolled in this exam."); return;
            }

            // Check deadline
            if (exam.isExpired()) {
                writeError(res, "This exam's deadline has passed."); return;
            }

            // Check already attempted
            if (enrollmentDAO.hasAttempted(examId, studentId)) {
                writeError(res, "You have already attempted this exam."); return;
            }

            // Load questions
            List<Question> questions = questionDAO.getQuestionsByExamId(examId);
            if (questions.isEmpty()) {
                writeError(res, "This exam has no questions yet."); return;
            }

            // Build exam object
            JsonObject examObj = new JsonObject();
            examObj.addProperty("id",              exam.getId());
            examObj.addProperty("title",           exam.getTitle());
            examObj.addProperty("durationMinutes", exam.getDurationMinutes());
            examObj.addProperty("totalMarks",      exam.getTotalMarks());
            examObj.addProperty("deadline",        exam.getDeadline());

            // Build questions array
            JsonArray qArr = new JsonArray();
            for (Question q : questions) {
                JsonObject qObj = new JsonObject();
                qObj.addProperty("id",           q.getId());
                qObj.addProperty("questionText", q.getQuestionText());
                qObj.addProperty("optionA",      q.getOptionA());
                qObj.addProperty("optionB",      q.getOptionB());
                qObj.addProperty("optionC",      q.getOptionC());
                qObj.addProperty("optionD",      q.getOptionD());
                qObj.addProperty("marks",        q.getMarks());
                qArr.add(qObj);
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("success", true);
            resp.add("exam",      examObj);
            resp.add("questions", qArr);
            res.getWriter().write(gson.toJson(resp));

        } catch (Exception e) {
            e.printStackTrace();
            writeError(res, "Server error: " + e.getMessage());
        }
    }

    private void writeError(HttpServletResponse res, String msg) throws IOException {
        JsonObject j = new JsonObject();
        j.addProperty("success", false);
        j.addProperty("message", msg);
        res.setStatus(400);
        res.getWriter().write(gson.toJson(j));
    }

    private void setCors(HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin",      "http://localhost:8080");
        res.setHeader("Access-Control-Allow-Methods",     "GET, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers",     "Content-Type");
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
