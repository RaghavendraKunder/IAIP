package service;

import dao.AdminDAO;
import dao.CollegeDAO;
import dao.CollegeDAO.College;
import dao.StudentDAO;
import Model.Admin;
import Model.Student;
import util.PasswordUtil;

import java.sql.SQLException;

/**
 * AuthService.java
 * ----------------
 * Business logic layer for registering students and admins.
 *
 * Rules enforced here (NOT in the servlet, NOT in the DAO):
 *  - Validates required fields are present
 *  - Checks for duplicate email / roll number
 *  - Hashes passwords and admin keys before touching the DB
 *  - Validates stream is provided for Class 11+ students
 *  - Validates course is provided for UG students
 */
public class AuthService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final CollegeDAO  collegeDAO = new CollegeDAO();
    private final AdminDAO   adminDAO   = new AdminDAO();

    // ── Classes that require a stream selection ───────────────────────────────────
    private static final java.util.Set<String> STREAM_REQUIRED =
            java.util.Set.of("11", "12", "UG", "PG");

    // ══════════════════════════════════════════════════════════════════════════════
    //  STUDENT REGISTRATION
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Validates and registers a new student.
     * @return RegisterResult carrying success flag + message
     */
    public RegisterResult registerStudent(
            String fullName,
            String rollNo,
            String email,
            String password,
            String confirmPassword,
            String collegeName,
            String accessKey,       // college access key (optional — validates & links college_id)
            String classLevel,
            String stream,
            String course
    ) {
        // ── 1. Required field checks ─────────────────────────────────────────────
        if (isBlank(fullName))    return fail("Full name is required.");
        if (isBlank(rollNo))      return fail("Roll number is required.");
        if (isBlank(email))       return fail("Email address is required.");
        if (isBlank(password))    return fail("Password is required.");
        if (isBlank(collegeName)) return fail("College / school name is required.");
        if (isBlank(classLevel))  return fail("Class / year is required.");

        // ── 2. Password rules ────────────────────────────────────────────────────
        if (password.length() < 8)
            return fail("Password must be at least 8 characters.");
        if (!password.equals(confirmPassword))
            return fail("Passwords do not match.");

        // ── 3. Stream required for Class 11 / 12 / UG / PG ──────────────────────
        if (STREAM_REQUIRED.contains(classLevel) && isBlank(stream))
            return fail("Please select a stream for Class " + classLevel + ".");

        // ── 4. Course required for UG ────────────────────────────────────────────
        if ("UG".equals(classLevel) && isBlank(course))
            return fail("Please enter the degree / course you are pursuing.");

        // ── 5. Validate access key + get college_id ──────────────────────────────
        // Do this FIRST so we can scope the roll number check to the college
        int collegeId = 0;
        if (!isBlank(accessKey)) {
            try {
                College college = collegeDAO.findByNameAndVerifyKey(
                    collegeName.trim(), accessKey.trim());
                if (college == null) {
                    return fail("Invalid access key for '" + collegeName.trim() +
                        "'. Please check with your institution.");
                }
                collegeId = college.id;
                System.out.println("[AuthService] Student linked to college_id=" + collegeId);
            } catch (SQLException e) {
                e.printStackTrace();
                return fail("Error validating access key. Please try again.");
            }
        }

        // ── 6. Duplicate checks — scoped to college ───────────────────────────────
        // Email must be globally unique (login identifier)
        // Roll number only needs to be unique within the same college
        try {
            if (studentDAO.emailExists(email))
                return fail("This email is already registered.");

            // Check roll number within same college only
            boolean rollTaken;
            if (collegeId > 0) {
                rollTaken = studentDAO.rollNoExistsInCollege(rollNo, collegeId);
            } else if (!isBlank(collegeName)) {
                rollTaken = studentDAO.rollNoExistsInCollegeName(rollNo, collegeName.trim());
            } else {
                rollTaken = studentDAO.rollNoExists(rollNo);
            }
            if (rollTaken)
                return fail("Roll number '" + rollNo + "' is already registered in your college.");

        } catch (SQLException e) {
            e.printStackTrace();
            return fail("Database error during duplicate check. Please try again.");
        }

        // ── 7. Hash password ──────────────────────────────────────────────────
        String hashedPassword = PasswordUtil.hash(password);

        // ── 8. Build model and persist ────────────────────────────────────────
        Student student = new Student(
            fullName.trim(),
            rollNo.trim(),
            email.trim().toLowerCase(),
            hashedPassword,
            collegeName.trim(),
            classLevel.trim(),
            isBlank(stream) ? null : stream.trim(),
            isBlank(course) ? null : course.trim()
        );
        if (collegeId > 0) student.setCollegeId(collegeId);

        try {
            int generatedId = studentDAO.registerStudent(student);
            if (generatedId > 0) {
                return success("Registered successfully! Welcome to " + collegeName.trim() + ".");
            } else {
                return fail("Registration failed. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Return the actual SQL error so we can debug it
            return fail("Database error: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  ADMIN REGISTRATION
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Validates and registers a new admin.
     * The admin's access key is hashed — it is never stored in plain text.
     */
    public RegisterResult registerAdmin(
            String fullName,
            String email,
            String password,
            String confirmPassword,
            String collegeName,
            String adminAccessKey
    ) {
        // ── 1. Required field checks ─────────────────────────────────────────────
        if (isBlank(fullName))      return fail("Full name is required.");
        if (isBlank(email))         return fail("Email address is required.");
        if (isBlank(password))      return fail("Password is required.");
        if (isBlank(collegeName))   return fail("College / institution name is required.");
        if (isBlank(adminAccessKey)) return fail("Admin access key is required.");

        // ── 2. Password rules ────────────────────────────────────────────────────
        if (password.length() < 8)
            return fail("Password must be at least 8 characters.");
        if (!password.equals(confirmPassword))
            return fail("Passwords do not match.");

        // ── 3. Duplicate email check ─────────────────────────────────────────────
        try {
            if (adminDAO.emailExists(email))
                return fail("An admin account with this email already exists.");
        } catch (SQLException e) {
            e.printStackTrace();
            return fail("Database error during duplicate check. Please try again.");
        }

        // ── 4. Hash password AND admin key ───────────────────────────────────────
        String hashedPassword = PasswordUtil.hash(password);
        String hashedKey      = PasswordUtil.hash(adminAccessKey);

        // ── 5. Build model and persist ───────────────────────────────────────────
        Admin admin = new Admin(
            fullName.trim(),
            email.trim().toLowerCase(),
            hashedPassword,
            collegeName.trim(),
            hashedKey
        );

        try {
            int generatedId = adminDAO.registerAdmin(admin);
            if (generatedId > 0) {
                return success("Admin account created successfully!");
            } else {
                return fail("Registration failed. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return fail("Database error during registration. Please try again.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private RegisterResult success(String message) {
        return new RegisterResult(true, message);
    }

    private RegisterResult fail(String message) {
        return new RegisterResult(false, message);
    }

    // ── Inner result type ────────────────────────────────────────────────────────

    /**
     * Simple value object returned by every register method.
     * The servlet reads this to decide what JSON to send back.
     */
    public static class RegisterResult {
        public final boolean success;
        public final String  message;

        RegisterResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}