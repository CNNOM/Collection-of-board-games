package com.example.collection_board_games.dao;

import com.example.collection_board_games.BoardGame;
import com.example.collection_board_games.GameSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BoardGameDaoMongoImpl implements BoardGameDao {
    private final MongoCollection<Document> gamesCollection;
    private final MongoCollection<Document> sessionsCollection;
    private final MongoClient mongoClient;

    public BoardGameDaoMongoImpl(String connectionString, String databaseName, String collectionPrefix) {
        this.mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.gamesCollection = database.getCollection(collectionPrefix + "_games");
        this.sessionsCollection = database.getCollection(collectionPrefix + "_sessions");
    }

    @Override
    public List<BoardGame> getAllGames() {
        return gamesCollection.find()
                .into(new ArrayList<>())
                .stream()
                .map(this::documentToBoardGame)
                .collect(Collectors.toList());
    }

    @Override
    public List<GameSession> getGameHistory() {
        return sessionsCollection.find()
                .into(new ArrayList<>())
                .stream()
                .map(this::documentToGameSession)
                .collect(Collectors.toList());
    }

    @Override
    public void addGame(BoardGame game) {
        Document doc = new Document()
                .append("name", game.getName())
                .append("description", game.getDescription())
                .append("category", game.getCategory())
                .append("minPlayers", game.getMinPlayers())
                .append("maxPlayers", game.getMaxPlayers())
                .append("averageTime", game.getAverageTime());
        gamesCollection.insertOne(doc);
    }

    @Override
    public void addGameSession(GameSession session) {
        String playersString = String.join(", ", session.getPlayers());

        Document doc = new Document()
                .append("gameId", session.getGameId())
                .append("gameName", session.getGameName())
                .append("date", Date.from(session.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .append("players", playersString)
                .append("winner", session.getWinner());
        sessionsCollection.insertOne(doc);
    }

    @Override
    public List<BoardGame> findGamesByPlayersAndTime(int players, int maxTime) {
        Bson filter = Filters.and(
                Filters.lte("minPlayers", players),
                Filters.gte("maxPlayers", players),
                Filters.lte("averageTime", maxTime)
        );
        return gamesCollection.find(filter)
                .into(new ArrayList<>())
                .stream()
                .map(this::documentToBoardGame)
                .collect(Collectors.toList());
    }

    @Override
    public void close() {
        mongoClient.close();
    }

    private BoardGame documentToBoardGame(Document doc) {
        return new BoardGame(
                doc.getObjectId("_id").toString(),
                doc.getString("name"),
                doc.getString("description"),
                doc.getString("category"),
                doc.getInteger("minPlayers", 2),
                doc.getInteger("maxPlayers", 4),
                doc.getInteger("averageTime", 30)
        );
    }

    @Override
    public List<GameSession> getRecentSessions(int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        Date cutoff = Date.from(cutoffDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return sessionsCollection.find(Filters.gte("date", cutoff))
                .sort(Sorts.descending("date"))
                .into(new ArrayList<>())
                .stream()
                .map(this::documentToGameSession)
                .collect(Collectors.toList());
    }

    private GameSession documentToGameSession(Document doc) {
        LocalDate date = doc.getDate("date").toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        String playersString = doc.getString("players");
        List<String> players = Arrays.stream(playersString.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        return new GameSession(
                doc.getObjectId("_id").toString(),
                doc.getString("gameId"),
                doc.getString("gameName"),
                date,
                players,
                doc.getString("winner")
        );
    }


    @Override
    public void updateGame(BoardGame game) {
        Document update = new Document("$set", new Document()
                .append("name", game.getName())
                .append("description", game.getDescription())
                .append("category", game.getCategory())
                .append("minPlayers", game.getMinPlayers())
                .append("maxPlayers", game.getMaxPlayers())
                .append("averageTime", game.getAverageTime()));

        gamesCollection.updateOne(Filters.eq("_id", new ObjectId(game.getId())), update);
    }

    @Override
    public void deleteGame(String id) {
        gamesCollection.deleteOne(Filters.eq("_id", new ObjectId(id)));
    }

    @Override
    public BoardGame getGameById(String id) {
        Document doc = gamesCollection.find(Filters.eq("_id", new ObjectId(id))).first();
        return documentToBoardGame(doc);
    }
}