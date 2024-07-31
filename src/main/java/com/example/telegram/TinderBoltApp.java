package com.example.telegram;

import com.example.telegram.ChatGPTService;
import com.example.telegram.DialogMode;
import com.example.telegram.MultiSessionTelegramBot;
import com.example.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "tinder_with_ai_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = ""; //TODO: добавь токен бота в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;

    private ArrayList<String> chatHistory = new ArrayList<>();

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {

        String message = getMessageText();

        if (message.equals("/start")) {
            sendPhotoMessage("main");
            sendTextMessage(loadMessage("main"));
            currentMode = DialogMode.MAIN;

            showMainMenu("Generate main Menu", "/start", "Generate Tinder-profile \uD83D\uDE0E",
                    "/profile", "Message to start with \uD83E\uDD70", "/opener", "Chat from your name \uD83D\uDE08",
                    "/message", "Chat with celebrities \uD83D\uDD25", "/date", "Ask question to ChatGPT \uD83E\uDDE0",
                    "/gpt");
        }


        //GPT
        if (message.equals("/gpt")) {
            sendPhotoMessage("gpt");
            sendTextMessage("Ask your question to *ChatGPT:*");
            currentMode = DialogMode.GPT;
            return;
        }

        if (currentMode == DialogMode.GPT) {
            Message upd_msg = sendTextMessage("GPT is generating an answer. Wait a little...");
            String answer = chatGPT.sendMessage("Answer the following question: ", message);
            updateTextMessage(upd_msg, answer);
        }


        //DATE
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            sendTextButtonsMessage(loadMessage("date"),
                    "Ariana Grande", "date_grande",
                    "Margo Robbie", "date_robbie",
                    "Zendaya", "date_zendaya",
                    "Ryan Gosling", "date_gosling",
                    "Tom Hardy", "date_hardy");
            return;
        }

        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Send your chat", "Next message", "message_next",
                    "Invite to a date", "message_date");
            return;
        }


        if (currentMode==DialogMode.DATE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("Good choice\uD83D\uDD25 \n Try to go on a date with him/her with 5 messages!");

                String prompt = loadPrompt(query);
                chatGPT.setPrompt(prompt);
                return;
            }

            Message upd_msg = sendTextMessage("Partner is typing. Wait a little...");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(upd_msg, answer);
            return;
        }



        if (currentMode == DialogMode.MESSAGE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String prompt = loadPrompt(query);
                Message upd_msg = sendTextMessage("Partner is typing. Wait a little...");
                String answer = chatGPT.sendMessage(prompt, String.join("\n\n", chatHistory));
                updateTextMessage(upd_msg, answer);
                return;
            }

            chatHistory.add(message);
            return;
        }

        if (message.equals("Hello")) {
            sendTextMessage("Hello, happy to see you!");
        }




    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
