package org.datn.bookstation.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.service.TrendingCacheService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 🔥 TRENDING CACHE SCHEDULER
 * DISABLED - Cache đã được tắt theo yêu cầu
 */
// @Component // DISABLED
@RequiredArgsConstructor
@Slf4j
public class TrendingCacheScheduler {
    
    private final TrendingCacheService trendingCacheService;
    
    /**
     * Rebuild cache mỗi 6 giờ - DISABLED
     */
    // @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // DISABLED
    public void rebuildTrendingCache() {
        // Cache đã được tắt - không làm gì cả
        log.debug("Cache rebuild scheduler disabled");
    }
    
    /**
     * Log cache statistics mỗi giờ - DISABLED
     */
    // @Scheduled(fixedRate = 60 * 60 * 1000) // DISABLED
    public void logCacheStatistics() {
        // Cache đã được tắt - không làm gì cả
        log.debug("Cache statistics logging disabled");
    }
}
