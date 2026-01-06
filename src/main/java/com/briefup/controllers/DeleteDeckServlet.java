package com.briefup.controllers;

import java.io.IOException;

import com.briefup.dao.DeckDAO;
import com.briefup.dao.UserStatsDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/deleteDeck")
public class DeleteDeckServlet extends HttpServlet {

    private final DeckDAO deckDAO = new DeckDAO();
    private final UserStatsDAO statsDAO = new UserStatsDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Validate session and user identity
        HttpSession session = request.getSession(false);
        Integer userId = (session != null)
                ? (Integer) session.getAttribute("userId")
                : null;

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Extract deck name
        String deckName = request.getParameter("name");
        if (deckName == null || deckName.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing deck name");
            return;
        }

        try {
            // Delete deck by name for this user
            deckDAO.deleteDeck(userId, deckName.trim());

            // Refresh user statistics after deletion
            statsDAO.recomputeTotals(userId);

            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("success");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Deck deletion failed"
            );
        }
    }
}
