package com.group2.TapEat.repository

import com.group2.TapEat.model.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository untuk akses database tabel Orders.
 * source belajar : https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/repository/JpaRepository.html
 */
@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    /**
     * Mengambil antrean pesanan berdasarkan status tertentu.
     * Diurutkan dari yang paling lama.
     */
    fun findByStatusInOrderByCreatedAtAsc(statuses: List<String>): List<Order>
}
