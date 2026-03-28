package dao;

import Model.Exam;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {

    // ── INSERT ────────────────────────────────────────────────────────────────
    public int addExam(Exam e) throws SQLException {
        String sql = "INSERT INTO exams " +
            "(title, duration_minutes, total_marks, deadline, is_published, college_name) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getTitle());
            ps.setInt   (2, e.getDurationMinutes());
            ps.setInt   (3, e.getTotalMarks());
            ps.setString(4, e.getDeadline());
            ps.setInt   (5, e.isPublished() ? 1 : 0);
            ps.setString(6, e.getCollegeName());
            int rows = ps.executeUpdate();
            if (rows == 0) return -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    // ── PUBLISH ───────────────────────────────────────────────────────────────
    public boolean publishExam(int examId, String deadline) throws SQLException {
        String sql = "UPDATE exams SET is_published = 1, deadline = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, deadline);
            ps.setInt   (2, examId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── READ: single exam ─────────────────────────────────────────────────────
    public Exam getExamById(int id) throws SQLException {
        String sql = "SELECT * FROM exams WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ── READ: all exams for a college ─────────────────────────────────────────
    public List<Exam> getExamsByCollege(String collegeName) throws SQLException {
        String sql = "SELECT * FROM exams WHERE college_name = ? ORDER BY created_at DESC";
        List<Exam> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collegeName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── READ: all exams (superadmin / fallback) ───────────────────────────────
    public List<Exam> getAllExams() throws SQLException {
        String sql = "SELECT * FROM exams ORDER BY created_at DESC";
        List<Exam> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────
    private Exam mapRow(ResultSet rs) throws SQLException {
        Exam e = new Exam();
        e.setId             (rs.getInt   ("id"));
        e.setTitle          (rs.getString("title"));
        e.setDurationMinutes(rs.getInt   ("duration_minutes"));
        e.setTotalMarks     (rs.getInt   ("total_marks"));
        e.setCreatedAt      (rs.getString("created_at"));
        try { e.setDeadline    (rs.getString("deadline"));    } catch (Exception ex) {}
        try { e.setPublished   (rs.getInt   ("is_published") == 1); } catch (Exception ex) {}
        try { e.setCollegeName (rs.getString("college_name")); } catch (Exception ex) {}
        return e;
    }
}