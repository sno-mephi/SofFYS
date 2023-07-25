package ru.idfedorov09.telegram.bot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.idfedorov09.telegram.bot.UpdatesHandler;
import ru.idfedorov09.telegram.bot.UpdatesSender;
import ru.idfedorov09.telegram.bot.util.UpdatesUtil;

@Component
public class UpdatesController extends UpdatesSender implements UpdatesHandler {

    /*
    Simple example of using handler.
     */

    @Autowired
    private UpdatesUtil updatesUtil;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private int cnt = 0;

    @Override
    //@Async("linearThread")
    //@Async("infinityThread")
    public void handle(TelegramLongPollingBot telegramBot, Update update) {
        String chatId = updatesUtil.getChatId(update);
        String message = updatesUtil.getText(update);
        if(chatId==null || message==null) return;
        try {
            log.info("handle update");
            cnt++;
            Message sent = telegramBot.execute(new SendMessage(chatId, ""+cnt));
            Thread.sleep(5 * 1000);
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(sent.getMessageId());
            editMessageText.setText("Test!");
            telegramBot.execute(editMessageText);
            cnt--;
        } catch (InterruptedException | TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }



}
