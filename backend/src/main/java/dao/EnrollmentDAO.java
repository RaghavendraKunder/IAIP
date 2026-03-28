package dao;

import Model.Exam;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EnrollmentDAO.java
 * ------------------
 * Handles exam_enrollments table:
 *   - Enroll a student into an exam
 *   - Get all exams a student is enrolled in
 *   - Check if already attempted
 */
public class EnrollmentDAO {

    // ── Enroll a student into an exam ─────────────────────────────────────────
    public boolean enroll(int examId, int studentId) throws SQLException {
        String sql = "INSERT IGNORE INTO exam_enrollments (exam_id, student_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Get all published, non-expired exams for a student ────────────────────
    public List<Exam> getEnrolledExams(int studentId) throws SQLException {
        String sql = """
            SELECT e.* FROM exams e
            JOIN exam_enrollments en ON e.id = en.exam_id
            WHERE en.student_id = ?
            AND e.is_published = 1
            ORDER BY e.deadline ASC
            """;
        List<Exam> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Check if student already attempted this exam ──────────────────────────
    public boolean hasAttempted(int examId, int studentId) throws SQLException {
        String sql = "SELECT id FROM results WHERE exam_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── Check if student is enrolled in exam ─────────────────────────────────
    public boolean isEnrolled(int examId, int studentId) throws SQLException {
        String sql = "SELECT id FROM exam_enrollments WHERE exam_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private Exam mapRow(ResultSet rs) throws SQLException {
        Exam e = new Exam();
        e.setId(rs.getInt("id"));
        e.setTitle(rs.getString("title"));
        e.setDurationMinutes(rs.getInt("duration_minutes"));
        e.setTotalMarks(rs.getInt("total_marks"));
        e.setCreatedAt(rs.getString("created_at"));
        e.setDeadline(rs.getString("deadline"));
        e.setPublished(rs.getInt("is_published") == 1);
        return e;
    }
}
