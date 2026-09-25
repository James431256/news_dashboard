package com.intelligence.news.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligence.news.model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Client adapter for Hacker News / Algolia Public Search API.
 * Provides live tech news and discussions with zero API key requirement.
 */
@Component
public class HackerNewsClient implements NewsClient {

    private static final Logger logger = LoggerFactory.getLogger(HackerNewsClient.class);
    private static final String BASE_URL = "https://hn.algolia.com/api/v1/search";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HackerNewsClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProviderName() {
        return "Hacker News";
    }

    @Override
    public List<Article> searchArticles(String query, String category) {
        List<Article> articles = new ArrayList<>();

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("query", query)
                    .queryParam("tags", "story")
                    .queryParam("hitsPerPage", 12);

            String url = builder.toUriString();
            logger.info("Calling Hacker News API: query='{}'", query);

            String jsonResponse = restTemplate.getForObject(url, String.class);
            if (jsonResponse == null || jsonResponse.isBlank()) {
                return articles;
            }

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode hits = root.path("hits");

            if (hits.isArray()) {
                for (JsonNode item : hits) {
                    String objectId = item.path("objectID").asText();
                    String title = item.path("title").asText("");
                    String externalUrl = item.path("url").asText("");

                    // If story is a self-post without external link, point to the discussion thread
                    if (externalUrl.isBlank()) {
                        externalUrl = "https://news.ycombinator.com/item?id=" + objectId;
                    }

                    String author = item.path("author").asText("Hacker News Contributor");
                    String publishedAt = item.path("created_at").asText("");
                    int points = item.path("points").asInt(0);
                    int comments = item.path("num_comments").asInt(0);

                    String description = String.format("Community discussion (%d points, %d comments). Source: Hacker News Community.", points, comments);
                    String id = "hn-" + objectId;

                    Article article = new Article(
                            id,
                            title,
                            description,
                            "Hacker News",
                            author,
                            externalUrl,
                            "", // Hacker news does not provide thumbnails
                            publishedAt,
                            "NEUTRAL"
                    );
                    articles.add(article);
                }
            }

        } catch (Exception e) {
            logger.warn("Hacker News API request failed: {}. Continuing gracefully.", e.getMessage());
        }

        return articles;
    }
}
