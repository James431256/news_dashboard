/**
 * News Intelligence Dashboard - Frontend Logic
 */

let currentArticles = [];
let activeSentimentFilter = 'ALL';
let savedBookmarks = [];
let recentSearches = [];

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    loadBookmarksFromStorage();
    loadRecentSearches();

    // Default search so the dashboard is vibrant on initial load
    document.getElementById('query-input').value = 'Technology';
    executeSearch('Technology', 'all');
});

/**
 * Handle form search submission
 */
function handleSearch(event) {
    if (event) event.preventDefault();
    const query = document.getElementById('query-input').value.trim();
    const category = document.getElementById('category-select').value;

    if (!query) {
        showError('Please enter a search topic or keyword.');
        return;
    }

    // Switch to feed tab if on bookmarks tab
    switchView('feed');
    executeSearch(query, category);
}

/**
 * Execute search via Spring Boot REST backend
 */
async function executeSearch(query, category) {
    showLoading();

    try {
        const url = `/api/news/search?query=${encodeURIComponent(query)}&category=${encodeURIComponent(category)}`;
        const response = await fetch(url);

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `Server responded with status ${response.status}`);
        }

        const data = await response.json();
        currentArticles = data.articles || [];

        // Save to recent searches
        addRecentSearch(query);

        // Update Intelligence stats
        updateIntelligenceOverview(data);

        // Render articles
        if (currentArticles.length === 0) {
            showEmpty();
        } else {
            activeSentimentFilter = 'ALL';
            updateFilterButtons();
            renderArticles(currentArticles, 'articles-container');
            showResults();
        }

    } catch (error) {
        console.error('Fetch error:', error);
        showError(error.message || 'Unable to fetch news articles. Please verify your connection.');
    }
}

/**
 * Update executive intelligence overview stats
 */
function updateIntelligenceOverview(data) {
    const overviewSection = document.getElementById('intel-overview');
    overviewSection.style.display = 'block';

    const breakdown = data.sentimentBreakdown || { POSITIVE: 0, NEUTRAL: 0, NEGATIVE: 0 };
    document.getElementById('stat-positive').textContent = breakdown.POSITIVE || 0;
    document.getElementById('stat-neutral').textContent = breakdown.NEUTRAL || 0;
    document.getElementById('stat-negative').textContent = breakdown.NEGATIVE || 0;

    const providersList = (data.providers || []).join(', ');
    document.getElementById('results-meta').textContent = 
        `Found ${data.totalResults || 0} articles across providers (${providersList})`;
}

/**
 * Render article cards into target container
 */
