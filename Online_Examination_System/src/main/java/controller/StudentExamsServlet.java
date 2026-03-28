package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dao.EnrollmentDAO;
import Model.Exam;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * StudentExamsServlet.java
 * ------------------------
 * GET /student/exams?studentId=1
 * Returns JSON list of enrolled exams for the student dashboard HTML page.
 */
@WebServlet("/student/exams")
public class StudentExamsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
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

        String sidStr = req.getParameter("studentId");
        if (sidStr == null || sidStr.isEmpty()) {
            writeError(res, "Missing studentId"); return;
        }

        int studentId;
        try { studentId = Integer.parseInt(sidStr); }
        catch (Exception e) { writeError(res, "Invalid studentId"); return; }

        try {
            List<Exam> exams = enrollmentDAO.getEnrolledExams(studentId);

            JsonArray arr = new JsonArray();
            for (Exam exam : exams) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id",              exam.getId());
                obj.addProperty("title",           exam.getTitle());
                obj.addProperty("durationMinutes", exam.getDurationMinutes());
                obj.addProperty("totalMarks",      exam.getTotalMarks());
                obj.addProperty("createdAt",       exam.getCreatedAt());
                obj.addProperty("deadline",        exam.getDeadline());
                obj.addProperty("isPublished",     exam.isPublished());
                obj.addProperty("attempted",
                    enrollmentDAO.hasAttempted(exam.getId(), studentId));
                arr.add(obj);
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("success", true);
            resp.add("exams", arr);
            res.getWriter().write(gson.toJson(resp));

        } catch (Exception e) {
            e.printStackTrace();
            writeError(res, "Database error: " + e.getMessage());
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