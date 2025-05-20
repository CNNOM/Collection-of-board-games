package com.example.collection_board_games;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public BoardGame(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("category") String category,
            @JsonProperty("minPlayers") int minPlayers,
            @JsonProperty("maxPlayers") int maxPlayers,
            @JsonProperty("averageTime") int averageTime) {
        this.id.set(id);
        this.name.set(name);
        this.description.set(description);
        this.category.set(category);
        this.minPlayers.set(minPlayers);
        this.maxPlayers.set(maxPlayers);
        this.averageTime.set(averageTime);
    }

    // Геттеры
    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getDescription() { return description.get(); }
    public String getCategory() { return category.get(); }
    public int getMinPlayers() { return minPlayers.get(); }
    public int getMaxPlayers() { return maxPlayers.get(); }
    public int getAverageTime() { return averageTime.get(); }

    // Сеттеры
    public void setId(String id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setDescription(String description) { this.description.set(description); }
    public void setCategory(String category) { this.category.set(category); }
    public void setMinPlayers(int minPlayers) { this.minPlayers.set(minPlayers); }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers.set(maxPlayers); }
    public void setAverageTime(int averageTime) { this.averageTime.set(averageTime); }

    @Override
    public String toString() {
        return "BoardGame{" +
                "name='" + getName() + '\'' +
                ", category='" + getCategory() + '\'' +
                ", players=" + getMinPlayers() + "-" + getMaxPlayers() +
                ", time=" + getAverageTime() +
                '}';
    }
}
