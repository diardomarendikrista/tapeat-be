package com.group2.TapEat.controller

import com.group2.TapEat.dto.OrderRequest
import com.group2.TapEat.model.Order
import com.group2.TapEat.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller untuk menangani Transaksi dari sisi Pelanggan/Kios.
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin("*")
class OrderController(private val orderService: OrderService) {

    /**
     * Mengambil seluruh riwayat pesanan (History), dengan seluruh status.
     */
    @GetMapping
    fun getAllOrders(): List<Order> = orderService.getAllOrders()

    /**
     * Mengambil detail satu pesanan.
     */
    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: Long): ResponseEntity<Order> {
        val order = orderService.getOrderById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(order)
    }

    /**
     * Endpoint Checkout untuk membuat pesanan baru.
     */
    @PostMapping
    fun createOrder(@RequestBody request: OrderRequest): ResponseEntity<Any> {
        return try {
            val order = orderService.createOrder(request)
            ResponseEntity.ok(order)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("message" to e.message))
        }
    }

    /**
     * Endpoint untuk membatalkan pesanan dan mengembalikan stok.
     */
    @PutMapping("/{id}/cancel")
    fun cancelOrder(@PathVariable id: Long): ResponseEntity<Order> {
        val cancelledOrder = orderService.cancelOrder(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(cancelledOrder)
    }
}
