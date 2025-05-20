package com.example.collection_board_games;

import com.example.collection_board_games.dao.BoardGameDao;

import java.util.List;

public class GameManager {
    private BoardGameDao boardGameDao;
    private List<BoardGame> allGames;

    public GameManager(BoardGameDao boardGameDao) {
        this.boardGameDao = boardGameDao;
        this.allGames = boardGameDao.getAllGames();
    }

    public void addNewGame(String name, String description, String category, int minPlayers, int maxPlayers, int avgTime) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Ошибка: название игры обязательно");
        }

        if (minPlayers > maxPlayers) {
            throw new IllegalArgumentException("Ошибка: минимум игроков не может быть больше максимума");
        }

        BoardGame newGame = new BoardGame(
                null,
                name,
                description,
                category,
                minPlayers,
                maxPlayers,
                avgTime
        );

        boardGameDao.addGame(newGame);
        allGames = boardGameDao.getAllGames();
    }

    public List<BoardGame> getAllGames() {
        return allGames;
    }
}
