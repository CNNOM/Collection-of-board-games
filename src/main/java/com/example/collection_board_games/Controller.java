// MainController.java
package com.example.collection_board_games;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;

public class Controller {
    private TaskService taskService;
    private ObservableList<BoardGame> boardGameList = FXCollections.observableArrayList();

    @FXML private TableView<BoardGame> taskTable;
    @FXML private TableColumn<BoardGame, Integer> idColumn;
    @FXML private TableColumn<BoardGame, String> titleColumn;
    @FXML private TableColumn<BoardGame, String> descriptionColumn;
    @FXML private TableColumn<BoardGame, String> categoryColumn;
    @FXML private TableColumn<BoardGame, String> priorityColumn;
    @FXML private TableColumn<BoardGame, String> statusColumn;
    @FXML private TableColumn<BoardGame, LocalDate> dueDateColumn;

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> storageTypeComboBox;

    @FXML
    public void initialize() {
        // Инициализация сервиса без приведения типов
        taskService = new TaskService(DaoFactory.createTaskDao("memory"));

        // Настройка столбцов таблицы
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        priorityColumn.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());

        // Инициализация ComboBox
        categoryComboBox.setItems(FXCollections.observableArrayList("Работа", "Личные данные", "Учеба", "Учеба"));
        priorityComboBox.setItems(FXCollections.observableArrayList("Высокий", "Средний", "Средний"));
        statusComboBox.setItems(FXCollections.observableArrayList("Не запущен", "Выполняется", "Завершен", "На удержании"));
        storageTypeComboBox.setItems(FXCollections.observableArrayList("Memory", "JSON", "MongoDB", "API"));
        storageTypeComboBox.getSelectionModel().selectFirst();

        // Загрузка данных
        refreshTaskList();
    }

    private void refreshTaskList() {
        boardGameList.setAll(taskService.getAllTasks());
        taskTable.setItems(boardGameList);
    }

    @FXML
    private void handleAddTask() {
        BoardGame boardGame = new BoardGame(
                0, // ID will be set by service
                titleField.getText(),
                descriptionField.getText(),
                categoryComboBox.getValue(),
                priorityComboBox.getValue(),
                statusComboBox.getValue(),
                dueDatePicker.getValue()
        );

        taskService.addTask(boardGame);
        refreshTaskList();
    }

    @FXML
    private void handleUpdateTask() {
        BoardGame selectedBoardGame = taskTable.getSelectionModel().getSelectedItem();
        if (selectedBoardGame != null) {
            selectedBoardGame.setTitle(titleField.getText());
            selectedBoardGame.setDescription(descriptionField.getText());
            selectedBoardGame.setCategory(categoryComboBox.getValue());
            selectedBoardGame.setPriority(priorityComboBox.getValue());
            selectedBoardGame.setStatus(statusComboBox.getValue());
            selectedBoardGame.setDueDate(dueDatePicker.getValue());

            taskService.updateTask(selectedBoardGame);
            refreshTaskList();
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            refreshTaskList();
        } else {
            boardGameList.setAll(taskService.searchTasks(searchText));
        }
    }

    @FXML
    private void handleStorageChange() {
        String storageType = storageTypeComboBox.getValue();
        taskService = new TaskService((BoardGameDao) DaoFactory.createTaskDao(storageType.toLowerCase()));
        refreshTaskList();
    }
}