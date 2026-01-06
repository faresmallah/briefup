package com.briefup.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.briefup.dao.CardDAO;
import com.briefup.dao.DeckDAO;
import com.briefup.dao.UserStatsDAO;
import com.briefup.db.DBConnection;
import com.briefup.models.Deck;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/saveDeck")
public class SaveDeckServlet extends HttpServlet {

    private final CardDAO cardDAO = new CardDAO();
    private final DeckDAO deckDAO = new DeckDAO();
    private final UserStatsDAO statsDAO = new UserStatsDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Parse deck parameters
        String deckIdParam = request.getParameter("deckId");
        String countParam  = request.getParameter("count");

        int deckId;
        int count;

        try {
            deckId = Integer.parseInt(deckIdParam);
            count  = Integer.parseInt(countParam);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
            return;
        }

        Connection conn = null;

        try {
            // Database connection for card operations
            conn = DBConnection.getConnection();

            // Remove existing cards before saving new content
            cardDAO.deleteCardsByDeck(deckId);

            // Insert all cards from the request
            for (int i = 0; i < count; i++) {
                String q = request.getParameter("question" + i);
                String a = request.getParameter("answer" + i);

                if (q != null || a != null) {
                    cardDAO.insertCard(
                        deckId,
                        q == null ? "" : q.trim(),
                        a == null ? "" : a.trim()
                    );
                }
            }

            // Update user statistics after changes
            Deck deck = deckDAO.getDeck(deckId);
            if (deck != null) {
                statsDAO.recomputeTotals(deck.getUserId());
            }

            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("success");

            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Save failed: " + e.getMessage()
            );

        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
