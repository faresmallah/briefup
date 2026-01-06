package com.briefup.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.briefup.dao.CardDAO;
import com.briefup.dao.DeckDAO;
import com.briefup.models.Deck;
import com.briefup.ai.AiFlashcardService;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/uploadPdfDeck")
@MultipartConfig
public class UploadPdfDeckServlet extends HttpServlet {

    private final DeckDAO deckDAO = new DeckDAO();
    private final CardDAO cardDAO = new CardDAO();
    private final AiFlashcardService ai = new AiFlashcardService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Extract form fields
        String deckName = request.getParameter("deckName");
        String deckDesc = request.getParameter("deckDesc");
        int numCards = Integer.parseInt(request.getParameter("numCards"));

        // Validate file upload
        Part pdfPart = request.getPart("pdfFile");
        if (pdfPart == null || pdfPart.getSize() == 0) {
            response.sendRedirect("home.jsp?error=emptyfile");
            return;
        }

        // Extract text content from the PDF file
        String extractedText = "";
        try (InputStream is = pdfPart.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            try (PDDocument doc = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                extractedText = stripper.getText(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("home.jsp?error=pdfread");
            return;
        }

        if (extractedText.trim().isEmpty()) {
            response.sendRedirect("home.jsp?error=pdfempty");
            return;
        }

        // Create new deck
        int userId = (int) request.getSession().getAttribute("userId");
        deckDAO.createDeck(userId, deckName, deckDesc);

        // Retrieve the newly created deck (last inserted for this user)
        List<Deck> decks = deckDAO.listDecks(userId);
        Deck newDeck = decks.get(decks.size() - 1);
        int deckId = newDeck.getId();

        try {
            // Generate flashcards via AI
            List<AiFlashcardService.Flashcard> cards =
                    ai.generateFlashcards(extractedText, numCards);

            // Insert generated cards
            for (AiFlashcardService.Flashcard c : cards) {
                cardDAO.insertCard(deckId, c.question, c.answer);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("home.jsp?error=ai_failed");
            return;
        }

        // Redirect user to the generated deck
        response.sendRedirect("deck.jsp?deckId=" + deckId + "&mode=study");
    }
}
