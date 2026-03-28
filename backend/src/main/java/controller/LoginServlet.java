package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.AdminDAO;
import dao.StudentDAO;
import Model.Admin;
import Model.Student;
import util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.sql.SQLException;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentDAO studentDAO = new StudentDAO();
    private final AdminDAO   adminDAO   = new AdminDAO();
    private final Gson       gson       = new Gson();

    // ── Handle preflight OPTIONS request from browser ─────────────────────────
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        setCorsHeaders(res);
        res.setStatus(HttpServletResponse.SC_OK);
    }

    // ── Handle POST /api/login ────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        setCorsHeaders(res);
        res.setContentType("application/json;charset=UTF-8");

        // ── Read JSON body ────────────────────────────────────────────────────
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        JsonObject body;
        try {
            body = JsonParser.parseString(sb.toString()).getAsJsonObject();
        } catch (Exception e) {
            writeJson(res, false, "Invalid JSON body.", null, null, null, 0);
            return;
        }

        String role       = getString(body, "role");
        String identifier = getString(body, "identifier");
        String password   = getString(body, "password");

        // ── STUDENT LOGIN ─────────────────────────────────────────────────────
        if ("student".equalsIgnoreCase(role)) {
            try {
                // Try by email first, then by roll number
                Student student = studentDAO.findByEmail(identifier);
                if (student == null) {
                    student = studentDAO.findByRollNo(identifier);
                }

                if (student == null) {
                    writeJson(res, false,
                        "No account found with that email or roll number.", null, null, null, 0);
                    return;
                }

                if (!PasswordUtil.verify(password, student.getPassword())) {
                    writeJson(res, false, "Incorrect password.", null, null, null, 0);
                    return;
                }

                // ── Create session ────────────────────────────────────────────
                HttpSession session = req.getSession(true);
                session.setAttribute("studentId",   student.getId());
                session.setAttribute("studentName", student.getFullName());
                session.setAttribute("rollNo",      student.getRollNo());
                session.setAttribute("role",        "STUDENT");

                writeJson(res, true,
                    "Welcome back, " + student.getFullName() + "!",
                    "student",
                    "/student/dashboard",
                    null, 0,
                    student.getId(),
                    student.getFullName(),
                    student.getRollNo());

            } catch (SQLException e) {
                e.printStackTrace();
                writeJson(res, false, "Database error. Please try again.", null, null, null, 0);
            }
            return;
        }

        // ── ADMIN LOGIN ───────────────────────────────────────────────────────
        if ("admin".equalsIgnoreCase(role)) {
            String adminKey = getString(body, "adminKey");

            try {
                Admin admin = adminDAO.findByEmail(identifier);

                if (admin == null) {
                    writeJson(res, false,
                        "No admin account found with that email.", null, null, null, 0);
                    return;
                }

                if (!PasswordUtil.verify(password, admin.getPassword())) {
                    writeJson(res, false, "Incorrect password.", null, null, null, 0);
                    return;
                }

                if (!PasswordUtil.verify(adminKey, admin.getAdminKeyHash())) {
                    writeJson(res, false, "Invalid admin access key.", null, null, null, 0);
                    return;
                }

                // ── Create session ────────────────────────────────────────────
                HttpSession session = req.getSession(true);
                session.setAttribute("adminId",      admin.getId());
                session.setAttribute("adminName",    admin.getFullName());
                session.setAttribute("collegeName",  admin.getCollegeName());
                session.setAttribute("role",         "ADMIN");

                writeJson(res, true,
                    "Welcome, " + admin.getFullName() + "!",
                    "admin",
                    "/admin/dashboard",
                    admin.getFullName(),
                    admin.getId());

                // Also put collegeName in session for AdminDataServlet
                session.setAttribute("collegeName", admin.getCollegeName());

            } catch (SQLException e) {
                e.printStackTrace();
                writeJson(res, false, "Database error. Please try again.", null, null, null, 0);
            }
            return;
        }

        writeJson(res, false,
            "Invalid role. Must be 'student' or 'admin'.", null, null, null, 0);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getString(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString().trim();
        }
        return "";
    }

    private void writeJson(HttpServletResponse res,
                           boolean success, String message,
                           String role, String redirectUrl,
                           String adminName, int adminId,
                           int studentId, String studentName, String rollNo)
            throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success",     success);
        json.addProperty("message",     message);
        if (role        != null) json.addProperty("role",        role);
        if (redirectUrl != null) json.addProperty("redirectUrl", redirectUrl);
        if (adminName   != null) json.addProperty("adminName",   adminName);
        if (adminId     > 0)     json.addProperty("adminId",     adminId);
        if (studentId   > 0)     json.addProperty("studentId",   studentId);
        if (studentName != null) json.addProperty("studentName", studentName);
        if (rollNo      != null) json.addProperty("rollNo",      rollNo);
        res.setStatus(success
                ? HttpServletResponse.SC_OK
                : HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write(gson.toJson(json));
    }

    // Overload for calls that don't need student fields
    private void writeJson(HttpServletResponse res,
                           boolean success, String message,
                           String role, String redirectUrl,
                           String adminName, int adminId)
            throws IOException {
        writeJson(res, success, message, role, redirectUrl,
                  adminName, adminId, 0, null, null);
    }

    /**
     * CORS headers — allows React on localhost:3000
     * to call this servlet on localhost:8085
     */
    private void setCorsHeaders(HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin",      "http://localhost:8080");
        res.setHeader("Access-Control-Allow-Methods",     "POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers",     "Content-Type");
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }
}