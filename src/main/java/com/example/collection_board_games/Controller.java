package com.example.collection_board_games;

import com.example.collection_board_games.dao.BoardGameDao;
import com.example.collection_board_games.dao.DaoFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Controller {
    @FXML private Spinner<Integer> playerCountSpinner;
    @FXML private Spinner<Integer> gameTimeSpinner;
    @FXML private TableView<BoardGame> gameSelectionTable;

    @FXML private Spinner<Integer> daysExcludeSpinner;
    @FXML private Label randomGameLabel;

    @FXML private ComboBox<String> gamesComboBox;
    @FXML private TableView<PlayerStats> winStatsTable;

    private BoardGameDao boardGameDao;
    private List<BoardGame> allGames;
    private List<GameSession> gameHistory;

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

    @FXML
    public void initialize() {
        boardGameDao = DaoFactory.createTaskDao("mongodb");
        allGames = boardGameDao.getAllGames();
        gameHistory = boardGameDao.getGameHistory();

        gamesComboBox.setItems(FXCollections.observableArrayList(
                allGames.stream().map(BoardGame::getName).collect(Collectors.toList())
        ));

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
                allGames.stream().map(BoardGame::getName).collect(Collectors.toList())
        ));

        // Загрузка истории игр
        loadPlayedGames();
    }

    private void loadPlayedGames() {
        List<GameSession> sessions = boardGameDao.getGameHistory();
        playedGamesTable.setItems(FXCollections.observableArrayList(sessions));
    }

    // Подбор игры для компании
    @FXML
    private void findGamesForCompany() {
        int players = playerCountSpinner.getValue();
        int maxTime = gameTimeSpinner.getValue();

        List<BoardGame> suitableGames = allGames.stream()
                .filter(game -> game.getMinPlayers() <= players && game.getMaxPlayers() >= players)
                .filter(game -> game.getAverageTime() <= maxTime)
                .collect(Collectors.toList());

        ObservableList<BoardGame> observableList = FXCollections.observableArrayList(suitableGames);
        gameSelectionTable.setItems(observableList);

        if (suitableGames.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Результаты поиска");
            alert.setHeaderText(null);
            alert.setContentText("Не найдено игр, соответствующих критериям");
            alert.showAndWait();
        }
    }

    // Ротация игр
    @FXML
    private void selectRandomGame() {
        int daysToExclude = daysExcludeSpinner.getValue();
        LocalDate excludeAfter = LocalDate.now().minusDays(daysToExclude);

        Set<String> recentlyPlayed = gameHistory.stream()
                .filter(session -> session.getDate().isAfter(excludeAfter))
                .map(GameSession::getGameId)
                .collect(Collectors.toSet());

        List<BoardGame> availableGames = allGames.stream()
                .filter(game -> !recentlyPlayed.contains(game.getId()))
                .collect(Collectors.toList());

        if (availableGames.isEmpty()) {
            randomGameLabel.setText("Нет доступных игр для ротации");
            return;
        }

        Random random = new Random();
        BoardGame selectedGame = availableGames.get(random.nextInt(availableGames.size()));
        randomGameLabel.setText("Рекомендуемая игра: " + selectedGame.getName());
    }

    // Статистика побед
    @FXML
    private void showWinStatistics() {
        String gameName = gamesComboBox.getValue();
        if (gameName == null || gameName.isEmpty()) {
            return;
        }

        BoardGame game = allGames.stream()
                .filter(g -> g.getName().equals(gameName))
                .findFirst()
                .orElse(null);

        if (game == null) return;

        Map<String, PlayerStats> statsMap = new HashMap<>();

        List<GameSession> gameSessions = gameHistory.stream()
                .filter(session -> session.getGameId() == game.getId())
                .collect(Collectors.toList());

        for (GameSession session : gameSessions) {
            for (String player : session.getPlayers()) {
                PlayerStats stats = statsMap.getOrDefault(player, new PlayerStats(player));
                stats.setTotalGames(stats.getTotalGames() + 1);
                statsMap.put(player, stats);
            }

            if (session.getWinner() != null && !session.getWinner().isEmpty()) {
                PlayerStats winnerStats = statsMap.getOrDefault(session.getWinner(), new PlayerStats(session.getWinner()));
                winnerStats.setWins(winnerStats.getWins() + 1);
                statsMap.put(session.getWinner(), winnerStats);
            }
        }

        ObservableList<PlayerStats> statsList = FXCollections.observableArrayList();
        for (PlayerStats stats : statsMap.values()) {
            if (stats.getTotalGames() > 0) {
                stats.setWinPercentage((double) stats.getWins() / stats.getTotalGames() * 100);
            }
            statsList.add(stats);
        }

        winStatsTable.setItems(statsList);
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

    @FXML
    private void addNewGame() {
        try {
            String name = gameNameField.getText().trim();
            String description = gameDescriptionField.getText().trim();
            int minPlayers = minPlayersSpinner.getValue();
            int maxPlayers = maxPlayersSpinner.getValue();
            int avgTime = avgTimeSpinner.getValue();

            if (name.isEmpty()) {
                addGameStatusLabel.setText("Ошибка: название игры обязательно");
                addGameStatusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            if (minPlayers > maxPlayers) {
                addGameStatusLabel.setText("Ошибка: минимум игроков не может быть больше максимума");
                addGameStatusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            String category = gameCategoryComboBox.getSelectionModel().getSelectedItem();

            BoardGame newGame = new BoardGame(
                    null,
                    gameNameField.getText().trim(),
                    gameDescriptionField.getText().trim(),
                    category,
                    minPlayersSpinner.getValue(),
                    maxPlayersSpinner.getValue(),
                    avgTimeSpinner.getValue()
            );

            boardGameDao.addGame(newGame);

            // Обновляем список игр
            allGames = boardGameDao.getAllGames();

            // Обновляем ComboBox в статистике
            gamesComboBox.setItems(FXCollections.observableArrayList(
                    allGames.stream().map(BoardGame::getName).collect(Collectors.toList())
            ));

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

            if (gameName == null || gameName.isEmpty()) {
                showPlayedGamesError("Выберите игру");
                return;
            }

            if (winner.isEmpty()) {
                showPlayedGamesError("Укажите победителя");
                return;
            }

            if (playersText.isEmpty()) {
                showPlayedGamesError("Укажите игроков");
                return;
            }

            List<String> players = Arrays.stream(playersText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (!players.contains(winner)) {
                showPlayedGamesError("Победитель должен быть в списке игроков");
                return;
            }

            // Находим игру в базе
            BoardGame game = allGames.stream()
                    .filter(g -> g.getName().equals(gameName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Игра не найдена"));

            // Создаем новую сессию
            GameSession session = new GameSession(
                    null,
                    game.getId(),
                    game.getName(),
                    LocalDate.now(),
                    players,
                    winner
            );

            // Сохраняем в БД
            boardGameDao.addGameSession(session);

            // Обновляем таблицу
            loadPlayedGames();

            // Очищаем поля
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
}