package com.example.collection_board_games.dao;

import com.example.collection_board_games.BoardGame;
import com.example.collection_board_games.GameSession;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<BoardGame> findGamesByPlayersAndTime(int players, int maxTime) {
        return games.stream()
                .filter(game -> game.getMinPlayers() <= players && game.getMaxPlayers() >= players)
                .filter(game -> game.getAverageTime() <= maxTime)
                .collect(Collectors.toList());
    }

    @Override
    public List<GameSession> getRecentSessions(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return sessions.stream()
                .filter(session -> session.getDateTime().isAfter(cutoff))
                .collect(Collectors.toList());
    }

    @Override
    public void updateGame(BoardGame updatedGame) {
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).getId().equals(updatedGame.getId())) {
                games.set(i, updatedGame);
                return;
            }
        }
    }

    @Override
    public void deleteGame(String id) {
        games.removeIf(game -> game.getId().equals(id));
    }

    @Override
    public BoardGame getGameById(String id) {
        return games.stream()
                .filter(game -> game.getId().equals(id))
                .findFirst()
                .orElse(null);
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
