package dao;

import Model.Result;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO {

    // ── Save result ───────────────────────────────────────────────────────────
    public int saveResult(Result r) throws SQLException {
        String sql = "INSERT INTO results (student_id, exam_id, score, total_marks) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getStudentId());
            ps.setInt(2, r.getExamId());
            ps.setInt(3, r.getScore());
            ps.setInt(4, r.getTotalMarks());
            int rows = ps.executeUpdate();
            if (rows == 0) return -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    // ── Find by student + exam ────────────────────────────────────────────────
    public Result findByStudentAndExam(int studentId, int examId) throws SQLException {
        String sql = "SELECT * FROM results WHERE student_id = ? AND exam_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ── All results for an exam (admin view) ──────────────────────────────────
    public List<Result> findByExamId(int examId) throws SQLException {
        String sql = "SELECT r.*, s.full_name, s.roll_no " +
                     "FROM results r JOIN students s ON r.student_id = s.id " +
                     "WHERE r.exam_id = ? ORDER BY r.score DESC";
        List<Result> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Result result = mapRow(rs);
                    result.setStudentName(rs.getString("full_name"));
                    result.setRollNo(rs.getString("roll_no"));
                    list.add(result);
                }
            }
        }
        return list;
    }

    // ── All results for a student ─────────────────────────────────────────────
    public List<Result> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT r.*, e.title " +
                     "FROM results r JOIN exams e ON r.exam_id = e.id " +
                     "WHERE r.student_id = ? ORDER BY r.attempted_at DESC";
        List<Result> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Result result = mapRow(rs);
                    result.setExamTitle(rs.getString("title"));
                    list.add(result);
                }
            }
        }
        return list;
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private Result mapRow(ResultSet rs) throws SQLException {
        Result r = new Result();
        r.setId(rs.getInt("id"));
        r.setStudentId(rs.getInt("student_id"));
        r.setExamId(rs.getInt("exam_id"));
        r.setScore(rs.getInt("score"));
        r.setTotalMarks(rs.getInt("total_marks"));
        try { r.setAttemptedAt(rs.getString("attempted_at")); } catch (Exception ignored) {}
        return r;
    }
}