function renderArticles(articles, containerId) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';

    if (!articles || articles.length === 0) {
        container.innerHTML = `
            <div class="state-container" style="grid-column: 1 / -1;">
                <i class="fa-regular fa-folder-open state-icon"></i>
                <h4>No matching articles found in this filter view.</h4>
            </div>`;
        return;
    }

    articles.forEach(article => {
        const isBookmarked = savedBookmarks.some(b => b.url === article.url);

        // Clean date format
        let formattedDate = 'Recently';
        if (article.publishedAt) {
            try {
                formattedDate = new Date(article.publishedAt).toLocaleDateString('en-GB', {
                    day: 'numeric', month: 'short', year: 'numeric'
                });
            } catch (e) {
                formattedDate = article.publishedAt;
            }
        }

        const sentimentClass = (article.sentiment || 'NEUTRAL').toLowerCase();
        const sentimentIcon = sentimentClass === 'positive' ? 'fa-arrow-trend-up' :
                              sentimentClass === 'negative' ? 'fa-arrow-trend-down' : 'fa-minus';

        // Image or clean placeholder
        const imageHtml = article.imageUrl ?
            `<img src="${escapeHtml(article.imageUrl)}" alt="${escapeHtml(article.title)}" class="article-image" onerror="this.outerHTML='<div class=\\'article-placeholder-img\\'><i class=\\'fa-regular fa-newspaper\\'></i></div>'">` :
            `<div class="article-placeholder-img"><i class="fa-regular fa-newspaper"></i></div>`;

        const card = document.createElement('article');
        card.className = 'article-card';
        card.innerHTML = `
            ${imageHtml}
            <div class="article-body">
                <div class="card-top">
                    <span class="source-badge">${escapeHtml(article.source || 'News')}</span>
                    <span class="sentiment-badge ${sentimentClass}">
                        <i class="fa-solid ${sentimentIcon}"></i> ${escapeHtml(article.sentiment || 'NEUTRAL')}
                    </span>
                </div>
                <h3 class="article-title">
                    <a href="${escapeHtml(article.url)}" target="_blank" rel="noopener noreferrer">
                        ${escapeHtml(article.title)}
                    </a>
                </h3>
                <p class="article-description">${escapeHtml(article.description || '')}</p>
                <div class="card-footer">
                    <div class="article-meta">
                        <span><i class="fa-regular fa-user"></i> ${escapeHtml(article.author || 'Staff')}</span>
                        <span><i class="fa-regular fa-calendar"></i> ${formattedDate}</span>
                    </div>
                    <button class="bookmark-btn ${isBookmarked ? 'saved' : ''}" 
                            title="${isBookmarked ? 'Remove Bookmark' : 'Bookmark Article'}"
                            onclick="toggleBookmark('${escapeJson(article)}')">
                        <i class="${isBookmarked ? 'fa-solid' : 'fa-regular'} fa-bookmark"></i>
                    </button>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}

/**
 * Filter current articles by sentiment in-place
 */
function filterBySentiment(sentiment) {
    activeSentimentFilter = sentiment;
    updateFilterButtons();

    if (sentiment === 'ALL') {
        renderArticles(currentArticles, 'articles-container');
    } else {
        const filtered = currentArticles.filter(a => a.sentiment === sentiment);
        renderArticles(filtered, 'articles-container');
    }
}

function updateFilterButtons() {
    const buttons = document.querySelectorAll('.filter-pill');
    buttons.forEach(btn => {
        if (btn.textContent.toUpperCase().includes(activeSentimentFilter)) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}

/**
 * Toggle bookmarking an article
 */
function toggleBookmark(articleJsonString) {
    const article = JSON.parse(articleJsonString);
    const existingIndex = savedBookmarks.findIndex(b => b.url === article.url);

    if (existingIndex > -1) {
        savedBookmarks.splice(existingIndex, 1);
    } else {
        savedBookmarks.push(article);
    }

    saveBookmarksToStorage();
    updateBookmarkCounter();

    // Re-render whichever view is active
    const isBookmarkView = document.getElementById('bookmarks-view').style.display !== 'none';
    if (isBookmarkView) {
        renderArticles(savedBookmarks, 'bookmarks-view');
    } else {
        filterBySentiment(activeSentimentFilter);
    }
}

function saveBookmarksToStorage() {
    localStorage.setItem('pulse_news_bookmarks', JSON.stringify(savedBookmarks));
}

function loadBookmarksFromStorage() {
    const stored = localStorage.getItem('pulse_news_bookmarks');
    if (stored) {
        try {
            savedBookmarks = JSON.parse(stored);
        } catch (e) {
            savedBookmarks = [];
        }
    }
    updateBookmarkCounter();
}

function updateBookmarkCounter() {
    document.getElementById('bookmark-count').textContent = savedBookmarks.length;
}

/**
 * Recent Searches Management
 */
function addRecentSearch(query) {
    if (!query) return;
    recentSearches = recentSearches.filter(q => q.toLowerCase() !== query.toLowerCase());
    recentSearches.unshift(query);
    if (recentSearches.length > 6) recentSearches.pop();

    localStorage.setItem('pulse_recent_searches', JSON.stringify(recentSearches));
    renderRecentPills();
}

function loadRecentSearches() {
    const stored = localStorage.getItem('pulse_recent_searches');
    if (stored) {
        try {
            recentSearches = JSON.parse(stored);
        } catch (e) {
            recentSearches = [];
        }
    }
    renderRecentPills();
}

function renderRecentPills() {
    const container = document.getElementById('recent-pills');
    container.innerHTML = '';

    if (recentSearches.length === 0) {
        container.innerHTML = '<span style="font-size: 0.8rem; color: #94a3b8;">No recent searches</span>';
        return;
    }

    recentSearches.forEach(term => {
        const pill = document.createElement('button');
        pill.className = 'pill-tag';
        pill.textContent = term;
        pill.onclick = () => {
            document.getElementById('query-input').value = term;
            executeSearch(term, document.getElementById('category-select').value);
        };
        container.appendChild(pill);
    });
}

/**
 * View Switcher: Live Feed vs Saved Articles
 */
function switchView(view) {
    const tabFeed = document.getElementById('tab-feed');
    const tabBookmarks = document.getElementById('tab-bookmarks');
    const feedContainer = document.getElementById('articles-container');
    const bookmarksContainer = document.getElementById('bookmarks-view');
    const intelOverview = document.getElementById('intel-overview');

    if (view === 'bookmarks') {
        tabBookmarks.classList.add('active');
        tabFeed.classList.remove('active');
        feedContainer.style.display = 'none';
        bookmarksContainer.style.display = 'grid';
        intelOverview.style.display = 'none';
        hideStates();

        if (savedBookmarks.length === 0) {
            bookmarksContainer.innerHTML = `
                <div class="state-container" style="grid-column: 1 / -1;">
                    <i class="fa-regular fa-bookmark state-icon"></i>
                    <h4>No Bookmarked Articles Yet</h4>
                    <p>Click the bookmark icon on any news card to save it for quick reference.</p>
                </div>`;
        } else {
            renderArticles(savedBookmarks, 'bookmarks-view');
        }

    } else {
        tabFeed.classList.add('active');
        tabBookmarks.classList.remove('active');
        feedContainer.style.display = 'grid';
        bookmarksContainer.style.display = 'none';
        if (currentArticles.length > 0) {
            intelOverview.style.display = 'block';
        }
    }
}

/**
 * UI State Helpers
 */
function showLoading() {
    hideStates();
    document.getElementById('loading-state').style.display = 'block';
    document.getElementById('articles-container').style.display = 'none';
}

function showResults() {
    hideStates();
    document.getElementById('articles-container').style.display = 'grid';
}

function showEmpty() {
    hideStates();
    document.getElementById('empty-state').style.display = 'block';
}

function showError(msg) {
    hideStates();
    const errorState = document.getElementById('error-state');
    document.getElementById('error-message').textContent = msg;
    errorState.style.display = 'block';
}

function hideStates() {
    document.getElementById('loading-state').style.display = 'none';
    document.getElementById('error-state').style.display = 'none';
    document.getElementById('empty-state').style.display = 'none';
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function escapeJson(obj) {
    return JSON.stringify(obj)
        .replace(/&/g, '&amp;')
        .replace(/'/g, '&#39;')
        .replace(/"/g, '&quot;');
}
