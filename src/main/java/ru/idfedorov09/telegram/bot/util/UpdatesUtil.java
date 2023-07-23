package ru.idfedorov09.telegram.bot.util;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UpdatesUtil {

    @Autowired
    private Gson gson;

    public String getChatId(Update update){
        String chatId = getByPattern(update, "\"chat\"\\s*:\\s*\\{\"id\"\\s*:\\s*(-?\\d+)");
        return chatId;
    }

    public String getText(Update update){
        String text = getByPattern(update, "\"text\"\\s*:\\s*\"(.+?)\"");
        if(text==null) text = getByPattern(update, "\"caption\"\\s*:\\s*\"(.+?)\"");

        return text;
    }

    public String getByPattern(Update update, String pattern){
        String updateJson = gson.toJson(update);
        String result = null;
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(updateJson);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

}
