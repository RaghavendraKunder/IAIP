package dao;

import Model.Admin;
import util.DBConnection;

import java.sql.*;


public class AdminDAO {


    public int registerAdmin(Admin a) throws SQLException {
        String sql = """
            INSERT INTO admins
              (full_name, email, password, college_name, admin_key_hash)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getFullName());
            ps.setString(2, a.getEmail());
            ps.setString(3, a.getPassword());      // BCrypt hash
            ps.setString(4, a.getCollegeName());
            ps.setString(5, a.getAdminKeyHash());  // BCrypt hash of access key

            int rows = ps.executeUpdate();
            if (rows == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    // ── READ ─────────────────────────────────────────────────────────────────────

 
    public Admin findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM admins WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ── DUPLICATE CHECK ───────────────────────────────────────────────────────────

   
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM admins WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // ── MAPPER ───────────────────────────────────────────────────────────────────

    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setId(rs.getInt("id"));
        a.setFullName(rs.getString("full_name"));
        a.setEmail(rs.getString("email"));
        a.setPassword(rs.getString("password"));
        a.setCollegeName(rs.getString("college_name"));
        a.setAdminKeyHash(rs.getString("admin_key_hash"));
        return a;
    }
}

