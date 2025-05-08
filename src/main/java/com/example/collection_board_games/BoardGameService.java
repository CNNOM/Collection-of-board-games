package com.example.collection_board_games;

import com.example.collection_board_games.dao.BoardGameDao;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BoardGameService {
    private final BoardGameDao boardGameDao;

    public BoardGameService(BoardGameDao boardGameDao) {
        this.boardGameDao = boardGameDao;
    }

    public void addGame(BoardGame boardGame) {
        boardGameDao.addGame(boardGame);
    }

    public void updateGame(BoardGame boardGame) {
        boardGameDao.updateGame(boardGame);
    }

    public void deleteGame(String id) {
        boardGameDao.deleteGame(id);
    }

    public List<BoardGame> getAllGames() {
        return boardGameDao.getAllGames();
    }

    public List<BoardGame> searchGames(String searchText) {
        String searchLower = searchText.toLowerCase();
        return boardGameDao.getAllGames().stream()
                .filter(game ->
                        game.getName().toLowerCase().contains(searchLower) ||
                                game.getDescription().toLowerCase().contains(searchLower) ||
                                game.getCategory().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
    }

//    public List<BoardGame> getRecentlyPlayedGames(int days) {
//        LocalDate cutoffDate = LocalDate.now().minusDays(days);
//        return boardGameDao.getGameHistory().stream()
//                .filter(session -> session.getDate().isAfter(cutoffDate))
//                .map(session -> boardGameDao.getGameById(session.getGameId()))
//                .distinct()
//                .collect(Collectors.toList());
//    }
}