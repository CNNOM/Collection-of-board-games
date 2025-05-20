package com.example.collection_board_games;

import com.example.collection_board_games.dao.BoardGameDao;


public class BoardGameService {
    private final BoardGameDao boardGameDao;

    public BoardGameService(BoardGameDao boardGameDao) {
        this.boardGameDao = boardGameDao;
    }
}