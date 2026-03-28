package dao;

import Model.Student;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // ── INSERT ────────────────────────────────────────────────────────────────
    public int registerStudent(Student s) throws SQLException {
        String sql =
            "INSERT INTO students " +
            "(full_name, roll_no, email, password, college_name, college_id," +
            " class_level, stream, course) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getFullName());
            ps.setString(2, s.getRollNo());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPassword());
            ps.setString(5, s.getCollegeName());
            if (s.getCollegeId() > 0)
                ps.setInt(6, s.getCollegeId());
            else
                ps.setNull(6, Types.INTEGER);
            ps.setString(7, s.getClassLevel());
            if (s.getStream() != null && !s.getStream().isBlank())
                ps.setString(8, s.getStream());
            else ps.setNull(8, Types.VARCHAR);
            if (s.getCourse() != null && !s.getCourse().isBlank())
                ps.setString(9, s.getCourse());
            else ps.setNull(9, Types.VARCHAR);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    // ── READ: by id ───────────────────────────────────────────────────────────
    public Student findById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ── READ: by email ────────────────────────────────────────────────────────
    public Student findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM students WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ── READ: by roll number ──────────────────────────────────────────────────
    public Student findByRollNo(String rollNo) throws SQLException {
        String sql = "SELECT * FROM students WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ── READ: all students by college_id ─────────────────────────────────────
    // Primary filter — uses college_id (set when student enters access key)
    public List<Student> getStudentsByCollegeId(int collegeId) throws SQLException {
        String sql =
            "SELECT * FROM students WHERE college_id = ? " +
            "ORDER BY class_level, full_name ASC";
        List<Student> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, collegeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── READ: search within college_id ────────────────────────────────────────
    public List<Student> searchByCollegeIdAndQuery(int collegeId, String query)
            throws SQLException {
        String sql =
            "SELECT * FROM students WHERE college_id = ? " +
            "AND (full_name LIKE ? OR roll_no LIKE ? OR email LIKE ?) " +
            "ORDER BY full_name ASC";
        String like = "%" + query + "%";
        List<Student> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, collegeId);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── READ: fallback by college_name (for old data without college_id) ──────
    public List<Student> getStudentsByCollege(String collegeName) throws SQLException {
        String sql =
            "SELECT * FROM students WHERE college_name = ? " +
            "ORDER BY class_level, full_name ASC";
        List<Student> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collegeName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── READ: search within college_name (fallback) ───────────────────────────
    public List<Student> searchByCollegeAndQuery(String collegeName, String query)
            throws SQLException {
        String sql =
            "SELECT * FROM students WHERE college_name = ? " +
            "AND (full_name LIKE ? OR roll_no LIKE ? OR email LIKE ?) " +
            "ORDER BY full_name ASC";
        String like = "%" + query + "%";
        List<Student> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collegeName);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── READ: all students ────────────────────────────────────────────────────
    public List<Student> getAllStudents() throws SQLException {
        String sql = "SELECT * FROM students ORDER BY created_at DESC";
        List<Student> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── READ: search all ──────────────────────────────────────────────────────
    public List<Student> searchStudents(String query) throws SQLException {
        String sql =
            "SELECT * FROM students " +
            "WHERE full_name LIKE ? OR roll_no LIKE ? OR email LIKE ? " +
            "ORDER BY full_name ASC";
        String like = "%" + query + "%";
        List<Student> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Student> searchByRollOrName(String query) throws SQLException {
        return searchStudents(query);
    }

    // ── COUNT ─────────────────────────────────────────────────────────────────
    public int countByCollege(String collegeName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE college_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collegeName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countByCollegeId(int collegeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE college_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, collegeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── DUPLICATE CHECKS ──────────────────────────────────────────────────────
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM students WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean rollNoExists(String rollNo) throws SQLException {
        String sql = "SELECT 1 FROM students WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // ── Check roll number within same college (by college_id) ─────────────────
    public boolean rollNoExistsInCollege(String rollNo, int collegeId) throws SQLException {
        String sql = "SELECT 1 FROM students WHERE roll_no = ? AND college_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ps.setInt   (2, collegeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // ── Check roll number within same college (by college_name fallback) ───────
    public boolean rollNoExistsInCollegeName(String rollNo, String collegeName)
            throws SQLException {
        String sql = "SELECT 1 FROM students WHERE roll_no = ? AND college_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ps.setString(2, collegeName);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────
    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId         (rs.getInt   ("id"));
        s.setFullName   (rs.getString("full_name"));
        s.setRollNo     (rs.getString("roll_no"));
        s.setEmail      (rs.getString("email"));
        s.setPassword   (rs.getString("password"));
        s.setCollegeName(rs.getString("college_name"));
        s.setClassLevel (rs.getString("class_level"));
        s.setStream     (rs.getString("stream"));
        s.setCourse     (rs.getString("course"));
        try { s.setCollegeId (rs.getInt("college_id")); } catch (Exception e) {}
        try { s.setCreatedAt (rs.getString("created_at")); } catch (Exception e) {}
        return s;
    }
}