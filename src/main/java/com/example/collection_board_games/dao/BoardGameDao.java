package com.example.collection_board_games.dao;

import com.example.collection_board_games.BoardGame;
import com.example.collection_board_games.GameSession;
import java.util.List;

public interface BoardGameDao extends AutoCloseable {
    List<BoardGame> getAllGames();
    void addGame(BoardGame game);

    List<GameSession> getGameHistory();
    void addGameSession(GameSession session);
    void updateGameSessionStatus(GameSession session);

    void close();
}