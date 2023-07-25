package ru.idfedorov09.telegram.bot.entity;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.idfedorov09.telegram.bot.config.BotContainer;

@Controller
@ConditionalOnProperty(name = "telegram.bot.interaction-method", havingValue = "webhook", matchIfMissing = false)
public class TelegramWebhookBot{

    @Autowired
    private BotContainer botContainer;

    @Autowired
    private Executor executor;

    @Autowired
    private Gson gson;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public TelegramWebhookBot(){
        log.info("Webhook starting..");
    }

    @PostConstruct
    public void postConstruct(){
        log.info("ok, started.");
    }

    @PostMapping("/")
    @ResponseStatus(value = HttpStatus.OK)
    public void handler(@RequestBody String jsonUpdate){
        Update update = gson.fromJson(jsonUpdate, Update.class);
        log.info("Update received: "+update);
        botContainer.updatesHandler.handle(executor, update);
    }

}
