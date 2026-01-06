package com.briefup.controllers;

import com.briefup.dao.UserStatsDAO;
import com.briefup.models.UserStats;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/leaderboard")
public class LeaderboardServlet extends HttpServlet {

    private final UserStatsDAO statsDAO = new UserStatsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Retrieve leaderboard rows sorted by score
        List<UserStats> rows = statsDAO.getLeaderboard();

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Build compact JSON array
        out.print("[");
        for (int i = 0; i < rows.size(); i++) {
            UserStats r = rows.get(i);

            out.print("{");
            out.printf("\"userName\":\"%s\",", escape(r.getUserName()));
            out.printf("\"score\":%d,", r.getScore());
            out.printf("\"totalDecks\":%d,", r.getTotalDecks());
            out.printf("\"totalCards\":%d", r.getTotalCards());
            out.print("}");

            if (i < rows.size() - 1) out.print(",");
        }
        out.print("]");
        out.flush();
    }

    /** Escapes JSON-sensitive characters. */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
