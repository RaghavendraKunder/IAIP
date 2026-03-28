package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.EnrollmentDAO;
import dao.ExamDAO;
import dao.StudentDAO;
import Model.Student;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

@WebServlet("/publish-exam")
public class PublishExamServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ExamDAO       examDAO       = new ExamDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final StudentDAO    studentDAO    = new StudentDAO();
    private final Gson          gson          = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        setCors(res);
        res.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        setCors(res);
        res.setContentType("application/json;charset=UTF-8");

        // ── Parse JSON body ───────────────────────────────────────────────────
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        System.out.println("[PublishExamServlet] body=" + sb.toString());

        JsonObject body;
        try {
            body = JsonParser.parseString(sb.toString()).getAsJsonObject();
        } catch (Exception e) {
            writeJson(res, false, "Invalid JSON: " + e.getMessage(), 0, 0); return;
        }

        int    examId   = body.has("examId")   ? body.get("examId").getAsInt()      : -1;
        String deadline = body.has("deadline") && !body.get("deadline").isJsonNull()
                          ? body.get("deadline").getAsString() : null;

        System.out.println("[PublishExamServlet] examId=" + examId + " deadline=" + deadline);

        if (examId < 1) {
            writeJson(res, false, "Invalid exam ID: " + examId, 0, 0); return;
        }

        // ── 1. Publish exam ───────────────────────────────────────────────────
        try {
            examDAO.publishExam(examId, deadline);
            System.out.println("[PublishExamServlet] Exam published OK");
        } catch (Exception e) {
            e.printStackTrace();
            writeJson(res, false, "Failed to publish: " + e.getMessage(), 0, 0);
            return;
        }

        // ── 2. Enroll students ────────────────────────────────────────────────
        int enrolled = 0;
        int notFound = 0;

        if (body.has("students") && body.get("students").isJsonArray()) {
            for (var el : body.getAsJsonArray("students")) {
                String rollNo = el.getAsJsonObject().has("rollNo")
                        ? el.getAsJsonObject().get("rollNo").getAsString().trim() : "";
                if (rollNo.isEmpty()) continue;
                try {
                    Student student = studentDAO.findByRollNo(rollNo);
                    if (student != null) {
                        enrollmentDAO.enroll(examId, student.getId());
                        enrolled++;
                        System.out.println("[PublishExamServlet] Enrolled: " + rollNo);
                    } else {
                        notFound++;
                        System.out.println("[PublishExamServlet] Not found: " + rollNo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String msg = "Exam published!";
        if (enrolled > 0) msg += " " + enrolled + " student(s) enrolled.";
        if (notFound  > 0) msg += " " + notFound + " roll number(s) not found.";

        writeJson(res, true, msg, enrolled, notFound);
    }

    private void writeJson(HttpServletResponse res,
                           boolean success, String message,
                           int enrolled, int notFound) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success",  success);
        json.addProperty("message",  message);
        json.addProperty("enrolled", enrolled);
        json.addProperty("notFound", notFound);
        res.setStatus(success ? 200 : 400);
        res.getWriter().write(gson.toJson(json));
    }

    private void setCors(HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin",      "http://localhost:8080");
        res.setHeader("Access-Control-Allow-Methods",     "POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers",     "Content-Type, X-Admin-Id");
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }
}