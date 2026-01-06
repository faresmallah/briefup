package com.briefup.controllers;

import java.io.IOException;

import com.briefup.dao.UserDAO;
import com.briefup.dao.UserStatsDAO;
import com.briefup.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final UserStatsDAO statsDAO = new UserStatsDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Extract user input
        String name     = request.getParameter("name");
        String email    = request.getParameter("email");
        String password = request.getParameter("password");

        // Validate required fields
        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || name == null || name.trim().isEmpty()) {

            response.sendRedirect("signup.html?error=missing_fields");
            return;
        }

        // Check for existing email
        if (userDAO.findByEmail(email) != null) {
            response.sendRedirect("signup.html?error=email_exists");
            return;
        }

        // Create new user instance
        User newUser = new User(name.trim(), email.trim(), password.trim());

        // Insert into DB
        boolean success = userDAO.insert(newUser);

        if (success) {
            // Initialize user statistics
            statsDAO.createStats(newUser.getId());
            response.sendRedirect("login.html?success=1");
        } else {
            response.sendRedirect("signup.html?error=1");
        }
    }
}
