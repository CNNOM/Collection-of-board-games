package com.example.collection_board_games;

import java.time.LocalDate;
import java.util.List;

public class GameSession {
    private String id;
    private String gameId;
    private String gameName;
    private LocalDate date;
    private List<String> players;  // Храним как список
    private String winner;

    // Конструктор
    public GameSession(String id, String gameId, String gameName, LocalDate date,
                       List<String> players, String winner) {
        this.id = id;
        this.gameId = gameId;
        this.gameName = gameName;
        this.date = date;
        this.players = players;
        this.winner = winner;
    }

    // Для TableView (возвращает строку)
    public String getPlayersAsString() {
        return String.join(", ", players);
    }

    // Для обработки (возвращает список)
    public List<String> getPlayers() {
        return players;
    }

    // Остальные геттеры
    public String getId() { return id; }
    public String getGameId() { return gameId; }
    public String getGameName() { return gameName; }
    public LocalDate getDate() { return date; }
    public String getWinner() { return winner; }
}