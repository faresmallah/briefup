package com.briefup.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.briefup.dao.DeckDAO;
import com.briefup.dao.UserStatsDAO;
import com.briefup.db.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/createDeck")
public class CreateDeckServlet extends HttpServlet {

    private final DeckDAO deckDAO = new DeckDAO();
    private final UserStatsDAO statsDAO = new UserStatsDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Verify active user session
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

        if (userId == null) {
            response.sendRedirect("login.html");
            return;
        }

        // Extract deck fields
        String name = request.getParameter("name");
        String description = request.getParameter("description");

        if (name == null || name.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Deck name required");
            return;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            // Insert the new deck and request the generated ID
            String sql = "INSERT INTO decks (user_id, name, description) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, userId);
            stmt.setString(2, name.trim());
            stmt.setString(3, description != null ? description.trim() : "");
            stmt.executeUpdate();

            // Retrieve generated deck ID for frontend redirect
            ResultSet rs = stmt.getGeneratedKeys();
            int deckId = -1;
            if (rs.next()) deckId = rs.getInt(1);

            rs.close();
            stmt.close();

            // Update user statistics
            statsDAO.recomputeTotals(userId);

            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(String.valueOf(deckId));

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (conn != null) try { conn.close(); } catch (Exception ignored) {}
        }
    }
}
