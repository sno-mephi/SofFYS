package ru.idfedorov09.telegram.bot.service

import org.springframework.stereotype.Component
import ru.idfedorov09.telegram.bot.data.model.UserInfo
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import kotlin.jvm.optionals.getOrNull

@Component
class UserInfoService(
    private val teamRepository: TeamRepository,
) {
    fun getTeam(userInfo: UserInfo) = userInfo.teamId?.let { teamRepository.findById(it).getOrNull() }
}
