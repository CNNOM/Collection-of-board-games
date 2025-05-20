package com.example.collection_board_games.dao;

import com.example.collection_board_games.BoardGame;
import com.example.collection_board_games.GameSession;

import java.util.*;

public class BoardGameDaoMemoryImpl implements BoardGameDao {
    private final List<BoardGame> games = new ArrayList<>();
    private final List<GameSession> sessions = new ArrayList<>();

    @Override
    public List<BoardGame> getAllGames() {
        return new ArrayList<>(games);
    }

    @Override
    public List<GameSession> getGameHistory() {
        return new ArrayList<>(sessions);
    }

    @Override
    public void addGame(BoardGame game) {
        if (game.getId() == null) {
            game.setId(UUID.randomUUID().toString());
        }
        games.add(game);
    }

    @Override
    public void addGameSession(GameSession session) {
        if (session.getId() == null) {
            session.setId(UUID.randomUUID().toString());
        }
        sessions.add(session);
    }


    @Override
    public void updateGameSessionStatus(GameSession session) {
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getId().equals(session.getId())) {
                sessions.set(i, session);
                return;
            }
        }
    }

    @Override
    public void close() {
    }
}
