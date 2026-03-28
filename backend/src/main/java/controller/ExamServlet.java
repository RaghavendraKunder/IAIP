// controller/ExamServlet.java
package controller;

import Model.Exam;
import Model.Question;
import service.ExamService;
import service.ExamService.ExamLoadResult;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/exam")
public class ExamServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ExamService examService = new ExamService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // ── 1. Session / auth check ───────────────────────────────────────────────
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            // Not logged in — redirect to login page
            res.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        // Only STUDENT role can take exams
        String role = (String) session.getAttribute("role");
        if (!"STUDENT".equals(role)) {
            res.sendRedirect(req.getContextPath() + "/admin/dashboard.jsp");
            return;
        }

        // ── 2. Read and validate examId param ────────────────────────────────────
        String examIdParam = req.getParameter("examId");

        if (examIdParam == null || examIdParam.trim().isEmpty()) {
            forwardWithError(req, res, "No exam ID provided.");
            return;
        }

        int examId;
        try {
            examId = Integer.parseInt(examIdParam.trim());
        } catch (NumberFormatException e) {
            forwardWithError(req, res, "Invalid exam ID.");
            return;
        }

        // ── 3. Load exam + questions via service ──────────────────────────────────
        ExamLoadResult result = null;
		try {
			result = examService.loadExamForStudent(examId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        if (!result.success) {
            forwardWithError(req, res, result.message);
            return;
        }

        Exam           exam      = result.exam;
        List<Question> questions = result.questions;

        // ── 4. Set request attributes ─────────────────────────────────────────────
        req.setAttribute("exam",         exam);
        req.setAttribute("questions",    questions);
        req.setAttribute("examId",       exam.getId());
        req.setAttribute("durationMins", exam.getDurationMinutes());
        req.setAttribute("totalMarks",   exam.getTotalMarks());
        req.setAttribute("examTitle",    exam.getTitle());

        // ── 5. Forward to exam JSP ────────────────────────────────────────────────
        req.getRequestDispatcher("/WEB-INF/jsp/student/exam.jsp")
           .forward(req, res);
    }

    /** Forward to exam page with an error message displayed. */
    private void forwardWithError(HttpServletRequest req,
                                  HttpServletResponse res,
                                  String errorMessage)
            throws ServletException, IOException {

        req.setAttribute("error", errorMessage);
        req.getRequestDispatcher("/WEB-INF/jsp/student/exam-error.jsp")
           .forward(req, res);
    }
}