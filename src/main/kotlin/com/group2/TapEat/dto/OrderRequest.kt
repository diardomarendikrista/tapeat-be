package com.group2.TapEat.dto

/**
 * Data Transfer Object untuk menerima request pesanan baru.
 */
data class OrderRequest(
    val orderType: String, // "DINE_IN" atau "TAKEAWAY"
    val tableNumber: String?,
    val customerName: String?,
    val items: List<OrderItemRequest>
)

/**
 * Representasi item di keranjang belanja.
 */
data class OrderItemRequest(
    val productId: Long,
    val quantity: Int
)
