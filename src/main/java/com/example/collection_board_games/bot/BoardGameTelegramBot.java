package com.example.collection_board_games.bot;

import com.example.collection_board_games.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class BoardGameTelegramBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private final Controller controller;

    private Map<Long, DialogState> userStates = new HashMap<>();
    private Map<Long, String> userData = new HashMap<>();

    public BoardGameTelegramBot(String botToken, String botUsername, Controller controller) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.controller = controller;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));

            DialogState currentState = userStates.getOrDefault(chatId, DialogState.IDLE);

            switch (currentState) {
                case IDLE:
                    handleIdleState(messageText, chatId, message);
                    break;
                case WAITING_FOR_GAME_NAME:
                    handleWaitingForGameNameState(messageText, chatId, message);
                    break;
            }

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleIdleState(String messageText, long chatId, SendMessage message) {
        if (messageText.startsWith("/start")) {
            message.setText("Привет! Я бот для управления коллекцией настольных игр. Вот что я могу сделать:\n" +
                    "/show_stats - Показать статистику побед для игры\n" +
                    "/show_games - Показать все доступные игры");
        } else if (messageText.startsWith("/show_stats")) {
            String gamesList = controller.getAllGamesForBot();
            message.setText("Пожалуйста, выберите игру из списка:\n" + gamesList);
            userStates.put(chatId, DialogState.WAITING_FOR_GAME_NAME);
        } else if (messageText.startsWith("/show_games")) {
            String gamesList = controller.getAllGamesForBot();
            message.setText(gamesList);
        } else {
            message.setText("Неизвестная команда. Используйте /start для списка доступных команд.");
        }
    }

    private void handleWaitingForGameNameState(String messageText, long chatId, SendMessage message) {
        String stats = controller.showWinStatisticsForBot(messageText);
        message.setText(stats);
        userStates.remove(chatId);
        userData.remove(chatId);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}


