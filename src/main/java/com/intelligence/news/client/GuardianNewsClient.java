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
 * Client adapter for The Guardian Open Platform API.
 * Maps Guardian API results into the unified Article model.
 */
@Component
public class GuardianNewsClient implements NewsClient {

    private static final Logger logger = LoggerFactory.getLogger(GuardianNewsClient.class);
    private static final String BASE_URL = "https://content.guardianapis.com/search";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GuardianNewsClient(RestTemplate restTemplate,
                              ObjectMapper objectMapper,
                              @Value("${guardian.api.key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    @Override
    public String getProviderName() {
        return "The Guardian";
    }

    @Override
    public List<Article> searchArticles(String query, String category) {
        List<Article> articles = new ArrayList<>();
        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("Guardian API key is not configured. Skipping The Guardian.");
            return articles;
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("q", query)
                    .queryParam("api-key", apiKey)
                    .queryParam("show-fields", "thumbnail,trailText,byline")
                    .queryParam("page-size", 12);

            if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) {
                builder.queryParam("section", category.toLowerCase());
            }

            String url = builder.toUriString();
            logger.info("Calling The Guardian API: query='{}', category='{}'", query, category);

            String jsonResponse = restTemplate.getForObject(url, String.class);
            if (jsonResponse == null || jsonResponse.isBlank()) {
                return articles;
            }

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode results = root.path("response").path("results");

            if (results.isArray()) {
                for (JsonNode item : results) {
                    JsonNode fields = item.path("fields");

                    String title = item.path("webTitle").asText("");
                    String webUrl = item.path("webUrl").asText("");
                    String publishedAt = item.path("webPublicationDate").asText("");
                    String description = fields.path("trailText").asText("No summary available.");
                    // Strip any HTML tags that The Guardian trailText sometimes contains
                    description = description.replaceAll("<[^>]*>", "");
                    String author = fields.path("byline").asText("The Guardian Staff");
                    String imageUrl = fields.path("thumbnail").asText("");

                    String id = "guardian-" + item.path("id").asText(UUID.randomUUID().toString());

                    Article article = new Article(
                            id,
                            title,
                            description,
                            "The Guardian",
                            author,
                            webUrl,
                            imageUrl,
                            publishedAt,
                            "NEUTRAL" // Sentiment is added after the provider results are combined
                    );
                    articles.add(article);
                }
            }

        } catch (Exception e) {
            logger.warn("The Guardian API request failed: {}. Continuing gracefully.", e.getMessage());
        }

        return articles;
    }
}
