package com.briefup.controllers;

import java.io.IOException;
import java.util.List;

import com.briefup.dao.DeckDAO;
import com.briefup.models.Deck;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/listDecks")
public class ListDecksServlet extends HttpServlet {

    private final DeckDAO deckDAO = new DeckDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Validate session and retrieve current user ID
        HttpSession session = request.getSession(false);
        Integer userId = (session != null)
                ? (Integer) session.getAttribute("userId")
                : null;

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        // Fetch all decks belonging to the user
        List<Deck> decks = deckDAO.listDecks(userId);

        // Build JSON array response
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < decks.size(); i++) {
            Deck d = decks.get(i);

            json.append("{");
            json.append("\"id\":").append(d.getId()).append(",");
            json.append("\"name\":\"").append(escape(d.getName())).append("\",");
            json.append("\"description\":\"").append(escape(d.getDescription())).append("\",");
            json.append("\"cardCount\":").append(d.getCardCount());
            json.append("}");

            if (i < decks.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json.toString());
    }

    /** Escape JSON-sensitive characters. */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
