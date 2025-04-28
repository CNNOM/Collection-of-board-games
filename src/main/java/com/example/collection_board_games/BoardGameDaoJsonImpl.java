// TaskDaoJsonImpl.java
package com.example.collection_board_games;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoardGameDaoJsonImpl implements BoardGameDao {
    private final String filePath;
    private final ObjectMapper mapper;

    public BoardGameDaoJsonImpl(String filePath) {
        this.filePath = filePath;
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        if (!Files.exists(Paths.get(filePath))) {
            try {
                Files.createDirectories(Paths.get(filePath).getParent());
                mapper.writeValue(new File(filePath), new BoardGame[0]);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create JSON file", e);
            }
        }
    }

    @Override
    public void addTask(BoardGame boardGame) {
        List<BoardGame> boardGames = new ArrayList<>(getAllTasks());
        boardGame.setId(boardGames.isEmpty() ? 1 : boardGames.get(boardGames.size() - 1).getId() + 1);
        boardGames.add(boardGame);
        saveAllTasks(boardGames);
    }

    @Override
    public void updateTask(BoardGame boardGame) {
        List<BoardGame> boardGames = getAllTasks();
        boardGames.replaceAll(t -> t.getId() == boardGame.getId() ? boardGame : t);
        saveAllTasks(boardGames);
    }

    @Override
    public void deleteTask(int id) {
        List<BoardGame> boardGames = getAllTasks();
        boardGames.removeIf(t -> t.getId() == id);
        saveAllTasks(boardGames);
    }

    @Override
    public List<BoardGame> getAllTasks() {
        try {
            BoardGame[] tasksArray = mapper.readValue(new File(filePath), BoardGame[].class);
            return new ArrayList<>(Arrays.asList(tasksArray));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file", e);
        }
    }

    private void saveAllTasks(List<BoardGame> boardGames) {
        try {
            mapper.writeValue(new File(filePath), boardGames);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save to JSON file", e);
        }
    }
}