package com.example.collection_board_games;

import com.example.collection_board_games.dao.BoardGameDao;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BoardGameService {
    private final BoardGameDao boardGameDao;

    public BoardGameService(BoardGameDao boardGameDao) {
        this.boardGameDao = boardGameDao;
    }

    public void addGame(BoardGame boardGame) {
        boardGameDao.addGame(boardGame);
    }
}