package com.intelligence.news.service;

import com.intelligence.news.client.NewsClient;
import com.intelligence.news.dto.NewsSearchResponse;
import com.intelligence.news.model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Core business service.
 * Coordinates parallel API fetching across all registered NewsClient strategies,
 * performs sentiment enrichment, and compiles executive statistics.
 */
@Service
public class NewsAggregatorService {

    private static final Logger logger = LoggerFactory.getLogger(NewsAggregatorService.class);

    private final List<NewsClient> newsClients;
    private final SentimentAnalysisService sentimentAnalysisService;

    // Spring automatically discovers and injects all beans implementing NewsClient
    public NewsAggregatorService(List<NewsClient> newsClients,
                                 SentimentAnalysisService sentimentAnalysisService) {
        this.newsClients = newsClients;
        this.sentimentAnalysisService = sentimentAnalysisService;
        logger.info("NewsAggregatorService initialized with {} providers: {}",
                newsClients.size(),
                newsClients.stream().map(NewsClient::getProviderName).collect(Collectors.toList()));
    }

    /**
     * Executes parallel searches across all news providers.
     */
    public NewsSearchResponse search(String query, String category) {
        String sanitizedQuery = query != null ? query.trim() : "";
        String sanitizedCategory = category != null ? category.trim() : "all";

        logger.info("Initiating parallel search for query='{}', category='{}'", sanitizedQuery, sanitizedCategory);

        // Fetch from each provider concurrently using CompletableFuture
        List<CompletableFuture<List<Article>>> futures = newsClients.stream()
                .map(client -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return client.searchArticles(sanitizedQuery, sanitizedCategory);
                    } catch (Exception e) {
                        logger.error("Error fetching from {}: {}", client.getProviderName(), e.getMessage());
                        return Collections.<Article>emptyList();
                    }
                }))
                .collect(Collectors.toList());

        // Wait for all provider tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Collect and flatten all articles
        List<Article> aggregatedArticles = futures.stream()
                .flatMap(future -> future.join().stream())
                .collect(Collectors.toList());

        // Add a sentiment label to each article
        for (Article article : aggregatedArticles) {
            String sentiment = sentimentAnalysisService.analyzeSentiment(article.getTitle(), article.getDescription());
            article.setSentiment(sentiment);
        }

        // List of providers actually involved
        List<String> providerNames = newsClients.stream()
                .map(NewsClient::getProviderName)
                .collect(Collectors.toList());

        return new NewsSearchResponse(
                sanitizedQuery,
                sanitizedCategory,
                aggregatedArticles.size(),
                providerNames,
                aggregatedArticles
        );
    }
}
