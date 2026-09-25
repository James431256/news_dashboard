package com.intelligence.news.controller;

import com.intelligence.news.dto.NewsSearchResponse;
import com.intelligence.news.service.NewsAggregatorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsAggregatorService aggregatorService;

    @Test
    @DisplayName("GET /api/news/search with valid query returns 200 OK and response JSON")
    void testSearchSuccess() throws Exception {
        NewsSearchResponse mockResponse = new NewsSearchResponse(
                "technology",
                "all",
                0,
                Map.of("POSITIVE", 0, "NEUTRAL", 0, "NEGATIVE", 0),
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(aggregatorService.search(anyString(), anyString())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/news/search").param("query", "technology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("technology"))
                .andExpect(jsonPath("$.totalResults").value(0));
    }

    @Test
    @DisplayName("GET /api/news/search with empty query returns 400 Bad Request")
    void testSearchEmptyQueryReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/news/search").param("query", "  "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
