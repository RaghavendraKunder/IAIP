package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * -----------------
 * Single-responsibility class that provides JDBC connections to the
 * ExamFlow MySQL database.
 *
 * Place mysql-connector-j-x.x.x.jar inside WEB-INF/lib/
 */
public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/examflow"
                                         + "?useSSL=false&serverTimezone=UTC"
                                         + "&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "Sujat@1972"; // ← change this

    // Load driver once when the class is first used
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. "
                + "Add mysql-connector-j to WEB-INF/lib/", e);
        }
    }

    /** Returns a fresh connection. Caller is responsible for closing it. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
