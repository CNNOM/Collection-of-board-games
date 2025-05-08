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

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> filterGameComboBox;
    @FXML private ComboBox<GameSession.GameStatus> filterStatusComboBox;

    @FXML
    private ToggleGroup dataSourceToggleGroup;

    @FXML
    public void initialize() {
        boardGameDao = DaoFactory.createTaskDao("mongodb");

        gameHistory = boardGameDao.getGameHistory();
        allGames = boardGameDao.getAllGames();
        gameHistory = boardGameDao.getGameHistory();

        gamesComboBox.setItems(FXCollections.observableArrayList(
                allGames.stream().map(BoardGame::getName).collect(Collectors.toList()))
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
                allGames.stream().map(BoardGame::getName).collect(Collectors.toList())
        ));

        // Инициализация фильтров
        filterGameComboBox.setItems(FXCollections.observableArrayList(
                allGames.stream().map(BoardGame::getName).collect(Collectors.toList())
        ));
        filterGameComboBox.getItems().add(0, "Все игры");
        filterGameComboBox.getSelectionModel().selectFirst();

        filterStatusComboBox.setItems(FXCollections.observableArrayList(GameSession.GameStatus.values()));
        filterStatusComboBox.getItems().add(0, null);
        filterStatusComboBox.getSelectionModel().selectFirst();

        // Загрузка истории игр
        loadPlayedGames();

        // Автоматическое обновление статусов
        updateGameStatuses();
    }

    // Обновите метод loadPlayedGames()
    private void loadPlayedGames() {
        List<GameSession> sessions = boardGameDao.getGameHistory();
        sessions.sort((s1, s2) -> s2.getDateTime().compareTo(s1.getDateTime()));
        playedGamesTable.setItems(FXCollections.observableArrayList(sessions));
        playedGamesStatusLabel.setText("Всего записей: " + sessions.size());
        playedGamesStatusLabel.setStyle("-fx-text-fill: green;");
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
                .filter(session -> !session.getDateTime().toLocalDate().isBefore(excludeAfter))
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

        List<GameSession> currentGameHistory = boardGameDao.getGameHistory();

        BoardGame game = allGames.stream()
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

            // Создаем новую сессию с текущей датой и временем
            GameSession session = new GameSession(
                    null,
                    game.getId(),
                    game.getName(),
                    LocalDateTime.now(),
                    players,
                    winner,
                    GameSession.GameStatus.IN_PROGRESS
            );

            // Сохраняем в БД
            boardGameDao.addGameSession(session);

            // Обновляем таблицу
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
            // Получаем все сессии игр
            List<GameSession> sessions = boardGameDao.getGameHistory();

            // Обновляем статус для игр, которые сыграны больше суток назад
            for (GameSession session : sessions) {
                if (session.getDateTime().toLocalDate().isBefore(LocalDate.now().minusDays(1)) && session.getStatus() != GameSession.GameStatus.PLAYED) {
                    session.setStatus(GameSession.GameStatus.PLAYED);
                    boardGameDao.updateGameSessionStatus(session);
                }
            }

            // Обновляем таблицу
            loadPlayedGames();

            // Устанавливаем сообщение об успешном обновлении
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
            // Получаем выбранную сессию из таблицы
            GameSession selectedSession = playedGamesTable.getSelectionModel().getSelectedItem();
            if (selectedSession == null) {
                showPlayedGamesError("Выберите сессию для редактирования");
                return;
            }

            // Создаем диалог для выбора нового статуса
            ChoiceDialog<GameSession.GameStatus> dialog = new ChoiceDialog<>(selectedSession.getStatus(), GameSession.GameStatus.values());
            dialog.setTitle("Редактирование статуса");
            dialog.setHeaderText("Выберите новый статус для сессии");
            dialog.setContentText("Статус:");

            // Показываем диалог и ждем выбора пользователя
            Optional<GameSession.GameStatus> result = dialog.showAndWait();
            if (result.isPresent()) {
                selectedSession.setStatus(result.get());
                boardGameDao.updateGameSessionStatus(selectedSession);

                // Обновляем таблицу
                loadPlayedGames();

                // Устанавливаем сообщение об успешном обновлении
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
            List<GameSession> filteredSessions = boardGameDao.getGameHistory();

            // Фильтрация по дате
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();

            if (fromDate != null) {
                filteredSessions = filteredSessions.stream()
                        .filter(s -> !s.getDateTime().toLocalDate().isBefore(fromDate))
                        .collect(Collectors.toList());
            }

            if (toDate != null) {
                filteredSessions = filteredSessions.stream()
                        .filter(s -> !s.getDateTime().toLocalDate().isAfter(toDate))
                        .collect(Collectors.toList());
            }

            // Фильтрация по игре
            String selectedGame = filterGameComboBox.getValue();
            if (selectedGame != null && !selectedGame.equals("Все игры")) {
                filteredSessions = filteredSessions.stream()
                        .filter(s -> s.getGameName().equals(selectedGame))
                        .collect(Collectors.toList());
            }

            // Фильтрация по статусу
            GameSession.GameStatus selectedStatus = filterStatusComboBox.getValue();
            if (selectedStatus != null) {
                filteredSessions = filteredSessions.stream()
                        .filter(s -> s.getStatus() == selectedStatus)
                        .collect(Collectors.toList());
            }

            // Сортировка по дате (новые сверху)
            filteredSessions.sort((s1, s2) -> s2.getDateTime().compareTo(s1.getDateTime()));

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

            // Перезагружаем все данные
            reloadAllData();

            // Показываем сообщение об успехе
            showAlert(Alert.AlertType.INFORMATION, "Источник данных изменен",
                    "Теперь используется: " + (selectedSource.equals("mongodb") ? "MongoDB" : "JSON"));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка",
                    "Не удалось изменить источник данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reloadAllData() {
        // Обновляем все данные из нового источника
        allGames = boardGameDao.getAllGames();
        gameHistory = boardGameDao.getGameHistory();

        // Обновляем ComboBox с играми
        gamesComboBox.setItems(FXCollections.observableArrayList(
                allGames.stream().map(BoardGame::getName).collect(Collectors.toList()))
        );

        // Обновляем ComboBox в разделе сыгранных игр
        playedGameComboBox.setItems(FXCollections.observableArrayList(
                allGames.stream().map(BoardGame::getName).collect(Collectors.toList())));

        // Обновляем фильтры
        filterGameComboBox.setItems(FXCollections.observableArrayList(
                allGames.stream().map(BoardGame::getName).collect(Collectors.toList())));
        filterGameComboBox.getItems().add(0, "Все игры");
        filterGameComboBox.getSelectionModel().selectFirst();

        // Обновляем таблицы
        loadPlayedGames();
        showWinStatistics(); // Обновляем статистику, если она была открыта
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}