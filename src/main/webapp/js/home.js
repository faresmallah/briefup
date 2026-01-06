// home.js - FINAL FIXED VERSION

document.addEventListener("DOMContentLoaded", () => {

    // State
    let allDecks = [];
    let currentFilter = 'all';
    let currentSearch = '';
    let currentSort = 'name';

    // DOM Elements
    const deckContainer = document.getElementById("deckContainer");
    const emptyAlert = document.getElementById("emptyAlert");
    const recentDecksSection = document.getElementById("recentDecksSection");
    const recentDeckContainer = document.getElementById("recentDeckContainer");
    const deckSearch = document.getElementById("deckSearch");
    const sortDecks = document.getElementById("sortDecks");

    // Stats elements
    const sidebarDeckCount = document.getElementById("sidebarDeckCount");
    const sidebarCardCount = document.getElementById("sidebarCardCount");
    const sidebarReadyCount = document.getElementById("sidebarReadyCount");
    const statTotalDecksMain = document.getElementById("statTotalDecksMain");
    const statTotalCardsMain = document.getElementById("statTotalCardsMain");
    const statReadyDecks = document.getElementById("statReadyDecks");
    const statMasteredDecks = document.getElementById("statMasteredDecks");
    const deckSectionTitle = document.getElementById("deckSectionTitle");

    // ----------------------------------------------------
    // DECK MANAGEMENT
    // ----------------------------------------------------
    const loadDecks = async () => {
        try {
            const res = await fetch('listDecks');
            if (!res.ok) throw new Error('Failed to load decks');
            
            const decks = await res.json();
      
            
            allDecks = Array.isArray(decks) ? decks : [];
            applyFiltersAndRender();
            updateSidebarStats();
            
        } catch (error) {
            allDecks = [];
            applyFiltersAndRender();
            updateSidebarStats();
        }
    };

    const applyFiltersAndRender = () => {
        let filteredDecks = [...allDecks];
        
        // Apply search filter
        if (currentSearch) {
            filteredDecks = filteredDecks.filter(deck => 
                deck.name.toLowerCase().includes(currentSearch.toLowerCase()) ||
                (deck.description && deck.description.toLowerCase().includes(currentSearch.toLowerCase()))
            );
        }
        
        // Apply category filter
        filteredDecks = applyDeckFilter(filteredDecks, currentFilter);
        
        // Apply sorting
        filteredDecks = sortDecksList(filteredDecks, currentSort);
        
        renderDecks(filteredDecks);
        updateMainStats(filteredDecks);
    };

    const applyDeckFilter = (decks, filter) => {
        switch(filter) {
            case 'recent':
                return decks.slice(0, Math.min(decks.length, 5));
            case 'large':
                return decks.filter(deck => deck.cardCount >= 10);
            case 'small':
                return decks.filter(deck => deck.cardCount < 5);
            case 'all':
            default:
                return decks;
        }
    };

    const sortDecksList = (decks, sortBy) => {
        switch(sortBy) {
            case 'recent':
                return [...decks].sort((a, b) => b.id - a.id);
            case 'cards':
                return [...decks].sort((a, b) => b.cardCount - a.cardCount);
            case 'name':
            default:
                return [...decks].sort((a, b) => a.name.localeCompare(b.name));
        }
    };

    const renderDecks = (decks) => {
        deckContainer.innerHTML = "";
        
        if (decks.length === 0) {
            emptyAlert.style.display = "block";
            recentDecksSection.style.display = "none";
        } else {
            emptyAlert.style.display = "none";
            
            decks.forEach(deck => {
                const deckElement = buildDeckCard(deck);
                deckContainer.appendChild(deckElement);
            });
            
            if (allDecks.length > 3) {
                showRecentDecks();
            }
        }
    };

    const buildDeckCard = (deck) => {
        const col = document.createElement("div");
        col.className = "col";
        
        const cardClass = deck.cardCount === 0 ? "border-warning" : "border-secondary";
        const badgeClass = deck.cardCount >= 10 ? "bg-success" : 
                          deck.cardCount >= 5 ? "bg-primary" : "bg-secondary";
        
        col.innerHTML = `
            <div class="card h-100 ${cardClass} deck-card">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <h5 class="card-title">${escapeHtml(deck.name)}</h5>
                        <span class="badge ${badgeClass}">${deck.cardCount} cards</span>
                    </div>
                    <p class="card-text text-muted small">${deck.description || "No description"}</p>
                </div>
                <div class="card-footer bg-transparent">
                    <div class="btn-group w-100">
                        <button class="btn btn-outline-primary btn-sm" onclick="studyDeck(${deck.id})">
                            <i class="bi bi-play-fill"></i> Study
                        </button>
                        <button class="btn btn-outline-secondary btn-sm" onclick="editDeck(${deck.id})">
                            <i class="bi bi-pencil"></i> Edit
                        </button>
                        <button class="btn btn-outline-danger btn-sm" onclick="deleteDeck(${deck.id})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
        
        return col;
    };

    const showRecentDecks = () => {
        const recentDecks = allDecks.slice(0, Math.min(allDecks.length, 3));
        recentDeckContainer.innerHTML = "";
        
        recentDecks.forEach(deck => {
            const deckElement = buildRecentDeckCard(deck);
            recentDeckContainer.appendChild(deckElement);
        });
        
        recentDecksSection.style.display = "block";
    };

    const buildRecentDeckCard = (deck) => {
        const col = document.createElement("div");
        col.className = "col";
        
        col.innerHTML = `
            <div class="card h-100 border-light bg-light">
                <div class="card-body">
                    <h6 class="card-title">${escapeHtml(deck.name)}</h6>
                    <p class="card-text text-muted small mb-2">${deck.description || "No description"}</p>
                    <div class="d-flex justify-content-between align-items-center">
                        <small class="text-muted">${deck.cardCount} cards</small>
                        <button class="btn btn-primary btn-sm" onclick="studyDeck(${deck.id})">
                            <i class="bi bi-arrow-repeat"></i> Resume
                        </button>
                    </div>
                </div>
            </div>
        `;
        
        return col;
    };

    // ----------------------------------------------------
    // FILTERS AND SEARCH
    // ----------------------------------------------------
    window.filterDecks = (filterType) => {
        currentFilter = filterType;
        applyFiltersAndRender();
        updateSectionTitle(filterType);
    };

    window.searchDecks = () => {
        currentSearch = deckSearch.value.trim();
        applyFiltersAndRender();
    };

    const updateSectionTitle = (filterType) => {
        const titles = {
            'all': 'All Decks',
            'recent': 'Recently Created',
            'large': 'Large Decks (10+ cards)',
            'small': 'Small Decks (< 5 cards)'
        };
        deckSectionTitle.textContent = titles[filterType] || 'All Decks';
    };

    // ----------------------------------------------------
    // STATS UPDATES
    // ----------------------------------------------------
    const updateSidebarStats = () => {
        const totalDecks = allDecks.length;
        const totalCards = allDecks.reduce((sum, deck) => sum + (deck.cardCount || 0), 0);
        const readyDecks = allDecks.filter(deck => deck.cardCount > 0).length;
        
        if (sidebarDeckCount) sidebarDeckCount.textContent = totalDecks;
        if (sidebarCardCount) sidebarCardCount.textContent = totalCards;
        if (sidebarReadyCount) sidebarReadyCount.textContent = readyDecks;
    };

    const updateMainStats = (filteredDecks) => {
        const totalDecks = allDecks.length;
        const totalCards = allDecks.reduce((sum, deck) => sum + (deck.cardCount || 0), 0);
        const readyDecks = allDecks.filter(deck => deck.cardCount > 0).length;
        
        //  We don't calculate mastered decks here - it should come from userStats
        // This is just for the deck statistics display
        const masteredDecks = allDecks.filter(deck => deck.cardCount >= 10).length;
        
        if (statTotalDecksMain) statTotalDecksMain.textContent = totalDecks;
        if (statTotalCardsMain) statTotalCardsMain.textContent = totalCards;
        if (statReadyDecks) statReadyDecks.textContent = readyDecks;
        if (statMasteredDecks) statMasteredDecks.textContent = masteredDecks;
    };

    // ----------------------------------------------------
    // DECK ACTIONS - DELETE & CREATE
    // ----------------------------------------------------
    window.studyDeck = (deckId) => {
        window.location.href = `deck.jsp?deckId=${deckId}&mode=study`;
    };

    window.editDeck = (deckId) => {
        window.location.href = `deck.jsp?deckId=${deckId}`;
    };

    window.deleteDeck = async (deckId) => {
        if (!confirm("Are you sure you want to delete this deck? This action cannot be undone.")) {
            return;
        }
       
        try {
            const response = await fetch('deleteDeckById', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `deckId=${deckId}`
            });
            
            if (response.ok) {
                const result = await response.text();
                if (result === "success") {
                    alert("Deck deleted successfully!");
                    loadDecks(); // Reload to show updated list
                } else {
                    throw new Error('Delete failed: ' + result);
                }
            } else {
                const errorText = await response.text();
                throw new Error(`Delete failed: ${errorText}`);
            }
        } catch (error) {
            console.error('Error deleting deck:', error);
            alert('Error deleting deck: ' + error.message);
        }
    };

    // ----------------------------------------------------
    // SECTION NAVIGATION 
    // ----------------------------------------------------
    window.showSection = (sectionId, event) => {
        if (event) event.preventDefault();
     
        
        // Update sidebar active state
        document.querySelectorAll('#sidebarMenu a').forEach(a => {
            a.classList.remove('active');
        });
        
        // Activate the clicked menu item
        if (event && event.target) {
            event.target.classList.add('active');
        }
        
        // Show selected section, hide others
        document.querySelectorAll('.content-section').forEach(section => {
            section.classList.remove('active');
        });
        
        const targetSection = document.getElementById(sectionId);
        if (targetSection) {
            targetSection.classList.add('active');
        } else {
            console.log('Section not found:', sectionId);
        }
        
        // Load section-specific data
        if (sectionId === 'progress') {
            loadProgressStats();
        } else if (sectionId === 'leaderboard') {
            loadLeaderboard();
        } else if (sectionId === 'mydecks') {
            loadDecks();
        } else if (sectionId === 'addcontent') {
            // Ensure modals are available
            initializeModals();
        }
    };

    // Initialize modal functionality
    function initializeModals() {
        console.log('🔧 Initializing modals...');
        // Bootstrap should handle this, but we can force initialization
        const modals = ['createDeckModal', 'aiDeckModal'];
        modals.forEach(modalId => {
            const modalElement = document.getElementById(modalId);
            if (modalElement) {
            } else {
                console.log('Modal not found:', modalId);
            }
        });
    }

    // ----------------------------------------------------
    // PROGRESS STATS 
    // ----------------------------------------------------
    window.loadProgressStats = async () => {
        
        try {
            const res = await fetch('userStats');            
            if (!res.ok) {
                throw new Error(`HTTP ${res.status}`);
            }
            
            const stats = await res.json();
            
            // CRITICAL: Check if masteredCards exists in response
            if (typeof stats.masteredCards === 'undefined') {
            } else {
                console.log("masteredCards found:", stats.masteredCards);
            }
            
            // Update ALL stat elements
            const updateElement = (id, value) => {
                const element = document.getElementById(id);
                if (element) {
                    element.textContent = value;
                } else {
                    console.log(`Element ${id} not found in DOM`);
                }
            };
            
            updateElement('statTotalDecks', stats.totalDecks || 0);
            updateElement('statTotalCards', stats.totalCards || 0);
            updateElement('statScore', stats.score || 0);
            updateElement('statCardsStudied', stats.cardsStudied || 0);
            updateElement('statStudySessions', stats.studySessions || 0);
            updateElement('statStreak', stats.streak || 0);
            updateElement('statMasteredCards', stats.masteredCards || 0); // This is the key one
            
            // Progress bar
            const progressBar = document.getElementById('statProgressBar');
            const progressLabel = document.getElementById('statProgressLabel');
            
            if (progressBar && progressLabel) {
                let progressPercent = 0;
                if (stats.totalCards > 0) {
                    progressPercent = Math.min(100, Math.round((stats.cardsStudied / stats.totalCards) * 100));
                }
                
                progressBar.style.width = `${progressPercent}%`;
                progressBar.setAttribute('aria-valuenow', progressPercent);
                progressLabel.textContent = `Overall progress: ${progressPercent}% - ${stats.cardsStudied} studied, ${stats.masteredCards || 0} mastered out of ${stats.totalCards} total`;
             
            }
            
            
        } catch (error) {
            console.error('Error loading stats:', error);
            
            // Set all to 0 on error
            const elements = [
                'statTotalDecks', 'statTotalCards', 'statScore', 
                'statCardsStudied', 'statStudySessions', 'statStreak', 'statMasteredCards'
            ];
            
            elements.forEach(id => {
                const element = document.getElementById(id);
                if (element) element.textContent = '0';
            });
        }
    };

    // ----------------------------------------------------
    // LEADERBOARD
    // ----------------------------------------------------
    const loadLeaderboard = async () => {
        try {
            const res = await fetch('leaderboard');
            if (!res.ok) throw new Error('Failed to load leaderboard');
            
            const leaderboard = await res.json();
            
            const tbody = document.getElementById('leaderboardBody');
            
            if (!Array.isArray(leaderboard) || leaderboard.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" class="text-center text-muted py-4">
                            <i class="bi bi-trophy display-4"></i>
                            <p class="mt-2">No leaderboard data available yet</p>
                            <small>Be the first to study and earn points!</small>
                        </td>
                    </tr>
                `;
                return;
            }
            
            tbody.innerHTML = leaderboard.map((user, index) => `
                <tr class="${index < 3 ? 'table-active' : ''}">
                    <td>
                        ${index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : index + 1}
                    </td>
                    <td>
                        <strong>${escapeHtml(user.userName || 'Anonymous')}</strong>
                        ${index < 3 ? '<span class="badge bg-warning ms-1">Top</span>' : ''}
                    </td>
                    <td><strong class="text-primary">${user.score || 0}</strong></td>
                    <td>${user.totalDecks || 0}</td>
                    <td>${user.totalCards || 0}</td>
                </tr>
            `).join('');
            
        } catch (error) {
            console.error('Error loading leaderboard:', error);
            const tbody = document.getElementById('leaderboardBody');
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-danger py-4">
                        <i class="bi bi-exclamation-triangle"></i>
                        <p class="mt-2">Error loading leaderboard</p>
                        <small>Please try again later</small>
                    </td>
                </tr>
            `;
        }
    };

    // ----------------------------------------------------
    // DECK CREATION - FIXED AUTO-REDIRECT
    // ----------------------------------------------------
    const initializeDeckCreation = () => {
        const saveDeckBtn = document.getElementById('saveDeck');
        if (saveDeckBtn) {
            saveDeckBtn.onclick = async () => {
                const name = document.getElementById("deckName").value.trim();
                const desc = document.getElementById("deckDesc").value.trim();
                
                if (!name) {
                    alert("Please enter a deck name");
                    return;
                }
                
                try {
                    const form = new URLSearchParams();
                    form.append("name", name);
                    form.append("description", desc);
                    
                    const response = await fetch("createDeck", {
                        method: "POST",
                        headers: { "Content-Type": "application/x-www-form-urlencoded" },
                        body: form.toString()
                    });
                    
                    if (response.ok) {
                        // Get the deck ID from the response
                        const deckId = await response.text();
                        
                        // Close modal and reset form
                        const modal = bootstrap.Modal.getInstance(document.getElementById('createDeckModal'));
                        if (modal) modal.hide();
                        document.getElementById("deckName").value = "";
                        document.getElementById("deckDesc").value = "";
                        
                        // AUTO-REDIRECT to the created deck
                        if (deckId && deckId !== "-1") {
                            window.location.href = `deck.jsp?deckId=${deckId}`;
                        } else {
                            // Fallback
                            loadDecks();
                            alert("Deck created successfully!");
                        }
                        
                    } else {
                        throw new Error('Create deck failed');
                    }
                } catch (error) {
                    console.error('Error creating deck:', error);
                    alert('Error creating deck: ' + error.message);
                }
            };
        }
    };

    // ----------------------------------------------------
    // UTILITY FUNCTIONS
    // ----------------------------------------------------
    const escapeHtml = (s) => !s ? "" : s.replace(/&/g, "&amp;").replace(/</g, "&lt;");

    // ----------------------------------------------------
    // INITIALIZATION
    // ----------------------------------------------------
    loadDecks();
    initializeDeckCreation();

    // FIX: Force load progress stats immediately and check if mastered cards display
    setTimeout(() => {
        loadProgressStats();
    }, 1000);

    // Set up search input listener
    if (deckSearch) {
        deckSearch.addEventListener('input', searchDecks);
    }

    // Set up sort listener
    if (sortDecks) {
        sortDecks.addEventListener('change', (e) => {
            currentSort = e.target.value;
            applyFiltersAndRender();
        });
    }

    // Auto-load progress stats if progress section is active on page load
    if (document.getElementById('progress').classList.contains('active')) {
        loadProgressStats();
    }
    
    // Listen for stats refresh events
    window.addEventListener('storage', (e) => {
        if (e.key === 'refreshStats') {
            loadProgressStats();
        }
    });

    // Also check for refresh on page focus
    window.addEventListener('focus', () => {
        loadProgressStats();
    });
});