// TaskDaoMemoryImpl.java
package com.example.collection_board_games;

import java.util.ArrayList;
import java.util.List;

public class BoardGameDaoMemoryImpl implements BoardGameDao {
    private final List<BoardGame> boardGames = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void addTask(BoardGame boardGame) {
        boardGame.setId(nextId++);
        boardGames.add(boardGame);
    }

    @Override
    public void updateTask(BoardGame boardGame) {
        for (int i = 0; i < boardGames.size(); i++) {
            if (boardGames.get(i).getId() == boardGame.getId()) {
                boardGames.set(i, boardGame);
                break;
            }
        }
    }

    @Override
    public void deleteTask(int id) {
        boardGames.removeIf(task -> task.getId() == id);
    }

    @Override
    public List<BoardGame> getAllTasks() {
        return new ArrayList<>(boardGames);
    }
}