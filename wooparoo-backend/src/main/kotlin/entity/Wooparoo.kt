package org.example.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "wooparoo",
    indexes = [
        Index(name = "idx_wooparoo_name", columnList = "name", unique = true)
    ]
)
data class Wooparoo(

    @Id
    val id: Long,

    @Column(nullable = false, unique = true, length = 50)
    val name: String,

    @Column(nullable = false)
    val summonTime: Long
)
