package ru.idfedorov09.telegram.bot.data.model

import jakarta.persistence.*

/**
 * Содержит информацию об ответе пользователя
 */
@Entity
@Table(name = "problems_table")
data class Problem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = -1,

    @Column(name = "name", columnDefinition = "TEXT")
    val name: String? = null,

    @ElementCollection
    @Column(name = "answers", columnDefinition = "TEXT[]")
    val answers: List<String> = mutableListOf(),
) {
    /**
     * проверяет, является ли anotherAnswer ответом
     */
    fun isAnswer(anotherAnswer: String) = anotherAnswer in answers
}