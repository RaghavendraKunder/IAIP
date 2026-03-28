package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.CollegeDAO;
import util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

/**
 * CollegeRegisterServlet.java
 * ---------------------------
 * POST /api/college/register
 *
 * Saves college registration to examflow_org database.
 * Completely separate from the examflow database.
 *
 * JSON body:
 * {
 *   "collegeName":    "XYZ School",
 *   "adminName":      "Dr. Ramesh",
 *   "email":          "admin@xyzschool.edu",
 *   "password":       "adminPass123",
 *   "confirmPassword":"adminPass123",
 *   "adminAccessKey": "XYZSchool@2026"
 * }
 */
@WebServlet("/api/college/register")
public class CollegeRegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final CollegeDAO collegeDAO = new CollegeDAO();
    private final Gson       gson       = new Gson();

    // ── Test endpoint: GET /api/college/register ────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        setCors(res);
        res.setContentType("application/json;charset=UTF-8");
        res.setStatus(200);
        res.getWriter().write("{" +
            "\"status\"" + ":" +
            "\"CollegeRegisterServlet is running\"" +
            "}");
    }

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

        // ── Parse body ────────────────────────────────────────────────────────
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = req.getReader()) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        JsonObject body;
        try {
            body = JsonParser.parseString(sb.toString()).getAsJsonObject();
        } catch (Exception e) {
            writeError(res, "Invalid JSON."); return;
        }

        String collegeName    = get(body, "collegeName");
        String adminName      = get(body, "adminName");
        String email          = get(body, "email");
        String password       = get(body, "password");
        String confirmPass    = get(body, "confirmPassword");
        // AuthModal sends "accessKey", fallback to "adminAccessKey"
        String accessKey = get(body, "accessKey");
        if (accessKey.isEmpty()) accessKey = get(body, "adminAccessKey");

        // ── Validate ──────────────────────────────────────────────────────────
        if (collegeName.isEmpty()) { writeError(res, "College name is required.");      return; }
        if (email.isEmpty())       { writeError(res, "Email is required.");             return; }
        if (password.isEmpty())    { writeError(res, "Password is required.");          return; }
        if (accessKey.isEmpty())    { writeError(res, "College access key is required."); return; }
        if (accessKey.length() < 3) { writeError(res, "Access key must be at least 3 characters."); return; }
        if (!password.equals(confirmPass)) {
            writeError(res, "Passwords do not match."); return;
        }

        try {
            // ── Check duplicates ──────────────────────────────────────────────
            if (collegeDAO.emailExists(email)) {
                writeError(res, "An account with this email already exists."); return;
            }
            if (collegeDAO.collegeNameExists(collegeName)) {
                writeError(res, "'" + collegeName + "' is already registered."); return;
            }

            // ── Hash and save ─────────────────────────────────────────────────
            String hashedPassword  = PasswordUtil.hash(password);
            String hashedAccessKey = PasswordUtil.hash(accessKey);

            int id = collegeDAO.registerCollege(
                collegeName, adminName.isEmpty() ? "Admin" : adminName,
                email, hashedPassword, hashedAccessKey
            );

            if (id > 0) {
                System.out.println("[CollegeRegister] ✅ Registered: " +
                    collegeName + " (id=" + id + ")");
                writeSuccess(res, "✅ " + collegeName + " registered successfully! " +
                    "You can now sign in using the Admin tab.");
            } else {
                writeError(res, "Registration failed. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            writeError(res, "Server error: " + e.getMessage());
        }
    }

    private String get(JsonObject obj, String key) {
        return (obj.has(key) && !obj.get(key).isJsonNull())
            ? obj.get(key).getAsString().trim() : "";
    }

    private void writeSuccess(HttpServletResponse res, String msg) throws IOException {
        JsonObject j = new JsonObject();
        j.addProperty("success", true);
        j.addProperty("message", msg);
        res.setStatus(201);
        res.getWriter().write(gson.toJson(j));
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
        res.setHeader("Access-Control-Allow-Methods",     "POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers",     "Content-Type");
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }
}