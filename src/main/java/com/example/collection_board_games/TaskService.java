// TaskService.java
package com.example.collection_board_games;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TaskService {
    private BoardGameDao boardGameDao;

    public TaskService(BoardGameDao boardGameDao) {
        this.boardGameDao = boardGameDao;
    }

    public void addTask(BoardGame boardGame) {
        boardGameDao.addTask(boardGame);
    }

    public void updateTask(BoardGame boardGame) {
        boardGameDao.updateTask(boardGame);
    }

    public void deleteTask(int id) {
        boardGameDao.deleteTask(id);
    }

    public List<BoardGame> getAllTasks() {
        return boardGameDao.getAllTasks();
    }

    public List<BoardGame> searchTasks(String searchText) {
        return boardGameDao.getAllTasks().stream()
                .filter(task ->
                        task.getTitle().toLowerCase().contains(searchText) ||
                                task.getDescription().toLowerCase().contains(searchText) ||
                                task.getCategory().toLowerCase().contains(searchText) ||
                                task.getPriority().toLowerCase().contains(searchText) ||
                                task.getStatus().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
    }

    public void updateTaskStatuses() {
        LocalDate today = LocalDate.now();
        List<BoardGame> boardGames = getAllTasks();

        for (BoardGame boardGame : boardGames) {
            if (boardGame.getDueDate().isBefore(today) && !boardGame.getStatus().equals("Completed")) {
                boardGame.setStatus("Overdue");
                updateTask(boardGame);
            }
        }
    }
}