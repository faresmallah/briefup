package com.briefup.controllers;

import com.briefup.dao.UserStatsDAO;
import com.briefup.models.UserStats;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/userStats")
public class GetStatsServlet extends HttpServlet {

    private final UserStatsDAO statsDAO = new UserStatsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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

        // Ensure stats record exists BEFORE recomputing
        UserStats stats = statsDAO.getStats(userId);
        if (stats == null) {
            statsDAO.createStats(userId);
        }

        // Now recompute totals to ensure they're current
        statsDAO.recomputeTotals(userId);
        
        // Retrieve the updated stats
        stats = statsDAO.getStats(userId);

        // Prepare JSON response
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String name = (stats != null && stats.getUserName() != null)
                ? stats.getUserName()
                : "Learner";

        out.print("{");
        out.printf("\"userName\":\"%s\",", escape(name));
        out.printf("\"totalDecks\":%d,", stats != null ? stats.getTotalDecks() : 0);
        out.printf("\"totalCards\":%d,", stats != null ? stats.getTotalCards() : 0);
        out.printf("\"cardsStudied\":%d,", stats != null ? stats.getCardsStudied() : 0);
        out.printf("\"studySessions\":%d,", stats != null ? stats.getStudySessions() : 0);
        out.printf("\"streak\":%d,", stats != null ? stats.getStreak() : 0);
        out.printf("\"score\":%d,", stats != null ? stats.getScore() : 0);
        out.printf("\"masteredCards\":%d", stats != null ? stats.getMasteredCards() : 0); // CHANGED: Use stats.getMasteredCards() instead of statsDAO.getMasteredCardsCount()
        out.print("}");
        out.flush();
    }

    /** Escape JSON-unsafe characters. */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}