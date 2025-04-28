// TaskDaoMongoImpl.java
package com.example.collection_board_games;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BoardGameDaoMongoImpl implements BoardGameDao {
    private final MongoCollection<Document> collection;

    public BoardGameDaoMongoImpl(String connectionString, String dbName, String collectionName) {
        MongoClient mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase(dbName);
        this.collection = database.getCollection(collectionName);
    }

    @Override
    public void addTask(BoardGame boardGame) {
        Document doc = new Document()
                .append("_id", boardGame.getId())
                .append("название", boardGame.getTitle())
                .append("описание", boardGame.getDescription())
                .append("категория", boardGame.getCategory())
                .append("приоритет", boardGame.getPriority())
                .append("статус", boardGame.getStatus())
                .append("dueDate", boardGame.getDueDate().toString());
        collection.insertOne(doc);
    }

    @Override
    public void updateTask(BoardGame boardGame) {
        collection.updateOne(
                new Document("_id", boardGame.getId()),
                new Document("$set", new Document()
                        .append("название", boardGame.getTitle())
                        .append("описание", boardGame.getDescription())
                        .append("категория", boardGame.getCategory())
                        .append("приоритет", boardGame.getPriority())
                        .append("статус", boardGame.getStatus())
                        .append("dueDate", boardGame.getDueDate().toString()))
        );
    }

    @Override
    public void deleteTask(int id) {
        collection.deleteOne(new Document("_id", id));
    }

    @Override
    public List<BoardGame> getAllTasks() {
        List<BoardGame> boardGames = new ArrayList<>();
        for (Document doc : collection.find()) {
            boardGames.add(new BoardGame(
                    doc.getInteger("_id"),
                    doc.getString("название"),
                    doc.getString("описание"),
                    doc.getString("категория"),
                    doc.getString("приоритет"),
                    doc.getString("статус"),
                    LocalDate.parse(doc.getString("dueDate"))
            ));
        }
        return boardGames;
    }
}