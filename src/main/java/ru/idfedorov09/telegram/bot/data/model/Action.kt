package ru.idfedorov09.telegram.bot.data.model

import jakarta.persistence.*
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import java.time.LocalDateTime

@Entity
@Table(name = "actions_table")
data class Action(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "team_id")
    val teamId: Long? = null,

    @Column(name = "time")
    val time: LocalDateTime? = null,

    @Enumerated(EnumType.STRING) // Сохранять как строку
    @Column(name = "action")
    val action: ResponseAction? = null,

    @Column(name = "problem_id")
    val problemId: Long? = null,

    @Column(name = "answer")
    val answer: String? = null,

    @Column(name = "is_correct_answer")
    val isCorrectAnswer: Boolean? = null,

    @Column(name = "correct_answer_attempt") // заполняется только для действия аппеляция
    val correctAnswerAttempt: Long? = null,
)
