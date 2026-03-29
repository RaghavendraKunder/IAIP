package controller;

import com.google.gson.*;
import dao.*;
import model.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.List;

/**
 * AdminDataServlet.java
 * ---------------------
 * Handles all admin dashboard API calls:
 *   GET  /admin/data?action=dashboard  → stats + batches + exams
 *   POST /admin/data?action=createBatch
 *   POST /admin/data?action=addStudent
 *   POST /admin/data?action=removeStudent
 *   POST /admin/data?action=deleteBatch
 */
@WebServlet("/admin/data")
public class AdminDataServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BatchDAO      batchDAO      = new BatchDAO();
    private final ExamDAO       examDAO       = new ExamDAO();
    private final StudentDAO    studentDAO    = new StudentDAO();
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

        int adminId = getAdminId(req);
        if (adminId < 1) { writeError(res, "Unauthorized"); return; }

        String action = req.getParameter("action");
        if (action == null) action = "dashboard";

        try {
            switch (action) {
                case "dashboard"    -> handleDashboard(req, res, adminId);
                case "batchStudents"-> handleBatchStudents(req, res, adminId);
                case "unassigned"   -> handleUnassigned(req, res, adminId);
                case "batches"      -> handleGetBatches(req, res, adminId);
                default -> writeError(res, "Unknown action");
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeError(res, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        setCors(res);
        res.setContentType("application/json;charset=UTF-8");

        int adminId = getAdminId(req);
        if (adminId < 1) { writeError(res, "Unauthorized"); return; }

        String action = req.getParameter("action");
        if (action == null) action = "";

        // Read JSON body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = req.getReader()) {
            String l; while ((l = r.readLine()) != null) sb.append(l);
        }
        JsonObject body;
        try { body = JsonParser.parseString(sb.toString()).getAsJsonObject(); }
        catch (Exception e) { body = new JsonObject(); }

        try {
            switch (action) {
                case "createBatch"     -> handleCreateBatch(res, body, adminId);
                case "addStudent"      -> handleAddStudent(res, body, adminId);
                case "removeStudent"   -> handleRemoveStudent(res, body);
                case "deleteBatch"     -> handleDeleteBatch(res, body);
                default -> writeError(res, "Unknown action");
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeError(res, "Server error: " + e.getMessage());
        }
    }

    // ── Dashboard data ────────────────────────────────────────────────────────
    private void handleDashboard(HttpServletRequest req,
                                  HttpServletResponse res, int adminId)
            throws Exception {

        String collegeName = getCollegeName(req, adminId);

        List<Batch> batches  = batchDAO.getBatchesByAdmin(adminId);
        List<Exam>  exams    = examDAO.getAllExams();

        // Count total students — check by college_id first, fallback to college_name
        int collegeId = getCollegeIdByName(collegeName);
        int totalStudents = (collegeId > 0)
            ? studentDAO.countByCollegeId(collegeId)
            : studentDAO.countByCollege(collegeName);

        JsonObject resp = new JsonObject();
        resp.addProperty("success", true);
        resp.addProperty("totalBatches",  batches.size());
        resp.addProperty("totalExams",    exams.size());
        resp.addProperty("totalStudents", totalStudents);

        // Batches array
        JsonArray bArr = new JsonArray();
        for (Batch b : batches) {
            JsonObject bObj = new JsonObject();
            bObj.addProperty("id",           b.getId());
            bObj.addProperty("name",         b.getName());
            bObj.addProperty("standard",     b.getStandard());
            bObj.addProperty("stream",       b.getStream());
            bObj.addProperty("division",     b.getDivision());
            bObj.addProperty("course",       b.getCourse());
            bObj.addProperty("studentCount", b.getStudentCount());
            bObj.addProperty("createdAt",    b.getCreatedAt());
            bArr.add(bObj);
        }
        resp.add("batches", bArr);

        // Exams array
        JsonArray eArr = new JsonArray();
        for (Exam e : exams) {
            JsonObject eObj = new JsonObject();
            eObj.addProperty("id",              e.getId());
            eObj.addProperty("title",           e.getTitle());
            eObj.addProperty("durationMinutes", e.getDurationMinutes());
            eObj.addProperty("totalMarks",      e.getTotalMarks());
            eObj.addProperty("deadline",        e.getDeadline());
            eObj.addProperty("isPublished",     e.isPublished());
            eArr.add(eObj);
        }
        resp.add("exams", eArr);

        res.getWriter().write(gson.toJson(resp));
    }

    // ── Students in batch ─────────────────────────────────────────────────────
    private void handleBatchStudents(HttpServletRequest req,
                                      HttpServletResponse res, int adminId)
            throws Exception {
        int batchId = parseInt(req.getParameter("batchId"));
        if (batchId < 1) { writeError(res, "Invalid batchId"); return; }

        List<Student> students = batchDAO.getStudentsInBatch(batchId);
        JsonObject resp = new JsonObject();
        resp.addProperty("success", true);
        JsonArray arr = new JsonArray();
        for (Student s : students) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id",       s.getId());
            obj.addProperty("fullName", s.getFullName());
            obj.addProperty("rollNo",   s.getRollNo());
            obj.addProperty("email",    s.getEmail());
            arr.add(obj);
        }
        resp.add("students", arr);
        res.getWriter().write(gson.toJson(resp));
    }

    // ── Get batches for admin (used by exam panel batch selector) ─────────────
    private void handleGetBatches(HttpServletRequest req,
                                   HttpServletResponse res, int adminId)
            throws Exception {
        List<Batch> batches = batchDAO.getBatchesByAdmin(adminId);
        JsonObject resp = new JsonObject();
        resp.addProperty("success", true);
        JsonArray arr = new JsonArray();
        for (Batch b : batches) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id",           b.getId());
            obj.addProperty("name",         b.getName());
            obj.addProperty("standard",     b.getStandard());
            obj.addProperty("stream",       b.getStream()    != null ? b.getStream()    : "");
            obj.addProperty("division",     b.getDivision()  != null ? b.getDivision()  : "");
            obj.addProperty("course",       b.getCourse()    != null ? b.getCourse()    : "");
            obj.addProperty("studentCount", b.getStudentCount());
            arr.add(obj);
        }
        resp.add("batches", arr);
        res.getWriter().write(gson.toJson(resp));
    }

    // ── Unassigned students ───────────────────────────────────────────────────
    private void handleUnassigned(HttpServletRequest req,
                                   HttpServletResponse res, int adminId)
            throws Exception {
        String college   = getCollegeName(req, adminId);
        int    collegeId = getCollegeIdByName(college);

        System.out.println("[AdminDataServlet] unassigned: college='" + college
            + "' collegeId=" + collegeId);

        List<Student> students = batchDAO.getUnassignedStudents(college);

        // If collegeId found, also include students linked by college_id
        if (collegeId > 0) {
            List<Student> byId = studentDAO.getStudentsByCollegeId(collegeId);
            // Add those not already in the list and not in a batch
            for (Student s : byId) {
                boolean alreadyIn = students.stream()
                    .anyMatch(x -> x.getId() == s.getId());
                if (!alreadyIn) students.add(s);
            }
        }

        System.out.println("[AdminDataServlet] unassigned: found "
            + students.size() + " students");

        JsonObject resp = new JsonObject();
        resp.addProperty("success", true);
        JsonArray arr = new JsonArray();
        for (Student s : students) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id",         s.getId());
            obj.addProperty("fullName",   s.getFullName());
            obj.addProperty("rollNo",     s.getRollNo());
            obj.addProperty("email",      s.getEmail());
            obj.addProperty("classLevel", s.getClassLevel() != null ? s.getClassLevel() : "");
            arr.add(obj);
        }
        resp.add("students", arr);
        res.getWriter().write(gson.toJson(resp));
    }

    // ── Create batch ──────────────────────────────────────────────────────────
    private void handleCreateBatch(HttpServletResponse res,
                                    JsonObject body, int adminId)
            throws Exception {
        Batch b = new Batch();
        b.setName(    getString(body, "name"));
        b.setStandard(getString(body, "standard"));
        b.setStream(  getString(body, "stream"));
        b.setDivision(getString(body, "division"));
        b.setCourse(  getString(body, "course"));
        b.setCollegeName(getString(body, "collegeName"));
        b.setAdminId(adminId);

        if (b.getName().isEmpty() || b.getStandard().isEmpty()) {
            writeError(res, "Name and standard are required."); return;
        }
        if (b.getCollegeName().isEmpty()) {
            b.setCollegeName("Unknown College");
        }

        int id = batchDAO.createBatch(b);
        JsonObject resp = new JsonObject();
        resp.addProperty("success", id > 0);
        resp.addProperty("message", id > 0 ? "Batch created successfully!" : "Failed to create batch.");
        resp.addProperty("batchId", id);
        res.getWriter().write(gson.toJson(resp));
    }

    // ── Add student to batch ──────────────────────────────────────────────────
    private void handleAddStudent(HttpServletResponse res,
                                   JsonObject body, int adminId)
            throws Exception {
        int batchId   = getInt(body, "batchId");
        int studentId = getInt(body, "studentId");

        if (batchId < 1 || studentId < 1) {
            writeError(res, "Invalid batchId or studentId"); return;
        }

        boolean ok = batchDAO.addStudentToBatch(batchId, studentId);
        JsonObject resp = new JsonObject();
        resp.addProperty("success", ok);
        resp.addProperty("message", ok ? "Student added to batch." : "Failed.");
        res.getWriter().write(gson.toJson(resp));
    }

    // ── Remove student from batch ─────────────────────────────────────────────
    private void handleRemoveStudent(HttpServletResponse res, JsonObject body)
            throws Exception {
        int batchId   = getInt(body, "batchId");
        int studentId = getInt(body, "studentId");
        boolean ok    = batchDAO.removeStudentFromBatch(batchId, studentId);
        JsonObject resp = new JsonObject();
        resp.addProperty("success", ok);
        resp.addProperty("message", ok ? "Student removed." : "Failed.");
        res.getWriter().write(gson.toJson(resp));
    }

    // ── Delete batch ──────────────────────────────────────────────────────────
    private void handleDeleteBatch(HttpServletResponse res, JsonObject body)
            throws Exception {
        int batchId = getInt(body, "batchId");
        boolean ok  = batchDAO.deleteBatch(batchId);
        JsonObject resp = new JsonObject();
        resp.addProperty("success", ok);
        resp.addProperty("message", ok ? "Batch deleted." : "Failed.");
        res.getWriter().write(gson.toJson(resp));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private int getAdminId(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null && "ADMIN".equals(s.getAttribute("role"))) {
            Object id = s.getAttribute("adminId");
            if (id instanceof Integer) return (Integer) id;
        }
        String h = req.getHeader("X-Admin-Id");
        if (h != null && !h.isEmpty() && !h.equals("undefined")) {
            try { return Integer.parseInt(h.trim()); } catch (Exception ignored) {}
        }
        return -1;
    }

    private int getCollegeIdByName(String collegeName) {
        if (collegeName == null || collegeName.isEmpty()) return 0;
        String sql = "SELECT id FROM colleges WHERE college_name = ? AND is_active = 1";
        try (java.sql.Connection conn = util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collegeName);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private String getCollegeName(HttpServletRequest req, int adminId) {
        // Try session first
        HttpSession s = req.getSession(false);
        if (s != null) {
            Object c = s.getAttribute("collegeName");
            if (c != null && !c.toString().isEmpty()) return c.toString();
        }
        // Fallback: X-College-Name header (sent from static HTML pages)
        String header = req.getHeader("X-College-Name");
        if (header != null && !header.isEmpty()) return header;
        return "";
    }

    private void writeError(HttpServletResponse res, String msg) throws IOException {
        JsonObject j = new JsonObject();
        j.addProperty("success", false);
        j.addProperty("message", msg);
        res.setStatus(400);
        res.getWriter().write(gson.toJson(j));
    }

    private String getString(JsonObject o, String k) {
        if (o.has(k) && !o.get(k).isJsonNull()) return o.get(k).getAsString().trim();
        return "";
    }
    private int getInt(JsonObject o, String k) {
        try { return o.get(k).getAsInt(); } catch (Exception e) { return 0; }
    }
    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private void setCors(HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin",      "http://localhost:8080");
        res.setHeader("Access-Control-Allow-Methods",     "GET, POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers",     "Content-Type, X-Admin-Id, X-College-Name");
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }
}