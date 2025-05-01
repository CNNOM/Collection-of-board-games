package com.example.collection_board_games;

public class BoardGame {
    private String id;
    private String name;
    private String description;
    private String category;
    private int minPlayers;
    private int maxPlayers;
    private int averageTime;

    public BoardGame(String id, String name, String description, String category,
                     int minPlayers, int maxPlayers, int averageTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.averageTime = averageTime;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getAverageTime() { return averageTime; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public void setAverageTime(int averageTime) { this.averageTime = averageTime; }
}