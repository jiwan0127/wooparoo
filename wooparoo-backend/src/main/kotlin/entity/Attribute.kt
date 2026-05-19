package org.example.entity

import jakarta.persistence.*

@Entity
@Table(name = "attribute")
data class Attribute(

    @Id
    val id: Long,

    @Column(nullable = false, unique = true, length = 30)
    val name: String
)
