<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.briefup.models.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>BriefUp | Dashboard</title>

  <!-- Bootstrap -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" />
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"></script>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

  <!-- External CSS -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
</head>

<body>
     <!-- NAVBAR -->
  <nav class="navbar navbar-expand-lg bg-white shadow-sm">
    <div class="container py-2">
      <a class="navbar-brand d-flex align-items-center" href="home">
        <span class="fw-bold text-primary fs-4" style="font-family: 'Arial Rounded MT Bold', 'Arial', sans-serif;">BriefUp</span>
      </a>

      <div class="ms-auto d-flex align-items-center">
        <%
          User loggedUser = (User) session.getAttribute("user");
          String displayName = "Learner"; // default fallback
          
          if (loggedUser != null && loggedUser.getName() != null && !loggedUser.getName().trim().isEmpty()) {
              displayName = loggedUser.getName();
          }
        %>

        <span class="me-3 text-muted">Hi, <strong><%= displayName %></strong></span>

        <a href="logout" class="btn btn-outline-dark">Logout</a>
      </div>
    </div>
  </nav>

  <!-- MAIN LAYOUT -->
  <div class="container-fluid mt-4">
    <div class="row">

      <!-- SIMPLIFIED SIDEBAR -->
      <div class="col-md-3 col-lg-2 border-end bg-light">
        <h5 class="mt-3 mb-4">Menu</h5>
        <div class="list-group" id="sidebarMenu">
          <a href="#" class="list-group-item list-group-item-action active"
             onclick="showSection('mydecks', event)">My Decks</a>
          <a href="#" class="list-group-item list-group-item-action"
             onclick="showSection('progress', event)">My Progress</a>
          <a href="#" class="list-group-item list-group-item-action"
             onclick="showSection('leaderboard', event)">Leaderboard</a>
          <a href="#" class="list-group-item list-group-item-action"
             onclick="showSection('addcontent', event)">Add Content</a>
        </div>

        <!-- DECK QUICK FILTERS -->
        <div class="mt-4 px-2">
          <h6 class="text-muted mb-2">Quick Filters</h6>
          <div class="btn-group-vertical w-100" role="group">
            <button type="button" class="btn btn-outline-secondary btn-sm text-start" onclick="filterDecks('all')">
              <i class="bi bi-collection"></i> All Decks
            </button>
            <button type="button" class="btn btn-outline-secondary btn-sm text-start" onclick="filterDecks('recent')">
              <i class="bi bi-clock"></i> Recently Studied
            </button>
            <button type="button" class="btn btn-outline-secondary btn-sm text-start" onclick="filterDecks('large')">
              <i class="bi bi-stack"></i> Large Decks (10+ cards)
            </button>
            <button type="button" class="btn btn-outline-secondary btn-sm text-start" onclick="filterDecks('small')">
              <i class="bi bi-file-text"></i> Small Decks (< 5 cards)
            </button>
          </div>
        </div>

        <!-- STUDY STATS SUMMARY -->
        <div class="mt-4 px-2">
          <h6 class="text-muted mb-2">Study Summary</h6>
          <div class="small">
            <div class="d-flex justify-content-between">
              <span>Total Decks:</span>
              <span id="sidebarDeckCount" class="fw-bold">0</span>
            </div>
            <div class="d-flex justify-content-between">
              <span>Total Cards:</span>
              <span id="sidebarCardCount" class="fw-bold">0</span>
            </div>
            <div class="d-flex justify-content-between">
              <span>Ready to Study:</span>
              <span id="sidebarReadyCount" class="fw-bold">0</span>
            </div>
          </div>
        </div>
      </div>

      <!-- MAIN CONTENT -->
      <div class="col-md-9 col-lg-10">

        <!-- MY DECKS SECTION (Default) -->
        <div id="mydecks" class="content-section active p-4">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h3>My Decks</h3>
              <p class="text-muted mb-0">Manage and study your flashcard decks</p>
            </div>

            <!-- Search and Sort Controls -->
            <div class="d-flex gap-2 align-items-center">
              <div class="input-group" style="width: 250px;">
                <input type="text" id="deckSearch" class="form-control form-control-sm" placeholder="Search decks...">
                <button class="btn btn-outline-secondary btn-sm" type="button" onclick="searchDecks()">
                  <i class="bi bi-search"></i>
                </button>
              </div>
              
              <select id="sortDecks" class="form-select form-select-sm" style="width: auto;" onchange="loadDecks()">
                <option value="name">Sort by Name</option>
                <option value="recent">Sort by Recent</option>
                <option value="cards">Sort by Card Count</option>
              </select>

              <div class="btn-group">
                <button class="btn btn-success" data-bs-toggle="modal" data-bs-target="#createDeckModal">
                  <i class="bi bi-plus-circle"></i> Manual Deck
                </button>
                <button class="btn btn-outline-primary" data-bs-toggle="modal" data-bs-target="#aiDeckModal">
                  <i class="bi bi-magic"></i> From PDF
                </button>
              </div>
            </div>
          </div>

          <!-- Deck Statistics -->
          <div class="row mb-4">
            <div class="col-md-3">
              <div class="card bg-primary text-white">
                <div class="card-body py-3">
                  <h6 class="card-title mb-1">Total Decks</h6>
                  <h4 id="statTotalDecksMain" class="mb-0">0</h4>
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="card bg-success text-white">
                <div class="card-body py-3">
                  <h6 class="card-title mb-1">Total Cards</h6>
                  <h4 id="statTotalCardsMain" class="mb-0">0</h4>
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="card bg-warning text-white">
                <div class="card-body py-3">
                  <h6 class="card-title mb-1">Ready to Study</h6>
                  <h4 id="statReadyDecks" class="mb-0">0</h4>
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="card bg-info text-white">
                <div class="card-body py-3">
                  <h6 class="card-title mb-1">Mastered</h6>
                  <h4 id="statMasteredDecks" class="mb-0">0</h4>
                </div>
              </div>
            </div>
          </div>

          <!-- Deck Grid with Organization -->
          <div class="mb-3">
            <h5 id="deckSectionTitle" class="text-muted">All Decks</h5>
          </div>

          <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4" id="deckContainer"></div>

          <div id="emptyAlert" class="alert alert-info mt-3" role="alert">
            <i class="bi bi-info-circle"></i> Your decks are empty. Start by creating your first deck!
          </div>

          <!-- Recently Studied Section -->
          <div id="recentDecksSection" class="mt-5" style="display: none;">
            <h5 class="text-muted mb-3">Recently Studied</h5>
            <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4" id="recentDeckContainer"></div>
          </div>
        </div>

        
        <!-- PROGRESS SECTION -->
