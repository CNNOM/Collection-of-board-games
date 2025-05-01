package com.example.collection_board_games;

import org.bson.Document;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class GameSession {
    private String id;
    private String gameId;
    private LocalDate date;
    private List<String> players;
    private String winner;

    public GameSession(String id, String gameId, LocalDate date, List<String> players, String winner) {
        this.id = id;
        this.gameId = gameId;
        this.date = date;
        this.players = players;
        this.winner = winner;
    }

    public String getId() { return id; }
    public String getGameId() { return gameId; }
    public LocalDate getDate() { return date; }
    public List<String> getPlayers() { return players; }
    public String getWinner() { return winner; }

}