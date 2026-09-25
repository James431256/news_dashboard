package com.intelligence.news.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligence.news.model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Client adapter for GNews API (https://gnews.io).
 * Integrates global news sources when an API key is configured.
 */
@Component
public class GNewsClient implements NewsClient {

    private static final Logger logger = LoggerFactory.getLogger(GNewsClient.class);
    private static final String BASE_URL = "https://gnews.io/api/v4/search";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GNewsClient(RestTemplate restTemplate,
                       ObjectMapper objectMapper,
                       @Value("${gnews.api.key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    @Override
    public String getProviderName() {
        return "GNews";
    }

    @Override
    public List<Article> searchArticles(String query, String category) {
        List<Article> articles = new ArrayList<>();

        // If no API key configured, skip gracefully without throwing errors
        if (apiKey == null || apiKey.isBlank()) {
            logger.debug("GNews API key is not configured. Skipping GNews provider.");
            return articles;
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("q", query)
                    .queryParam("apikey", apiKey)
                    .queryParam("lang", "en")
                    .queryParam("max", 10);

            if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) {
                builder.queryParam("category", category.toLowerCase());
            }

            String url = builder.toUriString();
            logger.info("Calling GNews API: query='{}'", query);

            String jsonResponse = restTemplate.getForObject(url, String.class);
            if (jsonResponse == null || jsonResponse.isBlank()) {
                return articles;
            }

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode items = root.path("articles");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    String title = item.path("title").asText("");
                    String description = item.path("description").asText("No summary provided.");
                    String urlLink = item.path("url").asText("");
                    String imageUrl = item.path("image").asText("");
                    String publishedAt = item.path("publishedAt").asText("");
                    String sourceName = item.path("source").path("name").asText("GNews Partner");

                    String id = "gnews-" + UUID.randomUUID().toString();

                    Article article = new Article(
                            id,
                            title,
                            description,
                            sourceName,
                            "Staff Reporter",
                            urlLink,
                            imageUrl,
                            publishedAt,
                            "NEUTRAL"
                    );
                    articles.add(article);
                }
            }
        } catch (Exception e) {
            logger.warn("GNews API request failed: {}. Continuing gracefully.", e.getMessage());
        }

        return articles;
    }
}
