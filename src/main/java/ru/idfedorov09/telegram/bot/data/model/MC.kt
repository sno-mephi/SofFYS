package ru.idfedorov09.telegram.bot.data.model

import jakarta.persistence.*

@Entity
@Table(name = "mc_table")
data class MC(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "name", columnDefinition = "TEXT")
    val name: String? = null,

    @Column(name = "max_users_count")
    val maxUsersCount: Long? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "users", columnDefinition = "BIGINT")
    val users: MutableList<Long> = mutableListOf(),
)