<div id="progress" class="content-section p-4">
  <h3 class="mb-4">My Progress</h3>
  <p class="text-muted">Track your learning journey and see how far you've come.</p>

  <div class="row g-3 mb-4">
    <div class="col-md-3">
      <div class="card h-100">
        <div class="card-body">
          <h6 class="text-muted mb-1">Total Decks</h6>
          <h2 id="statTotalDecks" class="mb-0">0</h2>
          <small class="text-muted">Number of decks you created</small>
        </div>
      </div>
    </div>

    <div class="col-md-3">
      <div class="card h-100">
        <div class="card-body">
          <h6 class="text-muted mb-1">Total Cards</h6>
          <h2 id="statTotalCards" class="mb-0">0</h2>
          <small class="text-muted">All cards across your decks</small>
        </div>
      </div>
    </div>

    <div class="col-md-3">
      <div class="card h-100">
        <div class="card-body">
          <h6 class="text-muted mb-1">Score</h6>
          <h2 id="statScore" class="mb-0">0</h2>
          <small class="text-muted">Earn points as you study</small>
        </div>
      </div>
    </div>

    <div class="col-md-3">
      <div class="card h-100">
        <div class="card-body">
          <h6 class="text-muted mb-1">Mastered Cards</h6>
          <h2 id="statMasteredCards" class="mb-0">0</h2>
          <small class="text-muted">Cards you've fully mastered</small>
        </div>
      </div>
    </div>
  </div>

  <div class="row g-3 mb-4">
    <div class="col-md-4">
      <div class="card h-100">
        <div class="card-body">
          <h6 class="text-muted mb-1">Cards Studied</h6>
          <h3 id="statCardsStudied" class="mb-0">0</h3>
          <small class="text-muted">Total cards reviewed in study mode</small>
        </div>
      </div>
    </div>

    <div class="col-md-4">
      <div class="card h-100">
        <div class="card-body">
          <h6 class="text-muted mb-1">Study Sessions</h6>
          <h3 id="statStudySessions" class="mb-0">0</h3>
          <small class="text-muted">How many sessions you started</small>
        </div>
      </div>
    </div>

    <div class="col-md-4">
      <div class="card h-100">
        <div class="card-body">
          <h6 class="text-muted mb-1">Streak</h6>
          <h3 id="statStreak" class="mb-0">0</h3>
          <small class="text-muted">Consecutive days of activity</small>
        </div>
      </div>
    </div>
  </div>

  <div>
    <h5 class="mb-2">Study Progress</h5>
    <div class="progress mb-1">
      <div id="statProgressBar" class="progress-bar" style="width: 0%"></div>
    </div>
    <small id="statProgressLabel" class="text-muted">
      Create your first deck to start tracking progress.
    </small>
  </div>
