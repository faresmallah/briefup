// deck.js 

document.addEventListener("DOMContentLoaded", () => {
    
    // URL params
    const urlParams = new URLSearchParams(window.location.search);
    const deckId = urlParams.get("deckId");
    const mode = urlParams.get("mode");

    

    // ----------------------------------------------------
    // DOM Elements
    // ----------------------------------------------------
    const flashcardList = document.getElementById("flashcardList");
    const addCardBtn = document.getElementById("addCardBtn");
    const saveDeckBtn = document.getElementById("saveDeckBtn");
    const studyModeBtn = document.getElementById("studyModeBtn");

    const editMode = document.getElementById("editMode");
    const studyMode = document.getElementById("studyMode");

    // Study UI
    const flashcard = document.getElementById("flashcardStudy");
    const frontEl = flashcard.querySelector(".flashcard-front");
    const backEl = flashcard.querySelector(".flashcard-back");
    const exitBtn = document.getElementById("exitStudy");
    const knewBtn = document.getElementById("knewBtn");
    const forgotBtn = document.getElementById("forgotBtn");
    const progressText = document.getElementById("progressText");
    const cardCountEl = document.getElementById("cardCount");
    const studyProgressBar = document.getElementById("studyProgressBar");

    // Quiz UI
    const quizMode = document.getElementById("quizMode");
    const quizTitle = document.getElementById("quizTitle");
    const quizProgress = document.getElementById("quizProgress");
    const quizCard = document.getElementById("quizCard");
    const quizOptions = document.getElementById("quizOptions");
    const quizNextBtn = document.getElementById("quizNextBtn");
    const quizExitBtn = document.getElementById("quizExitBtn");

    // ----------------------------------------------------
    // STATE
    // ----------------------------------------------------
    let cards = [];
    let studyQueue = [];
    let masteredCards = [];
    let currentIndex = 0;
    let flipped = false;
    let cardMastery = new Map();

    // Quiz state
    let quizQuestions = [];
    let quizIndex = 0;
    let quizCorrectCount = 0;

    // FIX: Track study session at page level (won't reset when exiting/returning)
    let studySessionStarted = false;

    // Track forgotten cards for final quiz
    let forgottenCards = new Map();

    // ----------------------------------------------------
    // BACKEND COMMUNICATION - SIMPLIFIED
    // ----------------------------------------------------
    const updateStudyStats = async (action, additionalParams = {}) => {
        const formData = new URLSearchParams();
        formData.append('action', action);
        
        Object.entries(additionalParams).forEach(([key, value]) => {
            formData.append(key, value);
        });

        try {
            await fetch('studySession', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData.toString()
            });
        } catch (error) {
            console.error('Error updating stats:', error);
        }
    };

    // ----------------------------------------------------
    // UTIL FUNCTIONS
    // ----------------------------------------------------
    const escapeHtml = (s) => !s ? "" : s.replace(/&/g, "&amp;").replace(/</g, "&lt;");
    const shuffle = (arr) => arr.sort(() => Math.random() - 0.5);
    const updateCardCount = () => {
        if (cardCountEl) cardCountEl.textContent = cards.length;
    };

    // ----------------------------------------------------
    // CARD MANAGEMENT
    // ----------------------------------------------------
    const loadCardsFromServer = async () => {
        
        try {
            const res = await fetch(`listCards?deckId=${deckId}`);
            if (!res.ok) {
                console.error("Server response not OK:", res.status);
                throw new Error('Failed to load cards');
            }
            
            const data = await res.json();
        
            
            if (!Array.isArray(data)) {
                console.error("Expected array but got:", typeof data);
                cards = [];
            } else {
                cards = data.map(c => ({ 
                    q: c.question || "", 
                    a: c.answer || "" 
                }));
            }
            
            renderCardInputs();
            updateCardCount();

            // Auto-enter study mode if requested
            if (mode === "study") {
                if (cards.length > 0) {
                    enterStudyMode();
                } else {
                    alert("No cards available to study. Please add cards first.");
                }
            }
        } catch (error) {
            console.error('Error loading cards:', error);
            cards = [];
            renderCardInputs();
            updateCardCount();
        }
    };

    const renderCardInputs = () => {
        flashcardList.innerHTML = "";
        
        if (cards.length === 0) {
            flashcardList.innerHTML = `
                <div class="alert alert-info">
                    <i class="bi bi-info-circle"></i> No cards yet. Click "Add Card" to create your first flashcard.
                </div>
            `;
        } else {
            cards.forEach((card, index) => {
                const cardElement = buildCardInput(card.q, card.a);
                flashcardList.appendChild(cardElement);
            });
        }
        
        updateCardCount();
    };

    const buildCardInput = (q = "", a = "") => {
        const div = document.createElement("div");
        div.className = "card-input mb-3 p-3 border rounded bg-white";
        div.innerHTML = `
            <input class="form-control question mb-2" value="${escapeHtml(q)}" placeholder="Question">
            <textarea class="form-control answer" rows="2" placeholder="Answer">${escapeHtml(a)}</textarea>
            <button class="btn btn-sm btn-outline-danger mt-2 remove-card">
                <i class="bi bi-trash"></i> Remove
            </button>
        `;
        return div;
    };

    const collectCardsFromInputs = () => {
        const newCards = [];
        document.querySelectorAll(".card-input").forEach(div => {
            const q = div.querySelector(".question").value.trim();
            const a = div.querySelector(".answer").value.trim();
            newCards.push({ q: q || "", a: a || "" });
        });
        return newCards;
    };

    // ----------------------------------------------------
    // SAVE DECK
    // ----------------------------------------------------
    const saveDeck = async () => {
        const cardsToSave = collectCardsFromInputs();
        
        const form = new URLSearchParams();
        form.append("deckId", deckId);
        form.append("count", cardsToSave.length);

        cardsToSave.forEach((c, i) => {
            form.append("question" + i, c.q);
            form.append("answer" + i, c.a);
        });

        try {
            const response = await fetch("saveDeck", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: form.toString()
            });

            if (response.ok) {
                alert("Saved successfully!");
                await loadCardsFromServer();
            } else {
                const errorText = await response.text();
                throw new Error(`Save failed: ${errorText}`);
            }
        } catch (error) {
            console.error('Error saving deck:', error);
            alert('Error saving deck: ' + error.message);
        }
    };

    // ----------------------------------------------------
    // STUDY MODE - FIXED: No multiple session counting
    // ----------------------------------------------------
    const getMasteryText = (masteryLevel) => {
        switch(masteryLevel) {
            case 0: return "New";
            case 1: return "Learning";
            case 2: return "Reviewing"; 
            case 3: return "Mastered!";
            default: return "New";
        }
    };

    const getMasteryColor = (masteryLevel) => {
        switch(masteryLevel) {
            case 0: return "secondary";
            case 1: return "warning";
            case 2: return "info";
            case 3: return "success";
            default: return "secondary";
        }
    };

    const renderStudyCard = () => {
        if (studyQueue.length === 0) {
            frontEl.textContent = "🎉 All Done!";
            backEl.textContent = "You've completed all cards for now.";
            return;
        }
        
        const currentCard = studyQueue[currentIndex];
        const mastery = cardMastery.get(currentCard.q) || 0;
        
        frontEl.textContent = currentCard.q || "[No question]";
        backEl.textContent = currentCard.a || "[No answer]";
        
        // Update mastery indicator
        const masteryBadge = document.getElementById('masteryBadge') || createMasteryBadge();
        masteryBadge.textContent = getMasteryText(mastery);
        masteryBadge.className = `badge bg-${getMasteryColor(mastery)} position-absolute top-0 end-0 m-2`;
    };

    const createMasteryBadge = () => {
        const badge = document.createElement('div');
        badge.id = 'masteryBadge';
        badge.className = 'badge bg-secondary position-absolute top-0 end-0 m-2';
        flashcard.appendChild(badge);
        return badge;
    };

    const enterStudyMode = async () => {
      
        if (!cards || cards.length === 0) {
            alert("Add cards first before studying.");
            return false;
        }

        // Only start ONE study session per page load
        if (!studySessionStarted) {
            await updateStudyStats('session');
            studySessionStarted = true;
        }

        // Switch to study mode UI
        editMode.style.display = "none";
        studyMode.style.display = "block";
        quizMode.style.display = "none";

        // Initialize study state
        studyQueue = [...cards];
        masteredCards = [];
        cardMastery.clear();
        forgottenCards.clear();
        shuffle(studyQueue);

        currentIndex = 0;
        flipped = false;

        // Create mastery badge if it doesn't exist
        if (!document.getElementById('masteryBadge')) {
            createMasteryBadge();
        }

        renderStudyCard();
        updateStudyProgress();
        
        return true;
    };

    const updateStudyProgress = () => {
        const totalCards = cards.length;
        const masteredCount = masteredCards.length;
        const progressPercent = totalCards > 0 ? (masteredCount / totalCards) * 100 : 0;
        
        if (studyProgressBar) {
            studyProgressBar.style.width = `${progressPercent}%`;
            studyProgressBar.setAttribute('aria-valuenow', progressPercent);
        }
        
        if (studyQueue.length === 0) {
            progressText.innerHTML = `<span class="text-success">🎉 All cards mastered! ${masteredCount}/${totalCards}</span>`;
        } else {
            const currentMastery = cardMastery.get(studyQueue[currentIndex]?.q) || 0;
            progressText.innerHTML = `
                <strong>Progress:</strong> ${masteredCount}/${totalCards} mastered 
                | <span class="text-${getMasteryColor(currentMastery)}">${getMasteryText(currentMastery)}</span>
                | ${studyQueue.length} left to review
            `;
        }
    };

    const markForgot = async () => {
        if (studyQueue.length === 0) return;
        
        // Track card studied - NO session increment
        await updateStudyStats('card');

        const currentCard = studyQueue[currentIndex];
        
        // Track forgotten cards for final quiz (important!)
        forgottenCards.set(currentCard.q, (forgottenCards.get(currentCard.q) || 0) + 1);
        
        // Reset mastery if user forgets
        cardMastery.set(currentCard.q, 0);
        
        // Move to next card (this card will reappear later)
        currentIndex = (currentIndex + 1) % studyQueue.length;
        flipped = false;
        flashcard.classList.remove("flipped");

        renderStudyCard();
        updateStudyProgress();
    };

    const markKnewIt = async () => {
        if (studyQueue.length === 0) return;
        
        // Track card studied - NO session increment
        await updateStudyStats('card');

        const currentCard = studyQueue[currentIndex];
        const currentMastery = cardMastery.get(currentCard.q) || 0;
        const newMastery = currentMastery + 1;

        cardMastery.set(currentCard.q, newMastery);

        // If mastered (level 3), move to mastered pile
        if (newMastery >= 3) {
            masteredCards.push(studyQueue.splice(currentIndex, 1)[0]);
            
            // NO QUIZ TRIGGERS DURING STUDY - ONLY AT THE VERY END
            // Check if ALL cards are completed
            if (studyQueue.length === 0) {
                startFinalReviewQuiz();
                return;
            }
            
            // Stay on current index after removal
            if (currentIndex >= studyQueue.length) {
                currentIndex = 0;
            }
        } else {
            // Move to next card for continued practice
            currentIndex = (currentIndex + 1) % studyQueue.length;
        }

        flipped = false;
        flashcard.classList.remove("flipped");

        renderStudyCard();
        updateStudyProgress();
    };

    // Final review quiz ONLY after ALL cards are completed
    const startFinalReviewQuiz = () => {
        // If no cards were forgotten, use random cards for review
        if (forgottenCards.size === 0) {
            // Pick 5 random cards from the entire deck
            const randomCards = [...cards];
            shuffle(randomCards);
            const quizCards = randomCards.slice(0, Math.min(5, randomCards.length));
            
            if (quizCards.length >= 3) {
                const title = `📝 Final Review - Random Selection`;
                startQuiz(quizCards, title);
            } else {
                // Not enough cards for quiz, just show completion
                alert("🎉 Perfect! You mastered all cards! No review needed.");
                exitStudy();
            }
            return;
        }

        // Sort cards by most forgotten first
        const sortedByForgot = [...cards].sort((a, b) => {
            const countA = forgottenCards.get(a.q) || 0;
            const countB = forgottenCards.get(b.q) || 0;
            return countB - countA; // Most forgotten first
        });
        
        // Take top 5 most forgotten cards
        const quizCards = sortedByForgot.slice(0, Math.min(5, sortedByForgot.length));
        
        if (quizCards.length >= 3) {
            const title = `📝 Final Review - Focus on Weak Areas`;
            startQuiz(quizCards, title);
        } else {
            // Not enough forgotten cards, supplement with random ones
            const remainingNeeded = 5 - quizCards.length;
            const allCards = [...cards];
            shuffle(allCards);
            
            // Add random cards to reach 5 total
            const additionalCards = allCards
                .filter(card => !quizCards.includes(card))
                .slice(0, remainingNeeded);
            
            const finalQuizCards = [...quizCards, ...additionalCards];
            const title = `📝 Final Review - Mixed Selection`;
            startQuiz(finalQuizCards, title);
        }
    };

    const exitStudy = async () => {
        // Don't reset studySessionStarted - let user continue same session
        studyMode.style.display = "none";
        quizMode.style.display = "none";
        editMode.style.display = "block";
    };

    // ----------------------------------------------------
    // QUIZ MODE : Proper quiz flow
    // ----------------------------------------------------
    const createQuizQuestions = (list) => {
        if (list.length < 3) {
            return [];
        }
        return list.map(c => ({
            q: c.q,
            a: c.a,
            options: buildOptions(c.a),
            userAnswer: null,
            isCorrect: false
        }));
    };

    const buildOptions = (correct) => {
        const pool = cards.map(c => c.a).filter(a => a && a.trim() !== "" && a !== correct);
        shuffle(pool);
        const opts = [correct, ...pool.slice(0, Math.min(3, pool.length))];
        while (opts.length < 2) {
            opts.push("No answer available");
        }
        return shuffle(opts);
    };

    const startQuiz = (list, title) => {
        quizQuestions = createQuizQuestions(list);
        if (quizQuestions.length === 0) {
            alert("Not enough cards with answers for a quiz. Need at least 3 cards.");
            exitStudy();
            return;
        }

        quizIndex = 0;
        quizCorrectCount = 0;

        editMode.style.display = "none";
        studyMode.style.display = "none";
        quizMode.style.display = "block";

        quizTitle.textContent = title;
        quizNextBtn.style.display = 'none';

        renderQuizQuestion();
    };

    const renderQuizQuestion = () => {
        const qObj = quizQuestions[quizIndex];

        quizCard.innerHTML = `<h5 class="mb-3 text-dark">${escapeHtml(qObj.q || "[No question]")}</h5>`;
        quizOptions.innerHTML = "";

        qObj.options.forEach(opt => {
            const btn = document.createElement("button");
            btn.className = "btn btn-outline-primary w-100 mb-2 text-start p-3";
            btn.innerHTML = `<span class="option-text">${opt || "[No answer]"}</span>`;
            btn.onclick = () => handleAnswer(opt, btn);
            quizOptions.appendChild(btn);
        });

        quizProgress.textContent = `Question ${quizIndex + 1} of ${quizQuestions.length}`;
    };

    const handleAnswer = (selected, buttonElement) => {
        const correct = quizQuestions[quizIndex].a;
        const allButtons = quizOptions.querySelectorAll('button');
        
        allButtons.forEach(btn => btn.disabled = true);
        
        allButtons.forEach(btn => {
            const btnText = btn.querySelector('.option-text').textContent;
            if (btnText === correct) {
                btn.classList.remove('btn-outline-primary');
                btn.classList.add('btn-success');
            } else if (btnText === selected && selected !== correct) {
                btn.classList.remove('btn-outline-primary');
                btn.classList.add('btn-danger');
            }
        });

        quizQuestions[quizIndex].userAnswer = selected;
        quizQuestions[quizIndex].isCorrect = (selected === correct);
        
        if (selected === correct) {
            quizCorrectCount++;
        }

        quizNextBtn.style.display = 'block';
    };

    const submitQuizResults = async () => {
        const quizType = "final";
        
        await updateStudyStats('quiz', {
            correctAnswers: quizCorrectCount,
            totalQuestions: quizQuestions.length,
            quizType: quizType,
            deckId: deckId
        });

        // Refresh the parent window stats if we're in an iframe or notify home page
        if (window.opener) {
            // If opened from home page, refresh parent stats
            window.opener.loadProgressStats && window.opener.loadProgressStats();
        }
        
        // Try to refresh using localStorage event
        localStorage.setItem('refreshStats', Date.now());

        return quizCorrectCount;
    };

    // Quiz completion properly exits
    quizNextBtn.onclick = async () => {
        quizIndex++;
        if (quizIndex >= quizQuestions.length) {
            const correctCount = await submitQuizResults();
            
            const message = correctCount === quizQuestions.length 
                ? `🎉 Perfect! You scored ${correctCount}/${quizQuestions.length}!`
                : `📚 Review Complete! You scored ${correctCount}/${quizQuestions.length}.`;
            
            alert(message);
            
            // Redirect to home page to refresh stats
            window.location.href = 'home?refresh=true';
            return;
        }
        quizNextBtn.style.display = 'none';
        renderQuizQuestion();
    };

    // Quiz exit properly exits
    quizExitBtn.onclick = () => {
        exitStudy();
    };

    // ----------------------------------------------------
    // EVENT LISTENERS
    // ----------------------------------------------------
    addCardBtn.onclick = () => {
        flashcardList.appendChild(buildCardInput());
        const newCards = collectCardsFromInputs();
        cards = newCards;
        updateCardCount();
    };

    flashcardList.onclick = (e) => {
        if (e.target.classList.contains("remove-card")) {
            e.target.parentElement.remove();
            const newCards = collectCardsFromInputs();
            cards = newCards;
            updateCardCount();
        }
    };

    flashcardList.addEventListener('input', (e) => {
        if (e.target.classList.contains('question') || e.target.classList.contains('answer')) {
            const newCards = collectCardsFromInputs();
            cards = newCards;
            updateCardCount();
        }
    });

    saveDeckBtn.onclick = saveDeck;
    studyModeBtn.onclick = enterStudyMode;
    exitBtn.onclick = exitStudy;

    flashcard.onclick = () => {
        if (studyQueue.length === 0) return;
        flipped = !flipped;
        flashcard.classList.toggle("flipped");
    };

    knewBtn.onclick = markKnewIt;
    forgotBtn.onclick = markForgot;

    // ----------------------------------------------------
    // INITIALIZATION
    // ----------------------------------------------------
    loadCardsFromServer();
});