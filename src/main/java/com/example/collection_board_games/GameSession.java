package com.example.collection_board_games;

import java.time.LocalDate;
import java.util.List;

public class GameSession {
    public enum GameStatus {
        IN_PROGRESS, PLAYED
    }

    private String id;
    private String gameId;
    private String gameName;
    private LocalDate date;
    private List<String> players;
    private String winner;
    private GameStatus status;

    public GameSession(String id, String gameId, String gameName, LocalDate date,
                       List<String> players, String winner, GameStatus status) {
        this.id = id;
        this.gameId = gameId;
        this.gameName = gameName;
        this.date = date;
        this.players = players;
        this.winner = winner;
        this.status = status;
    }

    public String getId() { return id; }
    public String getGameId() { return gameId; }
    public String getGameName() { return gameName; }
    public LocalDate getDate() { return date; }
    public List<String> getPlayers() { return players; }
    public String getWinner() { return winner; }
    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public String getPlayersAsString() {
        return String.join(", ", players);
    }
}
