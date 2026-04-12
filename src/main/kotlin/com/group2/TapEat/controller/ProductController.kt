package com.group2.TapEat.controller

import com.group2.TapEat.model.Product
import com.group2.TapEat.service.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * Controller untuk mengelola Katalog Produk dan Operasi Admin.
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
class ProductController(private val productService: ProductService) {

    /**
     * Mengambil semua produk aktif untuk ditampilkan di Kios atau Admin.
     * Filter: name (pencarian), category (kategori), availableOnly (true untuk Kios, false untuk Admin).
     */
    @GetMapping
    fun getAllProducts(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false, defaultValue = "false") availableOnly: Boolean
    ): List<Product> = productService.getAllProducts(name, category, availableOnly)

    /**
     * Mengambil satu produk berdasarkan ID.
     */
    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<Product> {
        val product = productService.getProductById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(product)
    }

    /**
     * Endpoin Admin untuk membuat produk baru.
     * Menggunakan multipart/form-data untuk upload gambar.
     */
    @PostMapping(consumes = ["multipart/form-data"])
    fun createProduct(
        @RequestParam("name") name: String,
        @RequestParam("price") price: Double,
        @RequestParam("stock") stock: Int,
        @RequestParam("category", required = false, defaultValue = "Uncategorized") category: String,
        @RequestParam("image", required = false) image: MultipartFile?
    ): ResponseEntity<Product> {
        val product = productService.createProduct(name, price, stock, category, image)
        return ResponseEntity.ok(product)
    }

    /**
     * Endpoint Admin untuk mengupdate produk yang sudah ada.
     */
    @PutMapping("/{id}", consumes = ["multipart/form-data"])
    fun updateProduct(
        @PathVariable id: Long,
        @RequestParam("name") name: String,
        @RequestParam("price") price: Double,
        @RequestParam("stock") stock: Int,
        @RequestParam("category", required = false, defaultValue = "Uncategorized") category: String,
        @RequestParam("image", required = false) image: MultipartFile?
    ): ResponseEntity<Product> {
        val updatedProduct = productService.updateProduct(id, name, price, stock, category, image)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updatedProduct)
    }

    /**
     * Endpoint Admin untuk soft delete produk.
     */
    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        return if (productService.softDeleteProduct(id)) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
