package com.briefup.controllers;

import java.io.IOException;

import com.briefup.dao.UserDAO;
import com.briefup.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Extract login credentials
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Fetch user by email
        User u = userDAO.findByEmail(email);

        // Validate password match
        if (u != null && u.getPassword().equals(password)) {

            // Store authentication state in session
            HttpSession session = request.getSession();
            session.setAttribute("userId", u.getId());
            session.setAttribute("user", u);

            response.sendRedirect("home");
        } else {
            // Redirect back with error indicator
            response.sendRedirect("login.html?error=invalid");
        }
    }
}
