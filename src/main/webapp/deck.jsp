<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.briefup.dao.DeckDAO" %>
<%@ page import="com.briefup.models.Deck" %>

<%
    String deckIdParam = request.getParameter("deckId");
    int deckId = -1;

    try {
        deckId = Integer.parseInt(deckIdParam);
    } catch (Exception e) {
        response.sendRedirect("home.jsp");
        return;
    }

    DeckDAO deckDAO = new DeckDAO();
    Deck currentDeck = deckDAO.getDeck(deckId);

    if (currentDeck == null) {
        response.sendRedirect("home.jsp");
        return;
    }

    String mode = request.getParameter("mode"); // "study" or null
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= currentDeck.getName() %> - BriefUp</title>

    <!-- Bootstrap first -->
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <!-- Custom flashcard styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/deck.css">
</head>

<body class="bg-light">

<div class="container py-4">

    <!-- Improved Navigation -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <a href="home.jsp" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left"></i> Back to Decks
        </a>
        <h3 class="mb-0 text-center flex-grow-1"><%= currentDeck.getName() %></h3>
        <div class="text-muted text-end" style="max-width: 200px;">
            <small><%= currentDeck.getDescription() %></small>
        </div>
    </div>

    <!-- EDIT MODE -->
    <div id="editMode">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <button id="addCardBtn" class="btn btn-success">
                <i class="bi bi-plus-circle"></i> Add Card
            </button>
            <div>
                <button id="studyModeBtn" class="btn btn-warning me-2">
                    <i class="bi bi-play-circle"></i> Start Study Mode
                </button>
                <button id="saveDeckBtn" class="btn btn-primary">
                    <i class="bi bi-check-circle"></i> Save Deck
                </button>
            </div>
        </div>

        <div id="flashcardList"></div>
        
        <div class="card-count-display mt-3 p-3 bg-white rounded border">
            <strong>Total Cards:</strong> <span id="cardCount" class="badge bg-primary">0</span>
            <small class="text-muted ms-2">Save your changes after adding/editing cards</small>
        </div>
    </div>

    <!-- STUDY MODE -->
    <div id="studyMode" style="display:none; text-align:center;">
        <!-- Progress Bar -->
        <div class="progress mb-4" style="height: 8px;">
            <div id="studyProgressBar" class="progress-bar" role="progressbar" style="width: 0%"></div>
        </div>

        <!-- Flashcard -->
        <div class="flashcard" id="flashcardStudy">
            <div class="flashcard-inner">
                <div class="flashcard-front"></div>
                <div class="flashcard-back"></div>
            </div>
        </div>

        <!-- Progress -->
        <p id="progressText" class="mt-2 text-muted small"></p>

        <!-- Active Recall buttons -->
        <div id="recallButtons" class="d-flex justify-content-center gap-3 mt-3">
            <button id="forgotBtn" class="btn btn-outline-danger px-4">
                <i class="bi bi-x-circle"></i> I Forgot
            </button>
            <button id="knewBtn" class="btn btn-success px-4">
                <i class="bi bi-check-circle"></i> I Knew It
            </button>
        </div>

        <!-- Exit -->
        <button id="exitStudy" class="btn btn-outline-secondary mt-3">
            <i class="bi bi-arrow-left"></i> Exit Study
        </button>
    </div>

    <!-- QUIZ MODE -->
    <div id="quizMode" style="display:none;">
        <div class="text-center mb-4">
            <h3 id="quizTitle" class="mb-2 text-primary"></h3>
            <p id="quizProgress" class="text-muted small mb-3"></p>
        </div>

        <div class="row justify-content-center">
            <div class="col-md-8 col-lg-6">
                <!-- Quiz Card -->
                <div id="quizCard" class="card p-4 shadow-sm mb-4 bg-white border-0">
                    <!-- Quiz question will be inserted here by JavaScript -->
                </div>

                <!-- Quiz Options -->
                <div id="quizOptions" class="mb-4">
                    <!-- Quiz options will be inserted here by JavaScript -->
                </div>

                <!-- Quiz Controls -->
                <div class="text-center">
                    <button id="quizNextBtn" class="btn btn-primary px-4 me-2" style="display:none;">
                        <i class="bi bi-arrow-right"></i> Next Question
                    </button>
                    <button id="quizExitBtn" class="btn btn-outline-secondary px-4">
                        <i class="bi bi-x-circle"></i> Exit Quiz
                    </button>
                </div>
            </div>
        </div>
    </div>
    
</div>

<script>
    const deckId = "<%= deckId %>";
    const studyModeRequested = "<%= mode != null ? mode : "" %>";
    const deckName = "<%= currentDeck.getName() %>";
</script>

<script src="${pageContext.request.contextPath}/js/deck.js"></script>

</body>
</html>