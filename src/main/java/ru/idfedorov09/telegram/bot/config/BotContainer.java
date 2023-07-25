package ru.idfedorov09.telegram.bot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ru.idfedorov09.telegram.bot.UpdatesHandler;

@Component
@PropertySource("application.properties")
public class BotContainer {

    @Value("${telegram.bot.token}")
    public String BOT_TOKEN;

    @Value("${telegram.bot.name:idfedorov09_bot}")
    public String BOT_NAME;

    @Value("${telegram.bot.reconnect-pause:1000}")
    public int RECONNECT_PAUSE;

    @Autowired
    public UpdatesHandler updatesHandler;

}
