package com.example.collection_board_games.game_controller;

import com.example.collection_board_games.BoardGame;
import com.example.collection_board_games.GameSession;
import com.example.collection_board_games.PlayerStats;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;

public class UIManager {
    private ComboBox<String> gamesComboBox;
    private TableView<PlayerStats> winStatsTable;
    private Label addGameStatusLabel;
    private Label playedGamesStatusLabel;
    private TableView<GameSession> playedGamesTable;
    private GameManager gameManager;

    public UIManager(ComboBox<String> gamesComboBox, TableView<PlayerStats> winStatsTable,
                     Label addGameStatusLabel, Label playedGamesStatusLabel,
                     TableView<GameSession> playedGamesTable, GameManager gameManager) {
        this.gamesComboBox = gamesComboBox;
        this.winStatsTable = winStatsTable;
        this.addGameStatusLabel = addGameStatusLabel;
        this.playedGamesStatusLabel = playedGamesStatusLabel;
        this.playedGamesTable = playedGamesTable;
        this.gameManager = gameManager;
    }

    public void updateGamesComboBox() {
        gamesComboBox.setItems(FXCollections.observableArrayList(
                gameManager.getAllGames().stream().map(BoardGame::getName).collect(Collectors.toList()))
        );
    }

    public void updateWinStatsTable(ObservableList<PlayerStats> statsList) {
        winStatsTable.setItems(statsList);
    }

    public void setAddGameStatus(String message, String style) {
        addGameStatusLabel.setText(message);
        addGameStatusLabel.setStyle(style);
    }

    public void setPlayedGamesStatus(String message, String style) {
        playedGamesStatusLabel.setText(message);
        playedGamesStatusLabel.setStyle(style);
    }

    public void updatePlayedGamesTable(List<GameSession> sessions) {
        sessions.sort((s1, s2) -> s2.getDateTime().compareTo(s1.getDateTime()));
        playedGamesTable.setItems(FXCollections.observableArrayList(sessions));
        setPlayedGamesStatus("Всего записей: " + sessions.size(), "-fx-text-fill: green;");
    }
}
