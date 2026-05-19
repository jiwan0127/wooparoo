package org.example.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "cross_rate",
    indexes = [
        Index(name = "idx_cross_lr", columnList = "left_id,right_id"),
        Index(name = "idx_cross_lr_result", columnList = "left_id,right_id,result_id"),
        Index(name = "idx_cross_type_event", columnList = "cross_type,luck_up_event")
    ]
)
data class CrossRate(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "left_id", nullable = false)
    val left: Wooparoo,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "right_id", nullable = false)
    val right: Wooparoo,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    val result: Wooparoo,

    @Column(nullable = false, precision = 7, scale = 4)
    val rate: BigDecimal,

    @Column(name = "cross_type", nullable = false)
    val crossType: Int,

    @Column(name = "luck_up_event", nullable = false)
    val luckUpEvent: Boolean
)
