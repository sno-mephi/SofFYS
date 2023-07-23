package ru.idfedorov09.telegram.bot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.idfedorov09.telegram.bot.UpdatesHandler;
import ru.idfedorov09.telegram.bot.UpdatesSender;
import ru.idfedorov09.telegram.bot.util.UpdatesUtil;

@Controller
public class UpdatesController extends UpdatesSender implements UpdatesHandler {

    /*
    Simple example of using handler.
     */

    @Autowired
    private UpdatesUtil updatesUtil;

    @Override
    public void handle(TelegramLongPollingBot telegramBot, Update update) {
        String chatId = updatesUtil.getChatId(update);
        String message = updatesUtil.getText(update);
        if(chatId==null || message==null) return;
        try {
            telegramBot.execute(new SendMessage(chatId, message));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }



}
