
package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService

@Component
class PoolToCompletedFetcher(
    private val redisService: RedisService,
    private val teamRepository: TeamRepository,
    private val userInfoRepository: UserInfoRepository,
) : GeneralFetcher() {
    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
        exp: ExpContainer,
        update: Update,
    ) {
        if (!exp.END_GAME_NOW) {
            return
        }

        teamRepository.findAll()
            .forEach { team ->
                val allProblems = team.completedProblems
                allProblems.addAll(team.problemsPool)
                teamRepository.save(team.copy(completedProblems = allProblems))
            }
    }
}
