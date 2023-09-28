package ru.idfedorov09.telegram.bot.data.model

import jakarta.persistence.*

/**
 * Содержит информацию об ответе пользователя
 */
@Entity
@Table(name = "users_table")
data class UserInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = -1,

    @Column(name = "true_user_id", columnDefinition = "TEXT")
    val tui: String? = null,

    @Column(name = "team_id")
    val teamId: Long? = null,

    @Column(name = "is_captain")
    val isCaptain: Boolean = false,

    @Column(name = "full_name", columnDefinition = "TEXT")
    val fullName: String? = null,

    @Column(name = "study_group", columnDefinition = "TEXT")
    val studyGroup: String? = null,
) {
    /**
     * проверяет, является ли юзер Иваном
     */
    fun isIvan() = tui == "920061911"
}
