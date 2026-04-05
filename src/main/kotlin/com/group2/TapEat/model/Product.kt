package com.group2.TapEat.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*

/**
 * Entity untuk menyimpan data Produk (Menu).
 */
@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String, // Nama menu

    @Column(nullable = false)
    var price: Double, // Harga menu

    @Column(nullable = false)
    var stock: Int, // Stok yang tersedia

    @Column(name = "image_url")
    @JsonIgnore
    var imageUrl: String? = null, // Nama file gambar yang disimpan di /uploads (disembunyikan dari JSON)

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true, // Fitur soft delete

    @Column(nullable = false)
    var category: String = "Uncategorized" // Kategori produk (Makanan, Minuman, dsb)
) {
    /**
     * Properti virtual untuk JSON agar otomatis menyertakan folder 'uploads/'.
     */
    @get:JsonProperty("imageUrl")
    val fullImageUrl: String?
        get() = if (imageUrl != null) "uploads/$imageUrl" else null
}
