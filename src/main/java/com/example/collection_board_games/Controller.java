package com.example.collection_board_games;

import com.example.collection_board_games.bot.BoardGameTelegramBot;
import com.example.collection_board_games.dao.BoardGameDao;
import com.example.collection_board_games.dao.DaoFactory;
import com.example.collection_board_games.game_controller.GameFilterManager;
import com.example.collection_board_games.game_controller.GameManager;
import com.example.collection_board_games.game_controller.GameSessionManager;
import com.example.collection_board_games.game_controller.UIManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Controller {
    private static final String BOT_TOKEN = "7978010225:AAGIC6g0LSgsdUzXfVwhMjL9N3DX2E3RfdU";
    private static final String BOT_USERNAME = "CollectionBoardGames_bot";

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
    private UIManager uiManager;
    private BoardGameTelegramBot telegramBot;

    @FXML
    public void initialize() {
        initializeDaoAndManagers();
        initializeUIComponents();
        initializeTelegramBot();
    }

    private void initializeDaoAndManagers() {
        boardGameDao = DaoFactory.createTaskDao("mongodb");
        gameManager = new GameManager(boardGameDao);
        gameSessionManager = new GameSessionManager(boardGameDao);
        gameFilterManager = new GameFilterManager(boardGameDao);
        uiManager = new UIManager(gamesComboBox, winStatsTable, addGameStatusLabel, playedGamesStatusLabel, playedGamesTable, gameManager);
    }

    private void initializeUIComponents() {
        uiManager.updateGamesComboBox();

        ObservableList<String> categories = FXCollections.observableArrayList(
                "Стратегия",
                "Экономическая",
                "Кооперативная",
                "Детективная",
                "Викторина",
                "Карточная",
                "Для вечеринок",
                "Абстрактная",
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

    private void initializeTelegramBot() {
        telegramBot = new BoardGameTelegramBot(BOT_TOKEN, BOT_USERNAME, this);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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

        uiManager.updateWinStatsTable(statsList);
    }

    @FXML
    private void addNewGame() {
        try {
            validateGameInput();

            gameManager.addNewGame(
                    gameNameField.getText().trim(),
                    gameDescriptionField.getText().trim(),
                    gameCategoryComboBox.getSelectionModel().getSelectedItem(),
                    minPlayersSpinner.getValue(),
                    maxPlayersSpinner.getValue(),
                    avgTimeSpinner.getValue()
            );

            uiManager.updateGamesComboBox();

            clearAddGameForm();
            uiManager.setAddGameStatus("Игра успешно добавлена!", "-fx-text-fill: green;");

        } catch (Exception e) {
            uiManager.setAddGameStatus("Ошибка при добавлении игры: " + e.getMessage(), "-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private void validateGameInput() throws IllegalArgumentException {
        if (gameNameField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Название игры не может быть пустым.");
        }
        if (gameDescriptionField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Описание игры не может быть пустым.");
        }
        if (minPlayersSpinner.getValue() <= 0) {
            throw new IllegalArgumentException("Минимальное количество игроков должно быть больше 0.");
        }
        if (maxPlayersSpinner.getValue() <= 0) {
            throw new IllegalArgumentException("Максимальное количество игроков должно быть больше 0.");
        }
        if (avgTimeSpinner.getValue() <= 0) {
            throw new IllegalArgumentException("Среднее время игры должно быть больше 0.");
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
            uiManager.setPlayedGamesStatus("Игра успешно добавлена в историю!", "-fx-text-fill: green;");

        } catch (Exception e) {
            uiManager.setPlayedGamesStatus("Ошибка: " + e.getMessage(), "-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void updateGameStatuses() {
        try {
            gameSessionManager.updateGameStatuses();
            loadPlayedGames();
            uiManager.setPlayedGamesStatus("Статусы игр успешно обновлены!", "-fx-text-fill: green;");
        } catch (Exception e) {
            uiManager.setPlayedGamesStatus("Ошибка при обновлении статусов: " + e.getMessage(), "-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void editGameStatuses() {
        try {
            GameSession selectedSession = playedGamesTable.getSelectionModel().getSelectedItem();
            if (selectedSession == null) {
                uiManager.setPlayedGamesStatus("Выберите сессию для редактирования", "-fx-text-fill: red;");
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
                uiManager.setPlayedGamesStatus("Статус сессии успешно обновлен!", "-fx-text-fill: green;");
            }
        } catch (Exception e) {
            uiManager.setPlayedGamesStatus("Ошибка при редактировании статуса: " + e.getMessage(), "-fx-text-fill: red;");
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

            uiManager.updatePlayedGamesTable(filteredSessions);
        } catch (Exception e) {
            uiManager.setPlayedGamesStatus("Ошибка фильтрации: " + e.getMessage(), "-fx-text-fill: red;");
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
            uiManager = new UIManager(gamesComboBox, winStatsTable, addGameStatusLabel, playedGamesStatusLabel, playedGamesTable, gameManager);

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
        uiManager.updateGamesComboBox();

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
        uiManager.updatePlayedGamesTable(sessions);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Методы для Telegram бота
    public String showWinStatisticsForBot(String gameName) {
        List<GameSession> gameSessions = boardGameDao.getGameHistory();

        BoardGame game = gameManager.getAllGames().stream()
                .filter(g -> g.getName().equals(gameName))
                .findFirst()
                .orElse(null);

        if (game == null) {
            return "Игра не найдена.";
        }

        List<GameSession> sessions = gameSessions.stream()
                .filter(session -> session.getGameId().equals(game.getId()))
                .collect(Collectors.toList());

        if (sessions.isEmpty()) {
            return "Нет данных о сыгранных играх для " + gameName;
        }

        Map<String, PlayerStats> statsMap = new HashMap<>();

        for (GameSession session : sessions) {
            for (String player : session.getPlayers()) {
                PlayerStats stats = statsMap.computeIfAbsent(player, PlayerStats::new);
                stats.setTotalGames(stats.getTotalGames() + 1);
            }

            if (session.getWinner() != null && !session.getWinner().isEmpty()) {
                PlayerStats winnerStats = statsMap.computeIfAbsent(session.getWinner(), PlayerStats::new);
                winnerStats.setWins(winnerStats.getWins() + 1);
            }
        }

        StringBuilder statsList = new StringBuilder("Статистика побед для игры: " + gameName + "\n");
        for (PlayerStats stats : statsMap.values()) {
            if (stats.getTotalGames() > 0) {
                double winPercentage = (double) stats.getWins() / stats.getTotalGames() * 100;
                stats.setWinPercentage(Math.round(winPercentage * 100.0) / 100.0);
            }
            statsList.append("- ").append(stats.getPlayerName()).append(": ").append(stats.getWins()).append(" побед, ").append(stats.getWinPercentage()).append("%\n");
        }

        return statsList.toString();
    }

    public String getAllGamesForBot() {
        List<BoardGame> games = gameManager.getAllGames();
        if (games.isEmpty()) {
            return "Нет доступных игр.";
        }

        StringBuilder gamesList = new StringBuilder("Список доступных игр:\n");
        for (BoardGame game : games) {
            gamesList.append("- ").append(game.getName()).append("\n");
        }

        return gamesList.toString();
    }

}
