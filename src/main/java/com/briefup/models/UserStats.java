package com.briefup.models;

/** Stores progress and performance statistics for a user. */
public class UserStats {

    private int userId;
    private int totalDecks;
    private int totalCards;
    private int cardsStudied;
    private int studySessions;
    private int streak;
    private int score;
    private int masteredCards; 
    private String userName; // used for leaderboard display
    
    public int getMasteredCards() { return masteredCards; }
    public void setMasteredCards(int masteredCards) { this.masteredCards = masteredCards; }


    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTotalDecks() { return totalDecks; }
    public void setTotalDecks(int totalDecks) { this.totalDecks = totalDecks; }

    public int getTotalCards() { return totalCards; }
    public void setTotalCards(int totalCards) { this.totalCards = totalCards; }

    public int getCardsStudied() { return cardsStudied; }
    public void setCardsStudied(int cardsStudied) { this.cardsStudied = cardsStudied; }

    public int getStudySessions() { return studySessions; }
    public void setStudySessions(int studySessions) { this.studySessions = studySessions; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
