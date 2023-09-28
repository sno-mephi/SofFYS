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
    val id: Long? = null,

    @Column(name = "category", columnDefinition = "TEXT")
    val category: String? = null,

    @Column(name = "cost")
    val cost: Long? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "answers", columnDefinition = "TEXT")
    val answers: MutableList<String> = mutableListOf(),

    /**
     * Хэш тг картинки условия задачи
     */
    @Column(name = "problem_hash", columnDefinition = "TEXT")
    val problemHash: String? = null,
) {
    /**
     * проверяет, является ли anotherAnswer ответом
     */
    fun isAnswer(anotherAnswer: String) = anotherAnswer in answers
}
