package com.group2.TapEat.service

import com.group2.TapEat.model.Product
import com.group2.TapEat.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * Service untuk manajemen Logika Katalog Produk (Admin & User).
 */
@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val fileStorageService: FileStorageService
) {
    /**
     * Mengambil daftar produk aktif dengan filter dinamis (nama, kategori, dan ketersediaan).
     */
    fun getAllProducts(name: String?, category: String?, availableOnly: Boolean): List<Product> {
        return productRepository.findFiltered(name, category, availableOnly)
    }

    /**
     * Mencari satu produk berdasarkan ID.
     */
    fun getProductById(id: Long): Product? {
        return productRepository.findById(id).orElse(null)
    }

    /**
     * Menambahkan produk baru berserta upload gambarnya.
     */
    fun createProduct(name: String, price: Double, stock: Int, category: String, imageFile: MultipartFile?): Product {
        val imageUrl = imageFile?.let { fileStorageService.storeFile(it) }
        val product = Product(
            name = name,
            price = price,
            stock = stock,
            category = category,
            imageUrl = imageUrl,
            isActive = true
        )
        return productRepository.save(product)
    }

    /**
     * Mengupdate detail produk. Jika parameter gambar dikirim, gambar lama akan diganti.
     */
    fun updateProduct(id: Long, name: String, price: Double, stock: Int, category: String, imageFile: MultipartFile?): Product? {
        val product = getProductById(id) ?: return null
        product.name = name
        product.price = price
        product.stock = stock
        product.category = category
        imageFile?.let {
            product.imageUrl = fileStorageService.storeFile(it)
        }
        return productRepository.save(product)
    }

    /**
     * Soft delete produk dengan mengubah status isActive menjadi false.
     */
    fun softDeleteProduct(id: Long): Boolean {
        val product = getProductById(id) ?: return false
        product.isActive = false
        productRepository.save(product)
        return true
    }
}
