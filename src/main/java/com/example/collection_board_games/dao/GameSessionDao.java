package com.example.collection_board_games.dao;

import com.example.collection_board_games.GameSession;
import java.util.List;

public interface GameSessionDao extends AutoCloseable {
    List<GameSession> getGameHistory();
    void addGameSession(GameSession session);
    void updateGameSessionStatus(GameSession session);
    void close();
}
