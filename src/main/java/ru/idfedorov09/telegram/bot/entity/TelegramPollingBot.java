package ru.idfedorov09.telegram.bot.entity;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.idfedorov09.telegram.bot.config.BotContainer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ConditionalOnProperty(name = "telegram.bot.interaction-method", havingValue = "polling", matchIfMissing = true)
public class TelegramPollingBot extends TelegramLongPollingBot {

    @Autowired
    private BotContainer botContainer;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public TelegramPollingBot() {
        log.info("Polling starting");
    }

    @Override
    public void onUpdateReceived(Update update) {

        new Thread(()->{
            long threadId = Thread.currentThread().getId();
            String threadName = "upd-" + threadId;
            Thread.currentThread().setName(threadName);
            botContainer.updatesHandler.handle(this, update);
        }).start();

    }

    @Override
    public String getBotUsername() {
        return botContainer.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return botContainer.BOT_TOKEN;
    }

    @PostConstruct
    public void botConnect(){
        TelegramBotsApi telegramBotsApi = null;

        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        }catch (TelegramApiException e) {
            System.out.println("Can't create API: "+e);
            botConnect();
        }

        try {
            telegramBotsApi.registerBot(this);
            System.out.println("TelegramAPI started. Look for messages");
        } catch (TelegramApiException e) {
            System.out.println("Cant Connect. Pause " + botContainer.RECONNECT_PAUSE / 1000 + "sec and try again. Error: " + e.getMessage());
            try {
                Thread.sleep(botContainer.RECONNECT_PAUSE);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return;
            }
            botConnect();
        }
    }



}