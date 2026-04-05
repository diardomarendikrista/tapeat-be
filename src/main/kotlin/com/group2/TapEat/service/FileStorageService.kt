package com.group2.TapEat.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

/**
 * Service untuk mengelola penyimpanan file gambar ke direktori lokal /uploads.
 * source belajar: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/multipart/MultipartFile.html
 */
@Service
class FileStorageService {
    private val uploadDir = "uploads"

    init {
        // Membuat direktori jika belum ada saat aplikasi dijalankan
        val path = Paths.get(uploadDir)
        if (!Files.exists(path)) {
            Files.createDirectories(path)
        }
    }

    /**
     * Menyimpan file yang diupload ke folder /uploads dengan nama random (UUID).
     */
    fun storeFile(file: MultipartFile): String {
        val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
        val targetPath = Paths.get(uploadDir).resolve(fileName)
        Files.copy(file.inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
        return fileName
    }
}
