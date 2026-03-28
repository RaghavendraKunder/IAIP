package dao;

import Model.Batch;
import Model.Student;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BatchDAO {

    // ── Create batch ──────────────────────────────────────────────────────────
    public int createBatch(Batch b) throws SQLException {
        String sql = """
            INSERT INTO batches (name, standard, stream, division, course, college_name, admin_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getName());
            ps.setString(2, b.getStandard());
            ps.setString(3, b.getStream());
            ps.setString(4, b.getDivision());
            ps.setString(5, b.getCourse());
            ps.setString(6, b.getCollegeName());
            ps.setInt(7, b.getAdminId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    // ── Get all batches for admin ─────────────────────────────────────────────
    public List<Batch> getBatchesByAdmin(int adminId) throws SQLException {
        String sql = """
            SELECT b.*, COUNT(bs.student_id) AS student_count
            FROM batches b
            LEFT JOIN batch_students bs ON b.id = bs.batch_id
            WHERE b.admin_id = ?
            GROUP BY b.id
            ORDER BY b.created_at DESC
            """;
        List<Batch> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Get single batch ──────────────────────────────────────────────────────
    public Batch getBatchById(int id) throws SQLException {
        String sql = """
            SELECT b.*, COUNT(bs.student_id) AS student_count
            FROM batches b
            LEFT JOIN batch_students bs ON b.id = bs.batch_id
            WHERE b.id = ?
            GROUP BY b.id
            """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ── Add student to batch ──────────────────────────────────────────────────
    public boolean addStudentToBatch(int batchId, int studentId) throws SQLException {
        String sql = "INSERT IGNORE INTO batch_students (batch_id, student_id) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            ps.setInt(2, studentId);
            ps.executeUpdate();
            // Also update student's batch_id
            String upd = "UPDATE students SET batch_id = ?, division = " +
                         "(SELECT division FROM batches WHERE id = ?) WHERE id = ?";
            try (PreparedStatement p2 = c.prepareStatement(upd)) {
                p2.setInt(1, batchId);
                p2.setInt(2, batchId);
                p2.setInt(3, studentId);
                p2.executeUpdate();
            }
            return true;
        }
    }

    // ── Remove student from batch ─────────────────────────────────────────────
    public boolean removeStudentFromBatch(int batchId, int studentId) throws SQLException {
        String sql = "DELETE FROM batch_students WHERE batch_id = ? AND student_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Get students in batch ─────────────────────────────────────────────────
    public List<Student> getStudentsInBatch(int batchId) throws SQLException {
        String sql = """
            SELECT s.* FROM students s
            JOIN batch_students bs ON s.id = bs.student_id
            WHERE bs.batch_id = ?
            ORDER BY s.full_name
            """;
        List<Student> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student();
                    s.setId(rs.getInt("id"));
                    s.setFullName(rs.getString("full_name"));
                    s.setRollNo(rs.getString("roll_no"));
                    s.setEmail(rs.getString("email"));
                    s.setCollegeName(rs.getString("college_name"));
                    s.setClassLevel(rs.getString("class_level"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    // ── Get students NOT in any batch (for admin's college) ───────────────────
    public List<Student> getUnassignedStudents(String collegeName) throws SQLException {
        if (collegeName == null || collegeName.trim().isEmpty()) {
            System.out.println("[BatchDAO] getUnassignedStudents: collegeName is empty!");
            return new java.util.ArrayList<>();
        }
        System.out.println("[BatchDAO] getUnassignedStudents: searching for college='" + collegeName + "'");

        // Match by college_name directly OR via college_id → colleges table
        String sql =
            "SELECT s.* FROM students s " +
            "LEFT JOIN colleges c ON s.college_id = c.id " +
            "WHERE (s.college_name = ? OR c.college_name = ?) " +
            "ORDER BY s.full_name";

        List<Student> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collegeName.trim());
            ps.setString(2, collegeName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student();
                    s.setId(rs.getInt("id"));
                    s.setFullName(rs.getString("full_name"));
                    s.setRollNo(rs.getString("roll_no"));
                    s.setEmail(rs.getString("email"));
                    s.setClassLevel(rs.getString("class_level"));
                    list.add(s);
                }
            }
        }
        System.out.println("[BatchDAO] Found " + list.size() + " students for " + collegeName);
        return list;
    }

    // ── Delete batch ──────────────────────────────────────────────────────────
    public boolean deleteBatch(int batchId) throws SQLException {
        String sql = "DELETE FROM batches WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            return ps.executeUpdate() > 0;
        }
    }

    private Batch mapRow(ResultSet rs) throws SQLException {
        Batch b = new Batch();
        b.setId(rs.getInt("id"));
        b.setName(rs.getString("name"));
        b.setStandard(rs.getString("standard"));
        b.setStream(rs.getString("stream"));
        b.setDivision(rs.getString("division"));
        b.setCourse(rs.getString("course"));
        b.setCollegeName(rs.getString("college_name"));
        b.setAdminId(rs.getInt("admin_id"));
        b.setCreatedAt(rs.getString("created_at"));
        try { b.setStudentCount(rs.getInt("student_count")); }
        catch (Exception ignored) {}
        return b;
    }
}