package ru.idfedorov09.telegram.bot.fetcher

import ru.idfedorov09.telegram.bot.flow.InjectData

class TestFetcher : GeneralFetcher() {

    private var num = 0

    @InjectData
    fun doFetch(a: String?): String {
        num++
        println(a)
        return "test fetcher: $num"
    }
}
