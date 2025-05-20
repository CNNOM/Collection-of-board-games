package com.example.collection_board_games;

import com.example.collection_board_games.dao.BoardGameDao;
import com.example.collection_board_games.dao.DaoFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Controller {
    @FXML private ComboBox<String> gamesComboBox;
    @FXML private TableView<PlayerStats> winStatsTable;

    @FXML private TextField gameNameField;
    @FXML private TextArea gameDescriptionField;
    @FXML private ComboBox<String> gameCategoryComboBox;
    @FXML private Spinner<Integer> minPlayersSpinner;
    @FXML private Spinner<Integer> maxPlayersSpinner;
    @FXML private Spinner<Integer> avgTimeSpinner;
    @FXML private Label addGameStatusLabel;

    @FXML private ComboBox<String> playedGameComboBox;
    @FXML private TextField winnerField;
    @FXML private TextField playersField;
    @FXML private TableView<GameSession> playedGamesTable;
    @FXML private Label playedGamesStatusLabel;

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> filterGameComboBox;
    @FXML private ComboBox<GameSession.GameStatus> filterStatusComboBox;

    @FXML
    private ToggleGroup dataSourceToggleGroup;

    private BoardGameDao boardGameDao;
    private GameManager gameManager;
    private GameSessionManager gameSessionManager;
    private GameFilterManager gameFilterManager;

    @FXML
    public void initialize() {
        boardGameDao = DaoFactory.createTaskDao("mongodb");
        gameManager = new GameManager(boardGameDao);
        gameSessionManager = new GameSessionManager(boardGameDao);
        gameFilterManager = new GameFilterManager(boardGameDao);

        gamesComboBox.setItems(FXCollections.observableArrayList(
                gameManager.getAllGames().stream().map(BoardGame::getName).collect(Collectors.toList()))
        );

        ObservableList<String> categories = FXCollections.observableArrayList(
                "Стратегия",
                "Экономическая",
                "Кооперативная",
                "Детективная",
                "Викторина",
                "Карточная",
                "Для вечеринок",
                "Абстрактная",
                "Войнушка",
                "Ролевая"
        );
        gameCategoryComboBox.setItems(categories);
        gameCategoryComboBox.getSelectionModel().selectFirst();

        playedGameComboBox.setItems(FXCollections.observableArrayList(
                gameManager.getAllGames().stream().map(BoardGame::getName).collect(Collectors.toList()))
        );

        filterGameComboBox.setItems(FXCollections.observableArrayList(
                gameManager.getAllGames().stream().map(BoardGame::getName).collect(Collectors.toList()))
        );
        filterGameComboBox.getItems().add(0, "Все игры");
        filterGameComboBox.getSelectionModel().selectFirst();

        filterStatusComboBox.setItems(FXCollections.observableArrayList(GameSession.GameStatus.values()));
        filterStatusComboBox.getItems().add(0, null);
        filterStatusComboBox.getSelectionModel().selectFirst();

        loadPlayedGames();
        updateGameStatuses();
    }

    @FXML
    private void showWinStatistics() {
        String gameName = gamesComboBox.getValue();
        if (gameName == null || gameName.isEmpty()) {
            return;
        }

        List<GameSession> currentGameHistory = boardGameDao.getGameHistory();

        BoardGame game = gameManager.getAllGames().stream()
                .filter(g -> g.getName().equals(gameName))
                .findFirst()
                .orElse(null);

        if (game == null) return;

        List<GameSession> gameSessions = currentGameHistory.stream()
                .filter(session -> session.getGameId().equals(game.getId()))
                .collect(Collectors.toList());

        Map<String, PlayerStats> statsMap = new HashMap<>();

        for (GameSession session : gameSessions) {
            for (String player : session.getPlayers()) {
                PlayerStats stats = statsMap.computeIfAbsent(player, PlayerStats::new);
                stats.setTotalGames(stats.getTotalGames() + 1);
            }

            if (session.getWinner() != null && !session.getWinner().isEmpty()) {
                PlayerStats winnerStats = statsMap.computeIfAbsent(session.getWinner(), PlayerStats::new);
                winnerStats.setWins(winnerStats.getWins() + 1);
            }
        }

        ObservableList<PlayerStats> statsList = FXCollections.observableArrayList();
        for (PlayerStats stats : statsMap.values()) {
            if (stats.getTotalGames() > 0) {
                double winPercentage = (double) stats.getWins() / stats.getTotalGames() * 100;
                stats.setWinPercentage(Math.round(winPercentage * 100.0) / 100.0);
            }
            statsList.add(stats);
        }

        statsList.sort((s1, s2) -> Integer.compare(s2.getWins(), s1.getWins()));

        winStatsTable.setItems(statsList);
    }

    @FXML
    private void addNewGame() {
        try {
            gameManager.addNewGame(
                    gameNameField.getText().trim(),
                    gameDescriptionField.getText().trim(),
                    gameCategoryComboBox.getSelectionModel().getSelectedItem(),
                    minPlayersSpinner.getValue(),
                    maxPlayersSpinner.getValue(),
                    avgTimeSpinner.getValue()
            );

            gamesComboBox.setItems(FXCollections.observableArrayList(
                    gameManager.getAllGames().stream().map(BoardGame::getName).collect(Collectors.toList()))
            );

            clearAddGameForm();
            addGameStatusLabel.setText("Игра успешно добавлена!");
            addGameStatusLabel.setStyle("-fx-text-fill: green;");

        } catch (Exception e) {
            addGameStatusLabel.setText("Ошибка при добавлении игры: " + e.getMessage());
            addGameStatusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private void clearAddGameForm() {
        gameNameField.clear();
        gameDescriptionField.clear();
        minPlayersSpinner.getValueFactory().setValue(2);
        maxPlayersSpinner.getValueFactory().setValue(4);
        avgTimeSpinner.getValueFactory().setValue(60);
        gameCategoryComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void addPlayedGame() {
        try {
            String gameName = playedGameComboBox.getSelectionModel().getSelectedItem();
            String winner = winnerField.getText().trim();
            String playersText = playersField.getText().trim();

            List<String> players = Arrays.stream(playersText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            gameSessionManager.addPlayedGame(gameName, winner, players);

            loadPlayedGames();

            winnerField.clear();
            playersField.clear();
            playedGamesStatusLabel.setText("Игра успешно добавлена в историю!");
            playedGamesStatusLabel.setStyle("-fx-text-fill: green;");

        } catch (Exception e) {
            showPlayedGamesError("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showPlayedGamesError(String message) {
        playedGamesStatusLabel.setText(message);
        playedGamesStatusLabel.setStyle("-fx-text-fill: red;");
    }

    @FXML
    private void updateGameStatuses() {
        try {
            gameSessionManager.updateGameStatuses();
            loadPlayedGames();
            playedGamesStatusLabel.setText("Статусы игр успешно обновлены!");
            playedGamesStatusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            showPlayedGamesError("Ошибка при обновлении статусов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void editGameStatuses() {
        try {
            GameSession selectedSession = playedGamesTable.getSelectionModel().getSelectedItem();
            if (selectedSession == null) {
                showPlayedGamesError("Выберите сессию для редактирования");
                return;
            }

            ChoiceDialog<GameSession.GameStatus> dialog = new ChoiceDialog<>(selectedSession.getStatus(), GameSession.GameStatus.values());
            dialog.setTitle("Редактирование статуса");
            dialog.setHeaderText("Выберите новый статус для сессии");
            dialog.setContentText("Статус:");

            Optional<GameSession.GameStatus> result = dialog.showAndWait();
            if (result.isPresent()) {
                gameSessionManager.editGameStatus(selectedSession, result.get());
                loadPlayedGames();
                playedGamesStatusLabel.setText("Статус сессии успешно обновлен!");
                playedGamesStatusLabel.setStyle("-fx-text-fill: green;");
            }
        } catch (Exception e) {
            showPlayedGamesError("Ошибка при редактировании статуса: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void applyPlayedGamesFilter() {
        try {
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            String selectedGame = filterGameComboBox.getValue();
            GameSession.GameStatus selectedStatus = filterStatusComboBox.getValue();

            List<GameSession> filteredSessions = gameFilterManager.applyFilter(fromDate, toDate, selectedGame, selectedStatus);

            playedGamesTable.setItems(FXCollections.observableArrayList(filteredSessions));
            playedGamesStatusLabel.setText("Найдено записей: " + filteredSessions.size());
            playedGamesStatusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            playedGamesStatusLabel.setText("Ошибка фильтрации: " + e.getMessage());
            playedGamesStatusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void resetPlayedGamesFilter() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        filterGameComboBox.getSelectionModel().selectFirst();
        filterStatusComboBox.getSelectionModel().selectFirst();
        loadPlayedGames();
    }

    @FXML
    private void changeDataSource() {
        try {
            String selectedSource = dataSourceToggleGroup.getSelectedToggle().getUserData().toString();
            boardGameDao = DaoFactory.createTaskDao(selectedSource);
            gameManager = new GameManager(boardGameDao);
            gameSessionManager = new GameSessionManager(boardGameDao);
            gameFilterManager = new GameFilterManager(boardGameDao);

            reloadAllData();

            showAlert(Alert.AlertType.INFORMATION, "Источник данных изменен",
                    "Теперь используется: " + (selectedSource.equals("mongodb") ? "MongoDB" : "JSON"));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка",
                    "Не удалось изменить источник данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reloadAllData() {
        gamesComboBox.setItems(FXCollections.observableArrayList(
                gameManager.getAllGames().stream().map(BoardGame::getName).collect(Collectors.toList()))
        );

        playedGameComboBox.setItems(FXCollections.observableArrayList(
                gameManager.getAllGames().stream().map(BoardGame::getName).collect(Collectors.toList()))
        );

        filterGameComboBox.setItems(FXCollections.observableArrayList(
                gameManager.getAllGames().stream().map(BoardGame::getName).collect(Collectors.toList()))
        );
        filterGameComboBox.getItems().add(0, "Все игры");
        filterGameComboBox.getSelectionModel().selectFirst();

        loadPlayedGames();
        showWinStatistics();
    }

    private void loadPlayedGames() {
        List<GameSession> sessions = gameSessionManager.getGameHistory();
        sessions.sort((s1, s2) -> s2.getDateTime().compareTo(s1.getDateTime()));
        playedGamesTable.setItems(FXCollections.observableArrayList(sessions));
        playedGamesStatusLabel.setText("Всего записей: " + sessions.size());
        playedGamesStatusLabel.setStyle("-fx-text-fill: green;");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class PlayerStats {
        private final String playerName;
        private int wins;
        private int totalGames;
        private double winPercentage;

        public PlayerStats(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerName() { return playerName; }
        public int getWins() { return wins; }
        public void setWins(int wins) { this.wins = wins; }
        public int getTotalGames() { return totalGames; }
        public void setTotalGames(int totalGames) { this.totalGames = totalGames; }
        public double getWinPercentage() { return winPercentage; }
        public void setWinPercentage(double winPercentage) { this.winPercentage = winPercentage; }
    }
}
