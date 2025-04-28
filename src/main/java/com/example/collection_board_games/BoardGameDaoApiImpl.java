package com.example.collection_board_games;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class BoardGameDaoApiImpl implements BoardGameDao {
    private final String apiUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public BoardGameDaoApiImpl(String apiUrl) {
        this.apiUrl = apiUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void addTask(BoardGame boardGame) {
        try {
            String requestBody = mapper.writeValueAsString(boardGame);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/tasks"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to add task via API", e);
        }
    }

    @Override
    public void updateTask(BoardGame boardGame) {
        try {
            String requestBody = mapper.writeValueAsString(boardGame);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/tasks/" + boardGame.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to update task via API", e);
        }
    }

    @Override
    public void deleteTask(int id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/tasks/" + id))
                    .DELETE()
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete task via API", e);
        }
    }

    @Override
    public List<BoardGame> getAllTasks() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/tasks"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            return mapper.readValue(response.body(),
                    mapper.getTypeFactory().constructCollectionType(List.class, BoardGame.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tasks from API", e);
        }
    }
}