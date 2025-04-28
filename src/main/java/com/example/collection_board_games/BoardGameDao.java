package com.example.collection_board_games;

import java.util.List;

public interface BoardGameDao {
    void addTask(BoardGame boardGame);
    void updateTask(BoardGame boardGame);
    void deleteTask(int id);
    List<BoardGame> getAllTasks();


}