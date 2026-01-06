package com.briefup.models;

import java.util.ArrayList;
import java.util.List;

/** Represents an application user. */
public class User {

    private int id;
    private String name;
    private String email;
    private String password;

    // Legacy in-memory deck list (not used for DB operations)
    private List<Deck> decks = new ArrayList<>();

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<Deck> getDecks() { return decks; }
    public void setDecks(List<Deck> decks) { this.decks = decks; }
}
