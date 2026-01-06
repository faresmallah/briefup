package com.briefup.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a centralized method for establishing connections to
 * the application's MySQL database.
 *
 * This class loads the JDBC driver and returns a live Connection
 * instance using the configured URL, username, and password.
 */
public class DBConnection {

    // Database connection URL and credentials
    private static final String URL =
            "jdbc:mysql://localhost:3306/briefup?useSSL=false&serverTimezone=UTC";

    private static final String USER = "root";

    // Local development password
    private static final String PASSWORD = "Root1234!";

    /**
     * Initializes and returns a new database connection.
     *
     * @return a valid MySQL {@link Connection} instance
     * @throws SQLException           if the connection cannot be established
     * @throws ClassNotFoundException if the JDBC driver is not found
     */
    public static Connection getConnection()
            throws SQLException, ClassNotFoundException {

        // Ensure the MySQL driver is loaded
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Establish and return the database connection
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
