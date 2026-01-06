package com.briefup.ai;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AiFlashcardService {

    // Flashcard structure used by servlets
    public static class Flashcard {
        public String question;
        public String answer;
        public Flashcard(String q, String a) {
            this.question = q;
            this.answer = a;
        }
    }

    // Accepts multiple text chunks and merges them before processing
    public List<Flashcard> generateFlashcards(List<String> chunks, int numCards) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String c : chunks) {
            sb.append(c).append(". ");
        }
        return generateFlashcards(sb.toString(), numCards);
    }

    // Generates flashcards through the OpenAI API
    public List<Flashcard> generateFlashcards(String rawText, int numCards) throws Exception {

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            String apiKey = System.getenv("OPENAI_API_KEY");

        }

        // Safety limit for prompt length
        if (rawText.length() > 8000) {
            rawText = rawText.substring(0, 8000);
        }

        String prompt =
                "Generate " + numCards + " short flashcards.\n" +
                "Format strictly as:\n" +
                "Q: <very short question>\n" +
                "A: <very short answer>\n" +
                "---\n\n" +
                "Rules:\n" +
                "- Question must be 3–6 words only.\n" +
                "- Answer must be one short sentence.\n" +
                "- No additional commentary.\n\n" +
                "Source text:\n" + rawText;

        String jsonBody = """
                {
                  "model": "gpt-4o-mini",
                  "messages": [
                    {"role": "system", "content": "You create flashcards."},
                    {"role": "user", "content": %s}
                  ],
                  "temperature": 0.3
                }
                """.formatted(toJson(prompt));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        return parseFlashcards(extractContent(response.body()));
    }

    // Escapes text so it can be safely included inside a JSON string
    private static String toJson(String s) {
        if (s == null) return "\"\"";

        s = s.replace("\\", "\\\\")
             .replace("\"", "\\\"")
             .replace("\n", "\\n")
             .replace("\r", "")
             .replace("\t", " ");

        return "\"" + s + "\"";
    }

    // Extracts the "content" field from the OpenAI JSON response
    private static String extractContent(String json) {
        String contentKey = "\"content\":";
        int idx = json.indexOf(contentKey);
        if (idx == -1) return "";

        idx = json.indexOf("\"", idx + contentKey.length());
        if (idx == -1) return "";
        idx++;

        StringBuilder sb = new StringBuilder();
        boolean escape = false;

        for (int i = idx; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escape) {
                switch (c) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    default: sb.append(c);
                }
                escape = false;
            } else {
                if (c == '\\') escape = true;
                else if (c == '"') break;
                else sb.append(c);
            }
        }

        return sb.toString();
    }

    // Converts raw OpenAI text output into structured flashcards
    private static List<Flashcard> parseFlashcards(String txt) {
        List<Flashcard> list = new ArrayList<>();
        if (txt == null || txt.isBlank()) return list;

        txt = txt.replace("  \n", "\n");
        String[] blocks = txt.split("(?m)^-{3,}\\s*$");

        for (String block : blocks) {
            String q = null, a = null;

            for (String line : block.split("\n")) {
                line = line.trim();

                if (line.startsWith("Q:")) q = line.substring(2).trim();
                if (line.startsWith("A:")) a = line.substring(2).trim();
            }

            if (q != null && !q.isBlank() && a != null && !a.isBlank()) {
                list.add(new Flashcard(q, a));
            }
        }

        return list;
    }
}
