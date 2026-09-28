package com.intelligence.news.service;

import com.intelligence.news.client.NewsClient;
import com.intelligence.news.dto.NewsSearchResponse;
import com.intelligence.news.model.Article;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsAggregatorServiceTest {

    @Mock
    private NewsClient mockClientA;

    @Mock
    private NewsClient mockClientB;

    @Test
    @DisplayName("Should aggregate articles from multiple clients and tag sentiment")
    void testAggregatorCombinesResults() {
        when(mockClientA.getProviderName()).thenReturn("Provider A");
        when(mockClientB.getProviderName()).thenReturn("Provider B");

        Article art1 = new Article("1", "Record profit announced", "Great growth this year", "Provider A", "Alice", "http://a.com", "", "2026-09-25", "NEUTRAL");
        Article art2 = new Article("2", "Market crisis and loss", "Severe slump reported", "Provider B", "Bob", "http://b.com", "", "2026-09-25", "NEUTRAL");

        when(mockClientA.searchArticles(anyString(), anyString())).thenReturn(List.of(art1));
        when(mockClientB.searchArticles(anyString(), anyString())).thenReturn(List.of(art2));

        SentimentAnalysisService sentimentService = new SentimentAnalysisService();
        NewsAggregatorService aggregator = new NewsAggregatorService(List.of(mockClientA, mockClientB), sentimentService);

        NewsSearchResponse response = aggregator.search("economy", "business");

        assertNotNull(response);
        assertEquals(2, response.getTotalResults());
        assertEquals("POSITIVE", response.getArticles().get(0).getSentiment());
        assertEquals("NEGATIVE", response.getArticles().get(1).getSentiment());
    }

    @Test
    @DisplayName("Keeps results when one provider is unavailable")
    void testAggregatorReturnsResultsFromAvailableClient() {
        when(mockClientA.getProviderName()).thenReturn("Unavailable provider");
        when(mockClientB.getProviderName()).thenReturn("Available provider");

        Article article = new Article(
                "1", "Company reports growth", "", "Available provider", "Staff",
                "https://example.com/story", "", "2026-09-25", "NEUTRAL"
        );

        when(mockClientA.searchArticles(anyString(), anyString()))
                .thenThrow(new RuntimeException("Service unavailable"));
        when(mockClientB.searchArticles(anyString(), anyString())).thenReturn(List.of(article));

        NewsAggregatorService aggregator = new NewsAggregatorService(
                List.of(mockClientA, mockClientB), new SentimentAnalysisService()
        );

        NewsSearchResponse response = aggregator.search("company", "all");

        assertEquals(1, response.getTotalResults());
        assertEquals("Available provider", response.getArticles().get(0).getSource());
    }
}
