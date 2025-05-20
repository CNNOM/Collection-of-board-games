package com.example.collection_board_games;

import com.example.collection_board_games.dao.BoardGameDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
public class GameSessionManager {
    private BoardGameDao boardGameDao;
    private List<GameSession> gameHistory;
    private List<BoardGame> allGames; // Добавлен список игр

    public GameSessionManager(BoardGameDao boardGameDao) {
        this.boardGameDao = boardGameDao;
        this.gameHistory = boardGameDao.getGameHistory();
        this.allGames = boardGameDao.getAllGames(); // Инициализация списка игр
    }

    public void addPlayedGame(String gameName, String winner, List<String> players) {
        if (gameName == null || gameName.isEmpty()) {
            throw new IllegalArgumentException("Выберите игру");
        }

        if (winner.isEmpty()) {
            throw new IllegalArgumentException("Укажите победителя");
        }

        if (players.isEmpty()) {
            throw new IllegalArgumentException("Укажите игроков");
        }

        if (!players.contains(winner)) {
            throw new IllegalArgumentException("Победитель должен быть в списке игроков");
        }

        BoardGame game = findGameByName(gameName);
        if (game == null) {
            throw new IllegalArgumentException("Игра не найдена");
        }

        GameSession session = new GameSession(
                null,
                game.getId(),
                game.getName(),
                LocalDateTime.now(),
                players,
                winner,
                GameSession.GameStatus.IN_PROGRESS
        );

        boardGameDao.addGameSession(session);
        gameHistory = boardGameDao.getGameHistory();
    }

    public void updateGameStatuses() {
        for (GameSession session : gameHistory) {
            if (session.getDateTime().toLocalDate().isBefore(LocalDate.now().minusDays(1)) && session.getStatus() != GameSession.GameStatus.PLAYED) {
                session.setStatus(GameSession.GameStatus.PLAYED);
                boardGameDao.updateGameSessionStatus(session);
            }
        }
    }

    public void editGameStatus(GameSession session, GameSession.GameStatus newStatus) {
        session.setStatus(newStatus);
        boardGameDao.updateGameSessionStatus(session);
    }

    public List<GameSession> getGameHistory() {
        return gameHistory;
    }

    private BoardGame findGameByName(String gameName) {
        return allGames.stream()
                .filter(g -> g.getName().equals(gameName))
                .findFirst()
                .orElse(null);
    }
}
