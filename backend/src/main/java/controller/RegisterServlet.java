package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import service.AuthService;
import service.AuthService.RegisterResult;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AuthService authService = new AuthService();
    private final Gson        gson        = new Gson();

    // ── Handle preflight OPTIONS request from browser ─────────────────────────
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        setCorsHeaders(res);
        res.setStatus(HttpServletResponse.SC_OK);
    }

    // ── Handle POST /api/register ─────────────────────────────────────────────
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
            writeJson(res, HttpServletResponse.SC_BAD_REQUEST,
                      false, "Invalid JSON body.");
            return;
        }

        String role = getString(body, "role");

        // ── Route to correct registration method ──────────────────────────────
        RegisterResult result;

        if ("student".equalsIgnoreCase(role)) {
            result = authService.registerStudent(
                getString(body, "fullName"),
                getString(body, "rollNo"),
                getString(body, "email"),
                getString(body, "password"),
                getString(body, "confirmPassword"),
                getString(body, "collegeName"),
                getString(body, "accessKey"),      // college access key
                getString(body, "classLevel"),
                getString(body, "stream"),
                getString(body, "course")
            );

        } else if ("admin".equalsIgnoreCase(role)) {
            result = authService.registerAdmin(
                getString(body, "fullName"),
                getString(body, "email"),
                getString(body, "password"),
                getString(body, "confirmPassword"),
                getString(body, "collegeName"),
                getString(body, "adminAccessKey")
            );

        } else {
            writeJson(res, HttpServletResponse.SC_BAD_REQUEST,
                      false, "Invalid role. Must be 'student' or 'admin'.");
            return;
        }

        int status = result.success
                ? HttpServletResponse.SC_CREATED   // 201
                : HttpServletResponse.SC_CONFLICT; // 409

        writeJson(res, status, result.success, result.message);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getString(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString().trim();
        }
        return "";
    }

    private void writeJson(HttpServletResponse res,
                           int status, boolean success, String message)
            throws IOException {
        res.setStatus(status);
        JsonObject json = new JsonObject();
        json.addProperty("success", success);
        json.addProperty("message", message);
        res.getWriter().write(gson.toJson(json));
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