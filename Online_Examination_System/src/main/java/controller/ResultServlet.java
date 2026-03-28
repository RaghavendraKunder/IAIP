package controller;

import dao.ExamDAO;
import dao.QuestionDAO;
import Model.Exam;
import Model.Question;
import Model.Result;
import service.ResultService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ResultServlet.java
 * ------------------
 * POST /submit-exam
 * Accepts form submission from student-exam.html,
 * calculates score, saves result, redirects to student-result.html
 */
@WebServlet("/submit-exam")
public class ResultServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ResultService resultService = new ResultService();
    private final ExamDAO       examDAO       = new ExamDAO();
    private final QuestionDAO   questionDAO   = new QuestionDAO();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        setCors(res);
        res.setStatus(200);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        setCors(res);

        // ── Get studentId from form param OR session ──────────────────────────
        int studentId = 0;
        try {
            String sidParam = req.getParameter("studentId");
            if (sidParam != null && !sidParam.isEmpty()) {
                studentId = Integer.parseInt(sidParam.trim());
            }
        } catch (Exception ignored) {}

        // Fallback to session
        if (studentId == 0) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("studentId") != null) {
                studentId = (int) session.getAttribute("studentId");
            }
        }

        if (studentId == 0) {
            res.sendRedirect("http://localhost:8080/student-portal.html");
            return;
        }

        // ── Parse examId ──────────────────────────────────────────────────────
        int examId;
        try {
            examId = Integer.parseInt(req.getParameter("examId").trim());
        } catch (Exception e) {
            res.sendRedirect("http://localhost:8080/student-dashboard.html");
            return;
        }

        // ── Load exam + questions ─────────────────────────────────────────────
        Exam           exam;
        List<Question> questions;
        try {
            exam      = examDAO.getExamById(examId);
            questions = questionDAO.getQuestionsByExamId(examId);
        } catch (Exception e) {
            e.printStackTrace();
            res.sendRedirect("http://localhost:8080/student-dashboard.html");
            return;
        }

        if (exam == null || questions.isEmpty()) {
            res.sendRedirect("http://localhost:8080/student-dashboard.html");
            return;
        }

        // ── Collect submitted answers ─────────────────────────────────────────
        Map<Integer, String> answers = new HashMap<>();
        for (Question q : questions) {
            String param = req.getParameter("q" + q.getId());
            if (param != null && !param.isEmpty()) {
                answers.put(q.getId(), param.trim().toUpperCase());
            }
        }

        // ── Calculate score ───────────────────────────────────────────────────
        int score = 0;
        for (Question q : questions) {
            String submitted = answers.get(q.getId());
            if (submitted != null && submitted.equals(q.getCorrectOption())) {
                score += q.getMarks();
            }
        }

        // ── Save result ───────────────────────────────────────────────────────
        Result result = null;
        try {
            result = resultService.evaluateAndSave(studentId, examId, answers);
        } catch (SQLException e) {
            e.printStackTrace();
            // Continue even if save fails — still show result
        }

        int    finalScore = result != null ? result.getScore()      : score;
        int    totalMarks = result != null ? result.getTotalMarks() : exam.getTotalMarks();
        String title      = exam.getTitle();

        // ── Return JSON with redirect URL (fetch-friendly) ──────────────────
        String resultUrl = "http://localhost:8080/student-result.html"
                + "?score=" + finalScore
                + "&total=" + totalMarks
                + "&title=" + URLEncoder.encode(title, "UTF-8");

        res.setContentType("application/json;charset=UTF-8");
        res.setStatus(200);
        res.getWriter().write(
            "{\"success\":true,\"redirectUrl\":\"" + resultUrl + "\"}"
        );
    }

    private void setCors(HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin",      "http://localhost:8080");
        res.setHeader("Access-Control-Allow-Methods",     "POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers",     "Content-Type");
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }
}