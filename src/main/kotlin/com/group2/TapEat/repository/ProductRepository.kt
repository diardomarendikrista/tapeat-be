package com.group2.TapEat.repository

import com.group2.TapEat.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository untuk akses database tabel Products.
 */
@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    /**
     * Mengambil daftar produk berdasarkan filter pencarian (nama, kategori, dan stok).
     */
    @Query("""
        SELECT p FROM Product p 
        WHERE p.isActive = true 
        AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) 
        AND (:category IS NULL OR LOWER(p.category) = LOWER(:category)) 
        AND (:availableOnly = false OR p.stock > 0)
    """)
    fun findFiltered(
        @Param("name") name: String?,
        @Param("category") category: String?,
        @Param("availableOnly") availableOnly: Boolean
    ): List<Product>
}


