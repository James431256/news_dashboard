package com.intelligence.news.dto;

import com.intelligence.news.model.Article;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object returned to the frontend.
 * Contains aggregated articles, sentiment statistics, and metadata.
 */
public class NewsSearchResponse {

    private String query;
    private String category;
    private int totalResults;
    private Map<String, Integer> sentimentBreakdown;
    private List<String> providers;
    private List<Article> articles;

    public NewsSearchResponse() {
    }

    public NewsSearchResponse(String query, String category, int totalResults,
                              Map<String, Integer> sentimentBreakdown,
                              List<String> providers, List<Article> articles) {
        this.query = query;
        this.category = category;
        this.totalResults = totalResults;
        this.sentimentBreakdown = sentimentBreakdown;
        this.providers = providers;
        this.articles = articles;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public Map<String, Integer> getSentimentBreakdown() {
        return sentimentBreakdown;
    }

    public void setSentimentBreakdown(Map<String, Integer> sentimentBreakdown) {
        this.sentimentBreakdown = sentimentBreakdown;
    }

    public List<String> getProviders() {
        return providers;
    }

    public void setProviders(List<String> providers) {
        this.providers = providers;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
}
