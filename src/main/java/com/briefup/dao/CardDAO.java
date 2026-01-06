package com.briefup.dao;

import com.briefup.db.DBConnection;
import com.briefup.models.Card;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@link Card} entity.
 * <p>
 * Provides basic CRUD-style operations for cards that belong to a deck.
 * All methods use a new database connection per call via {@link DBConnection}.
 */
public class CardDAO {

    /**
     * Returns all cards for a given deck, ordered by their primary key.
     *
     * @param deckId the identifier of the deck whose cards should be listed
     * @return list of {@link Card} instances for the specified deck
     */
    public List<Card> listCards(int deckId) {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT * FROM cards WHERE deck_id = ? ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deckId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Card c = new Card(
                        rs.getString("question"),
                        rs.getString("answer")
                );
                c.setId(rs.getInt("id"));
                cards.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cards;
    }

    /**
     * Inserts a new card for the given deck.
     *
     * @param deckId   the identifier of the deck the card belongs to
     * @param question the question text (may be empty but not null ideally)
     * @param answer   the answer text (may be empty but not null ideally)
     */
    public void insertCard(int deckId, String question, String answer) {
        String sql = "INSERT INTO cards (deck_id, question, answer) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deckId);
            stmt.setString(2, question);
            stmt.setString(3, answer);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes all cards that belong to the specified deck.
     * <p>
     * Used when a deck is being resaved with a completely new set of cards.
     *
     * @param deckId the identifier of the deck whose cards should be removed
     */
    public void deleteCardsByDeck(int deckId) {
        String sql = "DELETE FROM cards WHERE deck_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deckId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
