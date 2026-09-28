package com.intelligence.news.controller;

import com.intelligence.news.dto.NewsSearchResponse;
import com.intelligence.news.service.NewsAggregatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API controller for news searches.
 */
@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
public class NewsController {

    private final NewsAggregatorService aggregatorService;

    public NewsController(NewsAggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    /**
     * Search news across providers with keyword and category.
     * Validates input and returns aggregated, sentiment-tagged results.
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchNews(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", required = false, defaultValue = "all") String category) {

        // Validate user input gracefully
        if (query == null || query.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 400);
            errorResponse.put("error", "Bad Request");
            errorResponse.put("message", "Search query cannot be empty. Please enter a topic, company, or keyword.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        NewsSearchResponse response = aggregatorService.search(query.trim(), category);
        return ResponseEntity.ok(response);
    }
}
