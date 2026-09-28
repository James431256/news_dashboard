package com.intelligence.news.client;

import com.intelligence.news.model.Article;
import java.util.List;

/**
 * Strategy interface for external news providers.
 * Each news provider must implement this contract.
 */
public interface NewsClient {

    /**
     * @return The human-readable name of the news provider (e.g. "The Guardian").
     */
    String getProviderName();

    /**
     * Searches for articles matching a given keyword and optional category.
     *
     * @param query    Search query or keywords entered by the user
     * @param category Optional category filter (e.g. "technology", "business")
     * @return A list of unified Article objects
     */
    List<Article> searchArticles(String query, String category);
}
