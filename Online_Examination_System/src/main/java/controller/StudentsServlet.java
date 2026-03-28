package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dao.EnrollmentDAO;
import dao.StudentDAO;
import dao.CollegeDAO;
import Model.Exam;
import Model.Student;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * StudentsServlet.java
 * --------------------
 * GET /admin/students              → students from admin's college
 * GET /admin/students?q=query      → search within college
 * GET /admin/students?id=1         → single student + enrolled exams
 * GET /admin/students?all=true     → all students (cross-college, superadmin)
 *
 * College is determined from:
 *   1. Session attribute "collegeName" (set on login)
 *   2. Header "X-College-Name" (sent from admin-panel.html)
 */
@WebServlet("/admin/students")
public class StudentsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final StudentDAO    studentDAO    = new StudentDAO();
    private final CollegeDAO   collegeDAO   = new CollegeDAO();
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

        // ── Get college name from session or header ────────────────────────────
        String collegeName = getCollegeName(req);

        String q      = req.getParameter("q");
        String idStr  = req.getParameter("id");
        String allStr = req.getParameter("all");

        try {
            // ── Single student detail ─────────────────────────────────────────
            if (idStr != null) {
                int sid = Integer.parseInt(idStr);
                Student s = studentDAO.findById(sid);
                if (s == null) { writeError(res, "Student not found."); return; }

                List<Exam> exams = enrollmentDAO.getEnrolledExams(sid);
                JsonObject obj = studentToJson(s);
                JsonArray exArr = new JsonArray();
                for (Exam e : exams) {
                    JsonObject eo = new JsonObject();
                    eo.addProperty("id",              e.getId());
                    eo.addProperty("title",           e.getTitle());
                    eo.addProperty("durationMinutes", e.getDurationMinutes());
                    eo.addProperty("totalMarks",      e.getTotalMarks());
                    eo.addProperty("deadline",        e.getDeadline() != null ? e.getDeadline() : "");
                    eo.addProperty("attempted",       enrollmentDAO.hasAttempted(e.getId(), sid));
                    exArr.add(eo);
                }
                obj.add("exams", exArr);
                JsonObject resp = new JsonObject();
                resp.addProperty("success", true);
                resp.add("student", obj);
                res.getWriter().write(gson.toJson(resp));
                return;
            }

            // ── Student list filtered by college_id ──────────────────────────
            List<Student> students;

            if ("true".equals(allStr) || collegeName == null || collegeName.isEmpty()) {
                // No college filter — show all
                students = (q != null && !q.trim().isEmpty())
                        ? studentDAO.searchStudents(q.trim())
                        : studentDAO.getAllStudents();
            } else {
                // ── Look up college_id for this college name ──────────────────
                int collegeId = getCollegeIdByName(collegeName);

                if (collegeId > 0) {
                    // ✅ Primary: filter by college_id (access key verified students)
                    students = (q != null && !q.trim().isEmpty())
                            ? studentDAO.searchByCollegeIdAndQuery(collegeId, q.trim())
                            : studentDAO.getStudentsByCollegeId(collegeId);
                } else {
                    // ⚠️ Fallback: filter by college_name (old registrations)
                    students = (q != null && !q.trim().isEmpty())
                            ? studentDAO.searchByCollegeAndQuery(collegeName, q.trim())
                            : studentDAO.getStudentsByCollege(collegeName);
                }
            }

            // Group by class level
            JsonObject grouped  = new JsonObject();
            JsonArray  flatList = new JsonArray();

            for (Student s : students) {
                JsonObject obj = studentToJson(s);
                int examCount  = enrollmentDAO.getEnrolledExams(s.getId()).size();
                obj.addProperty("examCount", examCount);
                flatList.add(obj);

                // Build grouped structure
                String groupKey = s.getClassLevel()
                        + (s.getStream() != null && !s.getStream().isEmpty()
                           ? " — " + s.getStream() : "");
                if (!grouped.has(groupKey)) grouped.add(groupKey, new JsonArray());
                grouped.getAsJsonArray(groupKey).add(obj);
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("success",     true);
            resp.addProperty("collegeName", collegeName != null ? collegeName : "All Colleges");
            resp.addProperty("total",       students.size());
            resp.add("students",  flatList);
            resp.add("grouped",   grouped);
            res.getWriter().write(gson.toJson(resp));

        } catch (Exception e) {
            e.printStackTrace();
            writeError(res, "Server error: " + e.getMessage());
        }
    }

    // ── Get college from session OR header ────────────────────────────────────
    // ── Look up college_id by college_name ────────────────────────────────────
    private int getCollegeIdByName(String collegeName) {
        String sql = "SELECT id FROM colleges WHERE college_name = ? AND is_active = 1";
        try (java.sql.Connection conn = util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collegeName);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : 0;
            }
        } catch (Exception e) {
            System.err.println("[StudentsServlet] college_id lookup: " + e.getMessage());
            return 0;
        }
    }

    private String getCollegeName(HttpServletRequest req) {
        // Try session first
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("collegeName") != null) {
            return (String) session.getAttribute("collegeName");
        }
        // Fallback: header sent from static HTML pages
        String header = req.getHeader("X-College-Name");
        return (header != null && !header.isEmpty()) ? header : null;
    }

    private JsonObject studentToJson(Student s) {
        JsonObject o = new JsonObject();
        o.addProperty("id",          s.getId());
        o.addProperty("fullName",    s.getFullName());
        o.addProperty("rollNo",      s.getRollNo());
        o.addProperty("email",       s.getEmail());
        o.addProperty("collegeName", s.getCollegeName());
        o.addProperty("classLevel",  s.getClassLevel());
        o.addProperty("stream",      s.getStream()    != null ? s.getStream()    : "");
        o.addProperty("course",      s.getCourse()    != null ? s.getCourse()    : "");
        o.addProperty("createdAt",   s.getCreatedAt() != null ? s.getCreatedAt() : "");
        return o;
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
        res.setHeader("Access-Control-Allow-Headers",     "Content-Type, X-College-Name, X-Admin-Id");
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }
}