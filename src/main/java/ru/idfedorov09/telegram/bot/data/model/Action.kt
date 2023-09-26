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

    @Column(name = "correct_answer")
    val correctAnswer: Boolean? = null,

    @Column(name = "correct_answer_number") // заполняется только для действия аппеляция
    val correctAnswerNumber: Long? = null,
)
