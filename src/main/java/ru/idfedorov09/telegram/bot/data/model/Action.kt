package ru.idfedorov09.telegram.bot.data.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "actions_table")
data class Action(
    @Column(name = "team_id")
    val teamId: Long? = null,

    @Column(name = "time")
    val time: LocalDateTime,

    @Column(name = "action", columnDefinition = "TEXT")
    val action: String? = null,

    @Column(name = "problem_id")
    val problemId: Long? = null,

    @Column(name = "is_correct_answer")
    val isCorrectAnswer: Boolean? = null,

    @Column(name = "correct_answer_attempt") // заполняется только для действия аппеляция
    val correctAnswerAttempt: Long? = null,
)
