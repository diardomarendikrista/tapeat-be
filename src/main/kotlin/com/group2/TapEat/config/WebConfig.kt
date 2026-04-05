package com.group2.TapEat.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

/**
 * Konfigurasi Web untuk pemetaan resource statis.
 * Digunakan agar file di folder /uploads bisa diakses lewat URL.
 */
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Mendapatkan path ke folder uploads
        val uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString()
        
        // Memetakan URL /uploads/** ke folder fisik uploads
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(uploadPath)
    }
}
