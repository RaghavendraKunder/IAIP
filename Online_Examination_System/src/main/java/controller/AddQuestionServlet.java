package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dao.QuestionDAO;
import Model.Question;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/add-question")
public class AddQuestionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final Gson        gson        = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        setCors(res);
        res.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        setCors(res);
        res.setContentType("application/json;charset=UTF-8");

        // ── Read params ───────────────────────────────────────────────────────
        String examIdStr     = req.getParameter("examId");
        String questionText  = req.getParameter("questionText");
        String optionA       = req.getParameter("optionA");
        String optionB       = req.getParameter("optionB");
        String optionC       = req.getParameter("optionC");
        String optionD       = req.getParameter("optionD");
        String correctOption = req.getParameter("correctOption");
        String marksStr      = req.getParameter("marks");

        System.out.println("[AddQuestionServlet] examId=" + examIdStr
            + " question=" + questionText + " correct=" + correctOption);

        if (questionText == null || questionText.trim().isEmpty()) {
            writeJson(res, false, "Question text is required."); return;
        }
        if (correctOption == null || correctOption.trim().isEmpty()) {
            writeJson(res, false, "Correct option is required."); return;
        }
        if (examIdStr == null || examIdStr.trim().isEmpty()) {
            writeJson(res, false, "Exam ID is required."); return;
        }

        int examId = 0;
        int marks  = 1;
        try { examId = Integer.parseInt(examIdStr.trim()); }
        catch (Exception e) { writeJson(res, false, "Invalid exam ID."); return; }
        try { marks = Integer.parseInt(marksStr.trim()); }
        catch (Exception ignored) {}

        // ── Save to DB ────────────────────────────────────────────────────────
        try {
            Question q = new Question(
                0, examId, questionText.trim(),
                optionA  != null ? optionA.trim()  : "",
                optionB  != null ? optionB.trim()  : "",
                optionC  != null ? optionC.trim()  : "",
                optionD  != null ? optionD.trim()  : "",
                correctOption.trim().toUpperCase(),
                marks
            );
            int savedId = questionDAO.addQuestion(q);
            System.out.println("[AddQuestionServlet] savedId=" + savedId);

            if (savedId > 0) {
                writeJson(res, true, "Question added successfully.");
            } else {
                writeJson(res, false, "Failed to add question.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeJson(res, false, "Database error: " + e.getMessage());
        }
    }

    private void writeJson(HttpServletResponse res,
                           boolean success, String message) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", success);
        json.addProperty("message", message);
        res.setStatus(success ? 200 : 400);
        res.getWriter().write(gson.toJson(json));
    }

    private void setCors(HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin",      "http://localhost:8080");
        res.setHeader("Access-Control-Allow-Methods",     "POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers",     "Content-Type, X-Admin-Id");
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }
}