package ru.idfedorov09.telegram.bot.fetcher

import org.springframework.stereotype.Component
import ru.idfedorov09.telegram.bot.flow.InjectData

@Component
class TestFetcher : GeneralFetcher() {

    @InjectData
    fun doFetch(a: String): String {
        println(a)
        return "test fetcher!"
    }
}
