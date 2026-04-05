package com.group2.TapEat.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Entity untuk menyimpan data Transaksi (Order).
 */
@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "order_type", nullable = false)
    var orderType: String, // "DINE_IN" atau "TAKEAWAY"

    @Column(name = "table_number")
    var tableNumber: String? = null, // Nomor meja (jika DINE_IN)

    @Column(name = "customer_name")
    var customerName: String? = null, // Nama pelanggan (jika TAKEAWAY)

    @Column(name = "total_amount", nullable = false)
    var totalAmount: Double = 0.0, // Total harga pesanan

    @Column(nullable = false)
    var status: String = "PENDING", // Status: PENDING, COOKING, READY, CANCELLED

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    var items: MutableList<OrderItem> = mutableListOf()
) {
    /**
     * Helper untuk menambahkan item ke dalam order agar relasinya sinkron.
     */
    fun addOrderItem(item: OrderItem) {
        items.add(item)
        item.order = this
    }
}
