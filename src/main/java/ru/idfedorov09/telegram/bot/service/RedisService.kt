package ru.idfedorov09.telegram.bot.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class RedisService @Autowired constructor(
    private val jedis: Jedis
) {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    fun getSafe(key: String) = run {
        try {
            jedis.get(key)
        } catch (e: NullPointerException) {
            log.warn("Can't take value with key=$key from redis. Returning null")
            null
        }
    }

    fun getValue(key: String?): String? {
        return jedis[key]
    }

    fun setValue(key: String, value: String?) {
        value ?: run {
            jedis.del(key)
            return
        }
        jedis[key] = value
    }

    fun setLastPollDate(date: LocalDateTime) {
        val dateTimeStr = date.format(formatter)
        jedis["last_poll_date"] = dateTimeStr
    }

    fun lpop(key: String): String? {
        return jedis.lpop(key)
    }

    fun rpush(key: String, value: String) {
        jedis.rpush(key, value)
    }

    fun keys(key: String): Set<String> {
        return jedis.keys(key)
    }

    fun del(key: String) {
        jedis.del(key)
    }

    fun del(keys: Set<String>) {
        jedis.del(*keys.toTypedArray<String>())
    }

    fun getLastPollDate() = LocalDateTime.parse(getSafe("last_poll_date") ?: "1800-02-22 00:00:00", formatter)
}
