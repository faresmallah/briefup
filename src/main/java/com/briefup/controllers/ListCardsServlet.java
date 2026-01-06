package com.briefup.controllers;

import java.io.IOException;
import java.util.List;

import com.briefup.dao.CardDAO;
import com.briefup.models.Card;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/listCards")
public class ListCardsServlet extends HttpServlet {

    private final CardDAO cardDAO = new CardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Validate deckId parameter
        String deckIdParam = request.getParameter("deckId");
        int deckId;
        try {
            deckId = Integer.parseInt(deckIdParam);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid deckId");
            return;
        }

        // Fetch all cards for the deck
        List<Card> cards = cardDAO.listCards(deckId);

        // Build JSON response
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);

            json.append("{");
            json.append("\"question\":\"").append(escape(c.getQuestion())).append("\",");
            json.append("\"answer\":\"").append(escape(c.getAnswer())).append("\"");
            json.append("}");

            if (i < cards.size() - 1) {
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
