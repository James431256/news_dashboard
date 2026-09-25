package com.intelligence.news.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Heuristic Sentiment Analysis Service.
 * Evaluates article headlines and summaries to categorize sentiment into POSITIVE, NEGATIVE, or NEUTRAL.
 */
@Service
public class SentimentAnalysisService {

    private final Set<String> positiveWords = new HashSet<>(Arrays.asList(
            "growth", "surge", "record", "gain", "profit", "breakthrough", "success", "innovative",
            "win", "boost", "optimistic", "recovery", "soar", "rally", "lead", "milestone",
            "positive", "expansion", "bullish", "upgrade", "outperform", "thrive", "promising"
    ));

    private final Set<String> negativeWords = new HashSet<>(Arrays.asList(
            "crisis", "fall", "drop", "loss", "crash", "slump", "decline", "collapse",
            "struggle", "lawsuit", "warn", "threat", "inflation", "plunge", "deficit", "risk",
            "negative", "layoff", "recession", "bearish", "downgrade", "scandal", "investigation"
    ));

    /**
     * Determines sentiment of an article based on its title and description.
     */
    public String analyzeSentiment(String title, String description) {
        String combined = ((title != null ? title : "") + " " + (description != null ? description : ""))
                .toLowerCase(Locale.ROOT);

        String[] tokens = combined.split("[^a-zA-Z]+");
        int score = 0;

        for (String token : tokens) {
            if (token.isBlank()) continue;
            if (positiveWords.contains(token)) {
                score++;
            } else if (negativeWords.contains(token)) {
                score--;
            }
        }

        if (score >= 1) {
            return "POSITIVE";
        } else if (score <= -1) {
            return "NEGATIVE";
        } else {
            return "NEUTRAL";
        }
    }
}
