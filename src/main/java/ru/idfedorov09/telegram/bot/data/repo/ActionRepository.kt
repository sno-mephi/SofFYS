

package ru.idfedorov09.telegram.bot.data.repo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.idfedorov09.telegram.bot.data.model.Action

interface ActionRepository : JpaRepository<Action, Long> {

    @Query("SELECT COUNT(a) + 1 FROM Action a WHERE a.teamId = :teamId AND a.problemId = :problemId AND a.action = 'SEND_ANSWER'")
    fun countAnswer(@Param("teamId") teamId: Long, @Param("problemId") problemId: Long): Long

    @Query(
        "SELECT a " +
            "FROM Action a " +
            "WHERE a.teamId = :teamId AND a.problemId = :problemId AND a.action = 'SEND_ANSWER' AND a.isCorrectAnswer = true",
    )
    fun findCorrectAnswers(@Param("teamId") teamId: Long, @Param("problemId") problemId: Long): List<Action>

    @Query(
        "SELECT a.answer FROM Action a " +
            "WHERE a.teamId = :teamId AND a.problemId = :problemId AND a.action = 'SEND_ANSWER' ORDER BY a.time",
    )
    fun findAnswersByTeamIdAndProblemId(
        @Param("teamId") teamId: Long,
        @Param("problemId") problemId: Long,
    ): List<String>
}
