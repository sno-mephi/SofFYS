package ru.idfedorov09.telegram.bot.util

import com.google.gson.Gson
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import redis.clients.jedis.Jedis
import ru.idfedorov09.telegram.bot.service.RedisService
import ru.idfedorov09.telegram.bot.service.UserQueue
import java.util.regex.Pattern

@Component
class UpdatesUtil {
    @Autowired
    private lateinit var gson: Gson

    @Autowired
    private lateinit var redisService: RedisService

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    fun getChatId(update: Update?): String? {
        return getByPattern(update, "\"chat\"\\s*:\\s*\\{\"id\"\\s*:\\s*(-?\\d+)")
    }

    fun getText(update: Update?): String? {
        var text = getByPattern(update, "\"text\"\\s*:\\s*\"(.+?)\"")
        if (text == null) text = getByPattern(update, "\"caption\"\\s*:\\s*\"(.+?)\"")
        return text
    }

    fun getByPattern(update: Update?, pattern: String): String? {
        val updateJson = gson.toJson(update)
        var result: String? = null
        val r = Pattern.compile(pattern)
        val matcher = r.matcher(updateJson)
        if (matcher.find()) {
            result = matcher.group(1)
        }
        return result
    }

    fun getChatKey(chatId: String): String {
        return "cht_num_$chatId"
    }

    private fun removeKeyPrefix(prefix: String) {
        val keys = redisService.keys("$prefix*")
        if (keys != null && keys.isNotEmpty()) {
            redisService.del(keys)
        }
    }

    @PostConstruct
    fun clearAllQues() {
        log.info("Removing old ques data..")
        removeKeyPrefix("cht_num_")
        removeKeyPrefix(UserQueue.QUEUE_PREFIX)
        log.info("Removed.")
    }
}
