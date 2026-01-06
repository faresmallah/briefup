package com.briefup.dao;

import com.briefup.db.DBConnection;
import com.briefup.models.Deck;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link Deck}.
 * <p>
 * Handles creation, retrieval, listing, and deletion of decks.
 * All database connections are obtained through {@link DBConnection}.
 */
public class DeckDAO {

    /**
     * Creates a new deck for the specified user.
     *
     * @param userId      the owner of the deck
     * @param name        the deck name
     * @param description optional deck description
     */
    public void createDeck(int userId, String name, String description) {
        String sql = "INSERT INTO decks (user_id, name, description) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a deck by its identifier.
     *
     * @param deckId the deck identifier
     * @return a {@link Deck} instance or null if not found
     */
    public Deck getDeck(int deckId) {
        String sql = "SELECT * FROM decks WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deckId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Deck deck = new Deck(
                        rs.getString("name"),
                        rs.getString("description")
                );
                deck.setId(rs.getInt("id"));
                deck.setUserId(rs.getInt("user_id"));
                return deck;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lists all decks for a given user, including card count.
     *
     * @param userId the owner of the decks
     * @return list of {@link Deck} objects
     */
    public List<Deck> listDecks(int userId) {
        List<Deck> decks = new ArrayList<>();

        String sql = """
            SELECT d.*, COUNT(c.id) AS cardCount
            FROM decks d
            LEFT JOIN cards c ON c.deck_id = d.id
            WHERE d.user_id = ?
            GROUP BY d.id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Deck deck = new Deck(
                        rs.getString("name"),
                        rs.getString("description")
                );
                deck.setId(rs.getInt("id"));
                deck.setUserId(rs.getInt("user_id"));
                deck.setCardCount(rs.getInt("cardCount"));
                decks.add(deck);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return decks;
    }
    
    /**
     * Deletes a deck by its name for the specified user.
     *
     * @param userId   the owner
     * @param deckName the deck name to remove
     */
    public void deleteDeck(int userId, String deckName) {
        String sql = "DELETE FROM decks WHERE user_id = ? AND name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, deckName);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    /**
     * Deletes a deck by its identifier.
     * <p>
     * Note: Cards are expected to be removed separately
     *       by the card management layer before removing the deck.
     *
     * @param deckId the deck identifier to remove
     */
    public void deleteDeckById(int deckId) {
        String sql = "DELETE FROM decks WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deckId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
