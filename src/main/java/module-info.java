module com.example.collection_board_games {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;


    requires org.mongodb.driver.core;
    requires java.net.http;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;


    opens com.example.collection_board_games to javafx.fxml;
    exports com.example.collection_board_games;
    exports com.example.collection_board_games.dao;
    opens com.example.collection_board_games.dao to javafx.fxml;

}