</div>
        <!-- LEADERBOARD SECTION -->
        <div id="leaderboard" class="content-section p-4">
          <h3 class="mb-3">Leaderboard</h3>
          <p class="text-muted">
            Compete with other BriefUp users. Score is based on decks, cards,
            and study activity.
          </p>

          <div class="table-responsive">
            <table class="table table-hover align-middle">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Student</th>
                  <th>Score</th>
                  <th>Decks</th>
                  <th>Cards</th>
                </tr>
              </thead>
              <tbody id="leaderboardBody">
                <!-- filled by JS -->
              </tbody>
            </table>
          </div>
        </div>

        <!-- ADD CONTENT SECTION -->
        <div id="addcontent" class="content-section p-4">
          <h3 class="mb-3">Add Content</h3>
          <p class="text-muted mb-4">
            Quickly create new decks manually or from AI-generated flashcards.
          </p>

          <div class="row g-3">
            <div class="col-md-6">
              <div class="card h-100">
                <div class="card-body d-flex flex-column">
                  <h5 class="card-title">Manual Deck</h5>
                  <p class="card-text flex-grow-1">
                    Create a deck from scratch. Perfect for custom notes,
                    formulas, or definitions you want full control over.
                  </p>
                  <button class="btn btn-success mt-2"
                          data-bs-toggle="modal"
                          data-bs-target="#createDeckModal">
                    <i class="bi bi-plus-circle"></i> Create Manual Deck
                  </button>
                </div>
              </div>
            </div>

            <div class="col-md-6">
              <div class="card h-100">
                <div class="card-body d-flex flex-column">
                  <h5 class="card-title">From PDF (AI)</h5>
                  <p class="card-text flex-grow-1">
                    Upload lecture slides or notes and let BriefUp generate
                    concise flashcards automatically using AI.
                  </p>
                  <button class="btn btn-outline-primary mt-2"
                          data-bs-toggle="modal"
                          data-bs-target="#aiDeckModal">
                    <i class="bi bi-magic"></i> Generate from PDF
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>

  <!-- CREATE DECK MODAL -->
  <div class="modal fade" id="createDeckModal" tabindex="-1" aria-labelledby="createDeckLabel" aria-hidden="true">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="createDeckLabel">Create New Deck</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <input type="text" id="deckName" class="form-control mb-3" placeholder="Deck name" />
          <textarea id="deckDesc" class="form-control" rows="3" placeholder="Short description"></textarea>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="button" class="btn btn-success" id="saveDeck">Create Deck</button>
        </div>
      </div>
    </div>
  </div>

  <!-- AI PDF DECK MODAL -->
  <div class="modal fade" id="aiDeckModal" tabindex="-1" aria-labelledby="aiDeckLabel" aria-hidden="true">
    <div class="modal-dialog">
      <form class="modal-content" action="uploadPdfDeck" method="post" enctype="multipart/form-data">
        <div class="modal-header">
          <h5 class="modal-title" id="aiDeckLabel">Create Deck from PDF (AI)</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>

        <div class="modal-body">
          <div class="mb-3">
            <label class="form-label">Deck name</label>
            <input type="text" name="deckName" class="form-control" required>
          </div>

          <div class="mb-3">
            <label class="form-label">Short description</label>
            <textarea name="deckDesc" class="form-control" rows="2"></textarea>
          </div>

          <div class="mb-3">
            <label class="form-label">PDF file</label>
            <input type="file" name="pdfFile" class="form-control" accept="application/pdf" required>
            <small class="text-muted">Upload lecture slides or notes PDF (max ~10MB).</small>
          </div>

          <div class="mb-2">
            <label class="form-label">Number of flashcards</label>
            <input type="number" name="numCards" class="form-control" min="5" max="40" value="20">
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary">Generate</button>
        </div>
      </form>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/js/home.js"></script>
</body>
</html>