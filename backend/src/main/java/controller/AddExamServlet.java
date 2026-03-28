package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dao.ExamDAO;
import Model.Exam;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/add-exam")
public class AddExamServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ExamDAO examDAO = new ExamDAO();
    private final Gson    gson    = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        setCors(res); res.setStatus(200);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        setCors(res);
        res.setContentType("application/json;charset=UTF-8");

        // ── Get college name ──────────────────────────────────────────────────
        String collegeName = getCollegeName(req);

        // ── Read params ───────────────────────────────────────────────────────
        String title   = req.getParameter("title");
        String durStr  = req.getParameter("durationMinutes");
        String markStr = req.getParameter("totalMarks");

        System.out.println("[AddExamServlet] title=" + title
            + " college=" + collegeName);

        if (title == null || title.trim().isEmpty()) {
            writeJson(res, false, "Exam title is required.", -1); return;
        }
        if (durStr == null || durStr.trim().isEmpty()) {
            writeJson(res, false, "Duration is required.", -1); return;
        }
        if (markStr == null || markStr.trim().isEmpty()) {
            writeJson(res, false, "Total marks is required.", -1); return;
        }

        int durationMinutes, totalMarks;
        try { durationMinutes = Integer.parseInt(durStr.trim()); }
        catch (Exception e) { writeJson(res, false, "Invalid duration.", -1); return; }
        try { totalMarks = Integer.parseInt(markStr.trim()); }
        catch (Exception e) { writeJson(res, false, "Invalid total marks.", -1); return; }

        // ── Save ──────────────────────────────────────────────────────────────
        try {
            Exam exam = new Exam();
            exam.setTitle           (title.trim());
            exam.setDurationMinutes (durationMinutes);
            exam.setTotalMarks      (totalMarks);
            exam.setCollegeName     (collegeName);  // ← link exam to college

            int examId = examDAO.addExam(exam);
            System.out.println("[AddExamServlet] Created examId=" + examId
                + " for college=" + collegeName);

            if (examId > 0) writeJson(res, true,  "Exam created!", examId);
            else            writeJson(res, false, "Failed to create exam.", -1);

        } catch (Exception e) {
            e.printStackTrace();
            writeJson(res, false, "Database error: " + e.getMessage(), -1);
        }
    }

    // ── Get college from session OR header ────────────────────────────────────
    private String getCollegeName(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("collegeName") != null) {
            return (String) session.getAttribute("collegeName");
        }
        String header = req.getHeader("X-College-Name");
        return (header != null && !header.isEmpty()) ? header : null;
    }

    private void writeJson(HttpServletResponse res,
                           boolean success, String message, int examId)
            throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", success);
        json.addProperty("message", message);
        if (examId > 0) json.addProperty("examId", examId);
        res.setStatus(success ? 200 : 400);
        res.getWriter().write(gson.toJson(json));
    }

    private void setCors(HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin",      "http://localhost:8080");
        res.setHeader("Access-Control-Allow-Methods",     "POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers",     "Content-Type, X-Admin-Id, X-College-Name");
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }
}