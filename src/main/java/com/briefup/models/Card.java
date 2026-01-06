package com.briefup.models;

/** Represents a single flashcard containing a question and answer. */
public class Card {

    private int id;
    private String question;
    private String answer;

    public Card() {}

    public Card(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public int getId() { 
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }

    public String getQuestion() { 
        return question; 
    }
    public void setQuestion(String question) { 
        this.question = question; 
    }

    public String getAnswer() { 
        return answer; 
    }
    public void setAnswer(String answer) { 
        this.answer = answer; 
    }
}
