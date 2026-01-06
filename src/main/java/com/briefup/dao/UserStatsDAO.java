package com.briefup.dao;

import com.briefup.models.UserStats;
import com.briefup.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for user statistics.
 * <p>
 * Handles creation, retrieval, and updates of study-related metrics,
 * leaderboard values, and aggregated deck/card totals.
 */
public class UserStatsDAO {

    /**
     * Creates an initial statistics record for a new user.
     *
     * @param userId the ID of the newly created user
     */
    public void createStats(int userId) {
        String sql = """
            INSERT INTO user_stats (
                user_id, total_decks, total_cards,
                cards_studied, study_sessions,
                streak, score, mastered_cards
            )
            VALUES (?, 0, 0, 0, 0, 0, 0, 0)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves study statistics for a given user.
     *
     * @param userId user identifier
     * @return a populated {@link UserStats} object or null if not found
     */
    public UserStats getStats(int userId) {
        String sql = """
            SELECT u.name,
                   s.total_decks, s.total_cards,
                   s.cards_studied, s.study_sessions,
                   s.streak, s.score, s.mastered_cards
            FROM user_stats s
            JOIN users u ON u.id = s.user_id
            WHERE s.user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserStats stats = new UserStats();
                stats.setUserName(rs.getString("name"));
                stats.setTotalDecks(rs.getInt("total_decks"));
                stats.setTotalCards(rs.getInt("total_cards"));
                stats.setCardsStudied(rs.getInt("cards_studied"));
                stats.setStudySessions(rs.getInt("study_sessions"));
                stats.setStreak(rs.getInt("streak"));
                stats.setScore(rs.getInt("score"));
                stats.setMasteredCards(rs.getInt("mastered_cards"));
                return stats;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Increments the study session count and updates the score accordingly.
     *
     * @param userId user identifier
     */
    public void incrementStudySession(int userId) {
        String sql = """
            UPDATE user_stats
            SET study_sessions = study_sessions + 1,
                score = score + 5
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Increments the total number of studied cards and awards score points.
     *
     * @param userId the user performing the study action
     * @param amount number of cards studied
     */
    public void incrementCardsStudied(int userId, int amount) {
        String sql = """
            UPDATE user_stats
            SET cards_studied = cards_studied + ?,
                score = score + ?
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, amount);
            stmt.setInt(2, amount);
            stmt.setInt(3, userId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Increments a user's streak counter.
     *
     * @param userId user identifier
     */
    public void incrementStreak(int userId) {
        String sql = """
            UPDATE user_stats
            SET streak = streak + 1
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates deck and card totals when recalculating aggregated statistics.
     *
     * @param userId     user identifier
     * @param totalDecks computed deck count
     * @param totalCards computed card count
     */
    public void updateTotals(int userId, int totalDecks, int totalCards) {
        String sql = """
            UPDATE user_stats
            SET total_decks = ?, total_cards = ?
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, totalDecks);
            stmt.setInt(2, totalCards);
            stmt.setInt(3, userId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Recalculates deck and card totals directly from database state.
     *
     * @param userId user identifier
     */
    public void recomputeTotals(int userId) {
        String sql = """
            SELECT 
                (SELECT COUNT(*) FROM decks WHERE user_id = ?) AS totalDecks,
                (SELECT COUNT(*) 
                 FROM cards c 
                 JOIN decks d ON c.deck_id = d.id
                 WHERE d.user_id = ?) AS totalCards
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                updateTotals(userId, rs.getInt("totalDecks"), rs.getInt("totalCards"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves leaderboard entries sorted by highest score.
     *
     * @return list of {@link UserStats} rows
     */
    public List<UserStats> getLeaderboard() {
        List<UserStats> list = new ArrayList<>();

        String sql = """
            SELECT u.name,
                   s.total_decks, s.total_cards,
                   s.cards_studied, s.study_sessions,
                   s.streak, s.score, s.mastered_cards
            FROM user_stats s
            JOIN users u ON u.id = s.user_id
            ORDER BY s.score DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UserStats stats = new UserStats();
                stats.setUserName(rs.getString("name"));
                stats.setTotalDecks(rs.getInt("total_decks"));
                stats.setTotalCards(rs.getInt("total_cards"));
                stats.setCardsStudied(rs.getInt("cards_studied"));
                stats.setStudySessions(rs.getInt("study_sessions"));
                stats.setStreak(rs.getInt("streak"));
                stats.setScore(rs.getInt("score"));
                stats.setMasteredCards(rs.getInt("mastered_cards"));
                list.add(stats);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Updates statistics based on quiz performance.
     *
     * @param userId          user identifier
     * @param correctAnswers  number of correct answers in quiz
     * @param totalQuestions  number of questions in quiz
     * @param quizType        quiz mode used
     */
    public void updateQuizStats(int userId, int correctAnswers, int totalQuestions, String quizType) {
        int baseScore = correctAnswers * 10;
        int bonus = "final".equals(quizType) ? 50 : 20;
        int totalScore = baseScore + bonus;

        String sql = """
            UPDATE user_stats
            SET cards_studied = cards_studied + ?,
                study_sessions = study_sessions + 1,
                score = score + ?,
                streak = GREATEST(streak, 1)
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, totalQuestions);
            stmt.setInt(2, totalScore);
            stmt.setInt(3, userId);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Smart streak calculation based on consecutive days
     */
    public void updateStreak(int userId) {
        String sql = """
            UPDATE user_stats 
            SET last_study_date = CURDATE(),
                streak = CASE 
                    WHEN last_study_date IS NULL THEN 1
                    WHEN last_study_date = CURDATE() THEN streak  -- Same day, no change
                    WHEN last_study_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY) THEN streak + 1  -- Consecutive day
                    ELSE 1  -- Broken streak, reset to 1
                END
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Increment mastered cards properly
     */
    public void incrementMasteredCards(int userId, int count) {
        String sql = "UPDATE user_stats SET mastered_cards = COALESCE(mastered_cards, 0) + ? WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, count);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}