package ru.idfedorov09.telegram.bot.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import redis.clients.jedis.Jedis
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class RedisService @Autowired constructor(
    private var jedis: Jedis,
) {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val port = 6711
    }

    /**
     * TODO: пофиксить эти штуки try{}..catch{}
     */
    fun getSafe(key: String): String? {
        try {
            try {
                return jedis.get(key)
            } catch (e: NullPointerException) {
                log.warn("Can't take value with key=$key from redis. Returning null")
                return null
            }
        } catch (e: Exception) {
            Thread.sleep(100)
            jedis = Jedis("51.250.68.42", port)
            return getSafe(key)
        }
    }

    fun getValue(key: String?): String? {
        try {
            return jedis[key]
        } catch (e: Exception) {
            Thread.sleep(100)
            jedis = Jedis("51.250.68.42", port)
            return getValue(key)
        }
    }

    fun setValue(key: String, value: String?) {
        try {
            value ?: run {
                jedis.del(key)
                return
            }
            jedis[key] = value
        } catch (e: Exception) {
            Thread.sleep(100)
            jedis = Jedis("51.250.68.42", port)
            setValue(key, value)
        }
    }

    fun setLastPollDate(date: LocalDateTime) {
        try {
            val dateTimeStr = date.format(formatter)
            jedis["last_poll_date"] = dateTimeStr
        } catch (e: Exception) {
            Thread.sleep(100)
            jedis = Jedis("51.250.68.42", port)
            setLastPollDate(date)
        }
    }

    fun lpop(key: String): String? {
        try {
            return jedis.lpop(key)
        } catch (e: Exception) {
            Thread.sleep(100)
            jedis = Jedis("51.250.68.42", port)
            return lpop(key)
        }
    }

    fun rpush(key: String, value: String) {
        try {
            jedis.rpush(key, value)
        } catch (e: Exception) {
            Thread.sleep(100)
            jedis = Jedis("51.250.68.42", port)
            return rpush(key, value)
        }
    }

    fun keys(key: String): Set<String> {
        try {
            return jedis.keys(key)
        } catch (e: Exception) {
            Thread.sleep(100)
            jedis = Jedis("51.250.68.42", port)
            return keys(key)
        }
    }

    fun del(key: String) {
        try {
            jedis.del(key)
        } catch (e: Exception) {
            Thread.sleep(100)
            jedis = Jedis("51.250.68.42", port)
            del(key)
        }
    }

    fun del(keys: Set<String>) {
        try {
            jedis.del(*keys.toTypedArray<String>())
        } catch (e: Exception) {
            Thread.sleep(100)
            jedis = Jedis("51.250.68.42", port)
            del(keys)
        }
    }

    fun getLastPollDate() = LocalDateTime.parse(getSafe("last_poll_date") ?: "1800-02-22 00:00:00", formatter)
}
