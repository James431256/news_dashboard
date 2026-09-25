package com.intelligence.news.model;

/**
 * Unified domain model representing a news article across all external providers.
 */
public class Article {
    private String id;
    private String title;
    private String description;
    private String source;
    private String author;
    private String url;
    private String imageUrl;
    private String publishedAt;
    private String sentiment;

    // Default constructor for serialization/deserialization frameworks
    public Article() {
    }

    // All-arguments constructor for convenient instantiation
    public Article(String id, String title, String description, String source,
                   String author, String url, String imageUrl, String publishedAt, String sentiment) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.source = source;
        this.author = author;
        this.url = url;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
        this.sentiment = sentiment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
}
