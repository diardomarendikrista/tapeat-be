package com.group2.TapEat.repository

import com.group2.TapEat.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository untuk akses database tabel Products.
 */
@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    /**
     * Mengambil daftar produk yang masih aktif (belum di soft delete).
     */
    fun findByIsActiveTrue(): List<Product>
}
