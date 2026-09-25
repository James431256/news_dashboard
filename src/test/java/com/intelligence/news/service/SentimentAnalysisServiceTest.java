package com.intelligence.news.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SentimentAnalysisServiceTest {

    private SentimentAnalysisService sentimentService;

    @BeforeEach
    void setUp() {
        sentimentService = new SentimentAnalysisService();
    }

    @Test
    @DisplayName("Should detect POSITIVE sentiment when text contains growth and profit words")
    void testPositiveSentiment() {
        String title = "Tech giant reports record revenue and massive profit growth";
        String description = "Innovative breakthrough leads to optimistic recovery across global markets.";

        String sentiment = sentimentService.analyzeSentiment(title, description);

        assertEquals("POSITIVE", sentiment);
    }

    @Test
    @DisplayName("Should detect NEGATIVE sentiment when text contains crisis and decline words")
    void testNegativeSentiment() {
        String title = "Company faces deepening crisis and severe lawsuit";
        String description = "Shares slump after earnings drop and bankruptcy risk intensifies.";

        String sentiment = sentimentService.analyzeSentiment(title, description);

        assertEquals("NEGATIVE", sentiment);
    }

    @Test
    @DisplayName("Should detect NEUTRAL sentiment when text contains balanced or factual statements")
    void testNeutralSentiment() {
        String title = "Meeting scheduled for annual developer conference in London";
        String description = "Representatives will discuss logistics and project timelines for the upcoming season.";

        String sentiment = sentimentService.analyzeSentiment(title, description);

        assertEquals("NEUTRAL", sentiment);
    }
}
