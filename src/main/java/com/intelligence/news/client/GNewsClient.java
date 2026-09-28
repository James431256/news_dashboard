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

/** Calls GNews and converts its results into the app's Article format. */
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
        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("GNews API key is not configured. Skipping GNews.");
            return articles;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("q", query)
                    .queryParam("apikey", apiKey)
                    .queryParam("lang", "en")
                    .queryParam("max", 10)
                    .toUriString();

            String jsonResponse = restTemplate.getForObject(url, String.class);
            if (jsonResponse == null || jsonResponse.isBlank()) {
                return articles;
            }

            JsonNode items = objectMapper.readTree(jsonResponse).path("articles");
            if (!items.isArray()) {
                return articles;
            }

            for (JsonNode item : items) {
                String articleUrl = item.path("url").asText("");
                String title = item.path("title").asText("");
                String description = item.path("description").asText("No summary provided.");
                String source = item.path("source").path("name").asText("GNews");
                String imageUrl = item.path("image").asText("");
                String publishedAt = item.path("publishedAt").asText("");

                articles.add(new Article(
                        articleUrl,
                        title,
                        description,
                        source,
                        "Staff Reporter",
                        articleUrl,
                        imageUrl,
                        publishedAt,
                        "NEUTRAL"
                ));
            }
        } catch (Exception e) {
            logger.warn("GNews API request failed: {}. Continuing with other results.", e.getMessage());
        }

        return articles;
    }
}