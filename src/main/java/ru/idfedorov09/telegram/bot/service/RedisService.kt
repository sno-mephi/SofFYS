package ru.idfedorov09.telegram.bot.service

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import redis.clients.jedis.Jedis

@Service
class RedisService @Autowired constructor(private val jedis: Jedis, private val gson: Gson) {
    fun <T> getValue(key: String?, type: Class<T>?): T {
        val jsonValue = jedis[key]
        return gson.fromJson(jsonValue, type)
    }

    fun setValue(key: String, value: Any?) {
        val jsonValue = gson.toJson(value)
        jedis[key] = jsonValue
    }

    fun getValue(key: String?): String? {
        return jedis[key]
    }

    fun setValue(key: String, value: String?) {
        jedis[key] = value
    }
}
