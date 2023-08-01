package ru.idfedorov09.telegram.bot.util;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import redis.clients.jedis.Jedis;
import ru.idfedorov09.telegram.bot.service.UserQueue;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UpdatesUtil {

    @Autowired
    private Gson gson;

    @Autowired
    private Jedis jedis;

    private Logger log = LoggerFactory.getLogger(this.getClass());

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

    public String getChatKey(String chatId){
        return "cht_num_"+chatId;
    }

    private void removeKeyPrefix(String prefix){
        Set<String> keys = jedis.keys(prefix + "*");

        if (keys != null && !keys.isEmpty()) {
            jedis.del(keys.toArray(new String[0]));
        }

    }

    @PostConstruct
    public void clearAllQues(){
        log.info("Removing old ques data..");
        removeKeyPrefix("cht_num_");
        removeKeyPrefix(UserQueue.QUEUE_PREFIX);
        log.info("Removed.");
    }

}
