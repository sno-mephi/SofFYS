package ru.idfedorov09.telegram.bot.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.idfedorov09.telegram.bot.config.BotContainer;

@Lazy
@Component
public class Executor extends TelegramLongPollingBot {

    @Autowired
    private BotContainer botContainer;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Executor(){
        log.info("Telegram method executor created.");
    }
    @Override
    public void onUpdateReceived(Update update) {
    }

    @Override
    public String getBotUsername() {
        return botContainer.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return botContainer.BOT_TOKEN;
    }

}
