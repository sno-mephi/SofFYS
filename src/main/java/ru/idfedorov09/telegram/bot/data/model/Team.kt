package ru.idfedorov09.telegram.bot.data.model

import jakarta.persistence.*

@Entity
@Table(name = "team_table")
data class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = -1,

    @Column(name = "team_name", columnDefinition = "TEXT")
    val teamName: String? = null,

    @ElementCollection
    @Column(name = "current_problems", columnDefinition = "INTEGER[]")
    val problemsPool: List<Long> = mutableListOf(),

    @ElementCollection
    @Column(name = "completed_problems", columnDefinition = "INTEGER[]")
    val completedProblems: List<Long> = mutableListOf(),

    /**
     * Хэш тг картинки доски команды
     */
    @Column(name = "team_name", columnDefinition = "TEXT")
    val lastBoardCache: String? = null
)
