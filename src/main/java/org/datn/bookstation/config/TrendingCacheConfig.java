package org.datn.bookstation.config;

import org.springframework.cache.CacheManager;
// import org.springframework.cache.annotation.EnableCaching; // DISABLED - Cache đã được tắt
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 🔥 TRENDING CACHE CONFIGURATION
 * Cấu hình cache cho trending products với real-time invalidation
 */
@Configuration
// @EnableCaching // DISABLED - Cache đã được tắt theo yêu cầu
@EnableAsync
@EnableScheduling
public class TrendingCacheConfig {
    
    // @Bean // DISABLED - Cache manager đã được tắt
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
            "trending-books",              // Cache chính cho trending books
            "trending-books-by-category",  // Cache theo category
            "trending-books-fallback",     // Cache cho fallback books
            "book-authors"                 // Cache cho author mapping
        );
        
        // Allow dynamic cache creation
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}
