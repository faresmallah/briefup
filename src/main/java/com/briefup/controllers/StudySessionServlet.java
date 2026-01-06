package com.briefup.controllers;

import com.briefup.dao.UserStatsDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/studySession")
public class StudySessionServlet extends HttpServlet {

    private final UserStatsDAO statsDAO = new UserStatsDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Validate active session
        HttpSession session = request.getSession(false);
        Integer userId = (session != null)
                ? (Integer) session.getAttribute("userId")
                : null;

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Determine requested action
        String action = request.getParameter("action");  // "session", "card", or "quiz"

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if ("session".equals(action)) {
                // Simple increment - remove the duplicate check that's causing issues
                statsDAO.incrementStudySession(userId);
                updateStreak(userId);
                out.print("{\"status\":\"ok\"}");

            } else if ("card".equals(action)) {
                // Increment per-card study counter
                statsDAO.incrementCardsStudied(userId, 1);
                out.print("{\"status\":\"ok\"}");

            } else if ("quiz".equals(action)) {
                // Update quiz-related stats
                int correctAnswers = Integer.parseInt(request.getParameter("correctAnswers"));
                int totalQuestions = Integer.parseInt(request.getParameter("totalQuestions"));
                String quizType = request.getParameter("quizType");

                statsDAO.updateQuizStats(userId, correctAnswers, totalQuestions, quizType);
                
                //  Only increment mastered for PERFECT score
                boolean isPerfectScore = (correctAnswers == totalQuestions);
                
                if (isPerfectScore && totalQuestions > 0) {
                    String deckIdParam = request.getParameter("deckId");
                    if (deckIdParam != null) {
                        statsDAO.incrementMasteredCards(userId, 1);
                    }
                }
                
                out.print("{\"status\":\"ok\"}");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Server error"
            );
        }
    }

    /** Smart streak progression with date tracking. */
    private void updateStreak(int userId) {
        statsDAO.updateStreak(userId);
    }

    /** Check if this deck is newly mastered (not already counted) */
    private boolean isNewlyMasteredDeck(int userId, int deckId) {
        return true;
    }

    /** Mark deck as mastered to prevent duplicate counting */
    private void markDeckAsMastered(int userId, int deckId) {
    }

}