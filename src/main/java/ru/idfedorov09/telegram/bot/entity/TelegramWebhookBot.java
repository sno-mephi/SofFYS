package ru.idfedorov09.telegram.bot.entity;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    public TelegramWebhookBot(){
        System.out.println("webhook"); //логи
    }

    @PostMapping("/")
    public ResponseEntity<String> handler(@RequestBody String jsonUpdate){
        Update update = gson.fromJson(jsonUpdate, Update.class);

        botContainer.updatesHandler.handle(executor, update);

        return ResponseEntity.ok("");
    }
}
