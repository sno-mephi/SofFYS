package ru.idfedorov09.telegram.bot.data.model

import jakarta.persistence.*

@Entity
@Table(name = "mc_table")
data class MC(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = -1,

    @Column(name = "name", columnDefinition = "TEXT")
    val name: String? = null,

    @Column(name = "limit")
    val limit: Long? = null,

    @ElementCollection
    @Column(name = "users", columnDefinition = "BIGINT")
    val users: MutableList<Long> = mutableListOf(),
)
