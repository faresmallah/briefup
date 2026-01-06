package com.briefup.models;

/** Represents a user-created flashcard deck. */
public class Deck {

    private int id;
    private int userId;
    private String name;
    private String description;
    private int cardCount;

    public Deck() {}

    public Deck(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Deck(int id, int userId, String name, String description, int cardCount) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.cardCount = cardCount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCardCount() { return cardCount; }
    public void setCardCount(int cardCount) { this.cardCount = cardCount; }
}
