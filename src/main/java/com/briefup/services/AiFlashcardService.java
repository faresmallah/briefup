package com.briefup.services;

import java.util.ArrayList;
import java.util.List;

public class AiFlashcardService {

    // Placeholder API key; unused in this fallback generator
    String apiKey = System.getenv("OPENAI_API_KEY");

    public AiFlashcardService() {}

    // Basic fallback flashcard generator using simple text segmentation
    public List<String[]> generateFlashcards(String text, int numCards) {
        List<String[]> cards = new ArrayList<>();

        String[] lines = text.split("[\\n\\.]+");
        int index = 0;

        while (cards.size() < numCards && index < lines.length) {
            String s = lines[index].trim();

            if (s.length() > 10) {
                cards.add(new String[]{
                        summarizeQuestion(s),
                        summarizeAnswer(s)
                });
            }

            index++;
        }

        return cards;
    }

    // Shortens long text and formats it as a basic question
    private String summarizeQuestion(String s) {
        if (s.length() > 35)
            s = s.substring(0, 35) + "...";
        return "What is: " + s + "?";
    }

    // Produces the answer portion with optional truncation
    private String summarizeAnswer(String s) {
        if (s.length() > 110)
            s = s.substring(0, 110) + "...";
        return s;
    }
}
