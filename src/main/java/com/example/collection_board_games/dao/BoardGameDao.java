package com.example.collection_board_games.dao;

import com.example.collection_board_games.BoardGame;
import com.example.collection_board_games.GameSession;
import java.util.List;

public interface BoardGameDao extends AutoCloseable {
    List<BoardGame> getAllGames();
    void addGame(BoardGame game);
    void updateGame(BoardGame game);
    void deleteGame(String id);
    BoardGame getGameById(String id);
    List<BoardGame> findGamesByPlayersAndTime(int players, int maxTime);

    List<GameSession> getGameHistory();
    void addGameSession(GameSession session);

    void close();
}