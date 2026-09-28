/**
 * News Dashboard - page interactions
 */

let currentArticles = [];

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    // Show current stories when the page opens
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

        // Render articles
        if (currentArticles.length === 0) {
            showEmpty();
        } else {
            renderArticles(currentArticles, 'articles-container');
            showResults();
        }

    } catch (error) {
        console.error('Fetch error:', error);
        showError(error.message || 'Unable to fetch news articles. Please verify your connection.');
    }
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
                <h4>No stories match this filter.</h4>
            </div>`;
        return;
    }

    articles.forEach(article => {
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
                        <span>${escapeHtml(article.author || 'Staff')}</span>
                        <span>${formattedDate}</span>
                    </div>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
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
