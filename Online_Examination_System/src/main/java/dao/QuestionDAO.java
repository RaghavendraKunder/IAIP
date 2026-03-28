// dao/QuestionDAO.java
package dao;

import Model.Question;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    // ── INSERT ────────────────────────────────────────────────────────────────────

    /**
     * Inserts a new question and returns the generated ID.
     * @return generated question ID, or -1 on failure
     */
    public int addQuestion(Question q) throws SQLException {
        String sql = "INSERT INTO questions " +
                     "(exam_id, question_text, option_a, option_b, " +
                     " option_c, option_d, correct_option, marks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, q.getExamId());
            ps.setString(2, q.getQuestionText());
            ps.setString(3, q.getOptionA());
            ps.setString(4, q.getOptionB());
            ps.setString(5, q.getOptionC());
            ps.setString(6, q.getOptionD());
            ps.setString(7, q.getCorrectOption());
            ps.setInt(8, q.getMarks());

            int rows = ps.executeUpdate();
            if (rows == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    // ── READ ──────────────────────────────────────────────────────────────────────

    /** Fetch all questions for a given exam ID. */
    public List<Question> getQuestionsByExamId(int examId) throws SQLException {
        String sql = "SELECT * FROM questions WHERE exam_id = ?";
        List<Question> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Fetch a single question by its ID. */
    public Question getQuestionById(int id) throws SQLException {
        String sql = "SELECT * FROM questions WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Count how many questions an exam has. */
    public int countByExamId(int examId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM questions WHERE exam_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────────

    private Question mapRow(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setExamId(rs.getInt("exam_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setCorrectOption(rs.getString("correct_option"));
        q.setMarks(rs.getInt("marks"));
        return q;
    }
}
