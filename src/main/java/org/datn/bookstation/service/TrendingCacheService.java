package org.datn.bookstation.service;

import org.springframework.stereotype.Service;

/**
 * 🔥 REAL-TIME TRENDING CACHE SERVICE
 * Quản lý cache và cập nhật real-time cho trending products
 */
@Service
public interface TrendingCacheService {
    
    /**
     * Invalidate cache khi có đơn hàng mới
     */
    void invalidateCacheOnNewOrder(Integer bookId, Integer quantity);
    
    /**
     * Invalidate cache khi có review mới
     */
    void invalidateCacheOnNewReview(Integer bookId, Double rating);
    
    /**
     * Invalidate cache khi có flash sale mới
     */
    void invalidateCacheOnFlashSaleChange(Integer bookId, boolean isStarted);
    
    /**
     * Invalidate toàn bộ cache trending
     */
    void invalidateAllTrendingCache();
    
    /**
     * Invalidate cache theo category
     */
    void invalidateCacheByCategory(Integer categoryId);
    
    /**
     * Check và trigger rebuild cache nếu cần
     */
    void checkAndRebuildCacheIfNeeded();
    
    /**
     * Get cache statistics
     */
    String getCacheStatistics();
}
