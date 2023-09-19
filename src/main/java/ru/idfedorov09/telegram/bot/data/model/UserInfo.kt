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
    val tui: String = "default_user_id",

    @Column(name = "team_id")
    val teamId: Int? = null,

    @Column(name = "is_captain")
    val isCaptain: Boolean = false,

    @Column(name = "full_name", columnDefinition = "TEXT")
    val teamName: String = "default_team_name",

    @Column(name = "study_group", columnDefinition = "TEXT")
    val studyGroup: String = "default_group",
) {
    // TODO: дописать метод для получения команды текущего юзера
    fun team(): Team? {
        return null
    }
}
