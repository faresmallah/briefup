package com.briefup.dao;

import com.briefup.db.DBConnection;
import com.briefup.models.User;

import java.sql.*;

/**
 * Data Access Object for {@link User}.
 * <p>
 * Provides database operations for user registration and authentication.
 * All database connections are managed through {@link DBConnection}.
 */
public class UserDAO {

    /**
     * Inserts a new user into the database.
     * The generated primary key is stored in the provided {@link User} instance.
     *
     * @param user a populated user entity
     * @return true if the user was inserted successfully, false otherwise
     */
    public boolean insert(User user) {
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            // Retrieve generated ID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                user.setId(rs.getInt(1));
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a user by email. Primarily used for authentication.
     *
     * @param email user email
     * @return a {@link User} instance if found, otherwise null
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")
                );
                user.setId(rs.getInt("id"));
                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
}
