package com.example.collection_board_games;

public class PlayerStats {
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
