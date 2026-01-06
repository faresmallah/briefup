package com.briefup.controllers;

import java.io.IOException;

import com.briefup.dao.DeckDAO;
import com.briefup.dao.UserStatsDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/deleteDeckById")
public class DeleteDeckByIdServlet extends HttpServlet {

    private final DeckDAO deckDAO = new DeckDAO();
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

        // Read deck ID parameter
        String deckIdParam = request.getParameter("deckId");
        if (deckIdParam == null || deckIdParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing deck ID");
            return;
        }

        try {
            int deckId = Integer.parseInt(deckIdParam);

            // Lookup deck to verify ownership before deletion
            var deck = deckDAO.getDeck(deckId);
            if (deck != null && deck.getUserId() == userId) {

                // Perform deletion and update statistics
                deckDAO.deleteDeckById(deckId);
                statsDAO.recomputeTotals(userId);

                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("success");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Deck not found or access denied");
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid deck ID");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Deck deletion failed");
        }
    }
}
