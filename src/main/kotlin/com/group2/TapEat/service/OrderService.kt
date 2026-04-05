package com.group2.TapEat.service

import com.group2.TapEat.dto.OrderRequest
import com.group2.TapEat.model.Order
import com.group2.TapEat.model.OrderItem
import com.group2.TapEat.repository.OrderRepository
import com.group2.TapEat.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service untuk menangani alur Transaksi Pesanan (Checkout, Cancel, Status).
 */
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) {
    /**
     * Mengambil detail pesanan berdasarkan ID.
     */
    fun getOrderById(id: Long): Order? {
        return orderRepository.findById(id).orElse(null)
    }

    /**
     * Mengambil daftar pesanan aktif untuk keperluan Layar Dapur (Queue).
     */
    fun getActiveOrders(): List<Order> {
        return orderRepository.findByStatusInOrderByCreatedAtAsc(listOf("PENDING", "COOKING"))
    }

    /**
     * Mengambil daftar pesanan yang sudah selesai (Status DELIVERED).
     */
    fun getDeliveredOrders(): List<Order> {
        return orderRepository.findByStatusInOrderByCreatedAtAsc(listOf("DELIVERED")).reversed()
    }

    /**
     * Mengambil daftar pesanan yang berstatus UNPAID (Menunggu konfirmasi kasir).
     */
    fun getUnpaidOrders(): List<Order> {
        return orderRepository.findByStatusInOrderByCreatedAtAsc(listOf("UNPAID"))
    }

    /**
     * Mengambil seluruh riwayat pesanan (History).
     */
    fun getAllOrders(): List<Order> {
        return orderRepository.findAll().sortedByDescending { it.createdAt }
    }

    /**
     * PENTING!!! Logika Utama Checkout:
     * 1. cek ketersediaan stok produk.
     * 2. kurangi stok produk secara otomatis.
     * 3. simpan harga master produk ke dalam price_at_purchase (historis).
     * 4. hitung total transaksi.
     */
    @Transactional
    fun createOrder(request: OrderRequest): Order {
        val order = Order(
            orderType = request.orderType,
            tableNumber = request.tableNumber,
            customerName = request.customerName,
            status = "UNPAID"
        )

        var totalAmount = 0.0

        for (itemRequest in request.items) {
            val product = productRepository.findById(itemRequest.productId)
                .orElseThrow { RuntimeException("Product not found: ${itemRequest.productId}") }

            // Validasi Stok
            if (product.stock < itemRequest.quantity) {
                throw RuntimeException("Insufficient stock for product: ${product.name}")
            }

            // Potong stok otomatis
            product.stock -= itemRequest.quantity
            productRepository.save(product)

            // Kunci harga historis ke dalam OrderItem
            val orderItem = OrderItem(
                product = product,
                quantity = itemRequest.quantity,
                priceAtPurchase = product.price, // Harga saat ini dikunci, jadi kalau ada edit harga ke item, tetap aman
                subtotal = product.price * itemRequest.quantity,
                order = order
            )
            order.addOrderItem(orderItem)
            totalAmount += orderItem.subtotal
        }

        order.totalAmount = totalAmount
        return orderRepository.save(order)
    }

    /**
     * Update status pesanan (misal: PENDING -> COOKING -> DELIVERED).
     * Jika status berubah menjadi CANCELLED atau keluar dari CANCELLED, stok akan disesuaikan.
     */
    @Transactional
    fun updateOrderStatus(id: Long, newStatus: String): Order? {
        val order = getOrderById(id) ?: return null
        val oldStatus = order.status

        // Case 1: Baru mau di-CANCEL (Restore Stok)
        if (newStatus == "CANCELLED" && oldStatus != "CANCELLED") {
            restoreStock(order)
        }
        // Case 2: Dari CANCEL mau diaktifkan lagi (Potong Stok)
        else if (oldStatus == "CANCELLED" && newStatus != "CANCELLED") {
            deductStock(order)
        }

        order.status = newStatus
        return orderRepository.save(order)
    }

    /**
     * Logika Pembatalan Pesanan:
     * status berubah jadi CANCELLED dan stok produk dikembalikan/ditambah lagi.
     */
    @Transactional
    fun cancelOrder(id: Long): Order? {
        val order = getOrderById(id) ?: return null
        if (order.status == "CANCELLED") return order

        restoreStock(order)
        order.status = "CANCELLED"
        return orderRepository.save(order)
    }

    /**
     * Fungsi Privat untuk mengembalikan stok saat pesanan dibatalkan.
     */
    private fun restoreStock(order: Order) {
        for (item in order.items) {
            val product = item.product
            product.stock += item.quantity
            productRepository.save(product)
        }
    }

    /**
     * Fungsi Privat untuk memotong stok saat pesanan diaktifkan kembali dari status CANCELLED.
     */
    private fun deductStock(order: Order) {
        // Pre-check stok untuk semua item
        for (item in order.items) {
            if (item.product.stock < item.quantity) {
                throw RuntimeException("Stok tidak cukup untuk mengaktifkan kembali pesanan: ${item.product.name}")
            }
        }
        
        // Jika semua cukup, baru potong
        for (item in order.items) {
            val product = item.product
            product.stock -= item.quantity
            productRepository.save(product)
        }
    }
}
