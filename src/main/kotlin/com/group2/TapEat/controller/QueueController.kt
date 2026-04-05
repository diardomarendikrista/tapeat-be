package com.group2.TapEat.controller

import com.group2.TapEat.model.Order
import com.group2.TapEat.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller untuk mengelola Antrean Pesanan (Queue Display) dari sisi Dapur.
 */
@RestController
@RequestMapping("/api/queue")
@CrossOrigin("*")
class QueueController(private val orderService: OrderService) {

    /**
     * Mengambil daftar pesanan yang berstatus PENDING atau COOKING.
     */
    @GetMapping("/active")
    fun getActiveQueue(): List<Order> {
        return orderService.getActiveOrders()
    }

    /**
     * Mengupdate status pesanan (misal: PENDING -> COOKING -> READY).
     */
    @PutMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestParam("status") status: String
    ): ResponseEntity<Order> {
        val updatedOrder = orderService.updateOrderStatus(id, status) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updatedOrder)
    }
}
