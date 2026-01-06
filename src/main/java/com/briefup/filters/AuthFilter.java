package com.briefup.filters;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.*;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // Public pages and static assets
        boolean isPublic =
                path.endsWith("main.html") ||
                path.endsWith("login.html") ||
                path.endsWith("signup.html") ||
                path.endsWith("login") ||
                path.endsWith("signup") ||
                path.contains("/css/") ||
                path.contains("/js/") ||
                path.contains("/img/");

        // AJAX endpoints that must be allowed prior to page load
        boolean isDeckAjax =
                path.endsWith("listDecks") ||
                path.endsWith("createDeck") ||
                path.endsWith("deleteDeck") ||
                path.endsWith("listCards") ||
                path.endsWith("saveDeck");

        // JSP pages gated behind authentication
        boolean isDeckPage =
                path.contains("deck.jsp") ||
                path.contains("home.jsp") ||
                path.endsWith("home");

        // Check session
        HttpSession session = req.getSession(false);
        Integer userId = (session != null)
                ? (Integer) session.getAttribute("userId")
                : null;

        // Restrict access when user is not authenticated
        if (!isPublic && !isDeckAjax && !isDeckPage && userId == null) {
            res.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }

        // Continue request chain
        chain.doFilter(request, response);
    }
}
