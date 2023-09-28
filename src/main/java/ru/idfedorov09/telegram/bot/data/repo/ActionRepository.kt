

package ru.idfedorov09.telegram.bot.data.repo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.idfedorov09.telegram.bot.data.model.Action

interface ActionRepository : JpaRepository<Action, Long> {

    @Query("SELECT COUNT(a) + 1 FROM Action a WHERE a.teamId = :teamId AND a.problemId = :problemId AND a.action = 'SEND_ANSWER'")
    fun countAnswer(@Param("teamId") teamId: Long, @Param("problemId") problemId: Long): Long

    @Query(
        "SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Action a " +
            "WHERE a.teamId = :teamId AND a.problemId = :problemId AND a.action = 'SEND_ANSWER' AND a.isCorrectAnswer = false",
    )
    fun presenceOfIncorrectAnswers(@Param("teamId") teamId: Long, @Param("problemId") problemId: Long): Boolean

    @Query(
        "SELECT a.answer FROM Action a " +
            "WHERE a.teamId = :teamId AND a.problemId = :problemId AND a.action = 'SEND_ANSWER'",
    )
    fun findAnswersByTeamIdAndProblemId(
        @Param("teamId") teamId: Long,
        @Param("problemId") problemId: Long,
    ): List<String>
}
