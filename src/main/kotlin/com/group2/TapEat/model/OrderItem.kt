package com.group2.TapEat.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

/**
 * Entity untuk menyimpan Detail Item dalam sebuah Order.
 * Data di sini mengunci harga (priceAtPurchase) saat transaksi dilakukan.
 */
@Entity
@Table(name = "order_items")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // Relasi ke tabel orders
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    var order: Order? = null,

    // Relasi ke produk yang dipesan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false)
    var quantity: Int, // Jumlah yang dipesan

    @Column(name = "price_at_purchase", nullable = false)
    var priceAtPurchase: Double, // Harga saat checkout (historis)

    @Column(nullable = false)
    var subtotal: Double // quantity * priceAtPurchase
)
