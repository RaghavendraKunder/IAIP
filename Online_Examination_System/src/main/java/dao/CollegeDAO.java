package dao;

import Model.Admin;
import util.DBConnection;
import util.PasswordUtil;

import java.sql.*;

/**
 * CollegeDAO.java
 * ---------------
 * Handles the colleges table in examflow database.
 * Each college has a unique access key that students
 * must enter when registering to link to that college.
 */
public class CollegeDAO {

    // ── Register new college ──────────────────────────────────────────────────
    public int registerCollege(String collegeName, String adminName,
                               String email, String hashedPassword,
                               String hashedAccessKey) throws SQLException {
        String sql =
            "INSERT INTO colleges (college_name, admin_name, email, password, admin_key_hash) " +
            "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, collegeName);
            ps.setString(2, adminName);
            ps.setString(3, email);
            ps.setString(4, hashedPassword);
            ps.setString(5, hashedAccessKey);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    // ── Find by email (for admin login) ───────────────────────────────────────
    public Admin findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM colleges WHERE email = ? AND is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ── Find college by name + verify access key ──────────────────────────────
    // Used during student registration to validate the access key
    public College findByNameAndVerifyKey(String collegeName, String accessKey)
            throws SQLException {
        String sql =
            "SELECT id, college_name, admin_key_hash " +
            "FROM colleges WHERE college_name = ? AND is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collegeName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String storedHash = rs.getString("admin_key_hash");
                if (!PasswordUtil.verify(accessKey, storedHash)) return null;
                // Key matched — return college info
                College c = new College();
                c.id          = rs.getInt("id");
                c.collegeName = rs.getString("college_name");
                return c;
            }
        }
    }

    // ── Get college by id ─────────────────────────────────────────────────────
    public College findById(int id) throws SQLException {
        String sql = "SELECT id, college_name FROM colleges WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                College c = new College();
                c.id          = rs.getInt("id");
                c.collegeName = rs.getString("college_name");
                return c;
            }
        }
    }

    // ── Check duplicates ──────────────────────────────────────────────────────
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM colleges WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean collegeNameExists(String name) throws SQLException {
        String sql = "SELECT id FROM colleges WHERE college_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // ── Inner helper class ────────────────────────────────────────────────────
    public static class College {
        public int    id;
        public String collegeName;
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setId          (rs.getInt   ("id"));
        a.setFullName    (rs.getString("admin_name"));
        a.setEmail       (rs.getString("email"));
        a.setPassword    (rs.getString("password"));
        a.setCollegeName (rs.getString("college_name"));
        a.setAdminKeyHash(rs.getString("admin_key_hash"));
        return a;
    }
}