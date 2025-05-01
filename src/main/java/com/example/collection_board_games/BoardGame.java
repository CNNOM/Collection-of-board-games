package com.example.collection_board_games;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class BoardGame {
    private final SimpleStringProperty id = new SimpleStringProperty();
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final SimpleStringProperty category = new SimpleStringProperty();
    private final SimpleIntegerProperty minPlayers = new SimpleIntegerProperty();
    private final SimpleIntegerProperty maxPlayers = new SimpleIntegerProperty();
    private final SimpleIntegerProperty averageTime = new SimpleIntegerProperty();

    public BoardGame(String id, String name, String description, String category,
                     int minPlayers, int maxPlayers, int averageTime) {
        this.id.set(id);
        this.name.set(name);
        this.description.set(description);
        this.category.set(category);
        this.minPlayers.set(minPlayers);
        this.maxPlayers.set(maxPlayers);
        this.averageTime.set(averageTime);
    }

    // Геттеры для свойств (нужны для PropertyValueFactory)
    public String getName() { return name.get(); }
    public int getMinPlayers() { return minPlayers.get(); }
    public int getMaxPlayers() { return maxPlayers.get(); }
    public int getAverageTime() { return averageTime.get(); }
    public String getCategory() { return category.get(); }
    public String getId() { return id.get(); }
    public String getDescription() { return description.get(); }

    // Сеттеры
    public void setName(String name) { this.name.set(name); }
    public void setMinPlayers(int minPlayers) { this.minPlayers.set(minPlayers); }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers.set(maxPlayers); }
    public void setAverageTime(int averageTime) { this.averageTime.set(averageTime); }
    public void setCategory(String category) { this.category.set(category); }
    public void setId(String id) { this.id.set(id); }
    public void setDescription(String description) { this.description.set(description); }

    // Property-геттеры (для возможных будущих привязок)
    public SimpleStringProperty nameProperty() { return name; }
    public SimpleIntegerProperty minPlayersProperty() { return minPlayers; }
    public SimpleIntegerProperty maxPlayersProperty() { return maxPlayers; }
    public SimpleIntegerProperty averageTimeProperty() { return averageTime; }
    public SimpleStringProperty categoryProperty() { return category; }
}