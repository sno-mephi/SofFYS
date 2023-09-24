package ru.idfedorov09.telegram.bot.data.repo

import org.springframework.data.jpa.repository.JpaRepository
import ru.idfedorov09.telegram.bot.data.model.Team

interface TeamRepository : JpaRepository<Team, Long>
