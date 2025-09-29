package org.datn.bookstation.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.service.FlashSaleService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 🔥 FLASH SALE EXPIRATION SCHEDULER (DYNAMIC + BATCH)
 * Tự động schedule task tại thời điểm flash sale kết thúc
 * Hỗ trợ batch processing cho nhiều flash sales cùng expire 1 lúc
 * 
 * ✅ NEW LOGIC: Không set null flashSaleItemId trong cart item nữa
 * - Chỉ update status của FlashSaleItem từ 1 -> 0 khi hết hạn
 * - Cart item giữ nguyên flashSaleItemId, nhưng trả về giá gốc khi status = 0
 * 
 * Approach: Event-driven scheduling với batch processing
 * - Khi tạo flash sale: group theo endTime
 * - Khi endTime đến: xử lý batch tất cả flash sales cùng expire
 * - Efficient: 1 task cho nhiều flash sales cùng thời điểm
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FlashSaleExpirationScheduler {
    
    private final FlashSaleService flashSaleService;
    private final TaskScheduler taskScheduler;
    
    // Map theo thời điểm expiration: timestamp -> Set<flashSaleIds>
    private final ConcurrentHashMap<Long, Set<Integer>> flashSalesByTime = new ConcurrentHashMap<>();
    // Map theo thời điểm expiration: timestamp -> ScheduledTask
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledTasksByTime = new ConcurrentHashMap<>();
    
    /**
     * Schedule một task để xử lý flash sale hết hạn tại thời điểm cụ thể
     * Hỗ trợ batch processing cho nhiều flash sales cùng expire
     * 
     * @param flashSaleId ID của flash sale
     * @param endTime Thời gian kết thúc flash sale (timestamp)
     */
    public void scheduleFlashSaleExpiration(Integer flashSaleId, Long endTime) {
        try {
            // Normalize timestamp để group các flash sales cùng thời điểm
            // Round to nearest minute để tránh tạo quá nhiều tasks
            long normalizedTime = (endTime / 60000) * 60000; // Round to minute
            
            synchronized (this) {
                // Add flash sale vào group theo thời gian
                flashSalesByTime.computeIfAbsent(normalizedTime, k -> new HashSet<>()).add(flashSaleId);
                
                // Nếu chưa có task cho thời điểm này, tạo mới
                if (!scheduledTasksByTime.containsKey(normalizedTime)) {
                    ScheduledFuture<?> task = taskScheduler.schedule(
                        () -> handleBatchFlashSaleExpiration(normalizedTime),
                        Instant.ofEpochMilli(normalizedTime)
                    );
                    
                    scheduledTasksByTime.put(normalizedTime, task);
                    
                    LocalDateTime expireDateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(normalizedTime), ZoneId.systemDefault());
                    log.info("🔥 SCHEDULED: Batch task for time {} with flash sale {}", expireDateTime, flashSaleId);
                } else {
                    log.info("🔥 GROUPED: Added flash sale {} to existing batch at time {}", 
                        flashSaleId, LocalDateTime.ofInstant(Instant.ofEpochMilli(normalizedTime), ZoneId.systemDefault()));
                }
            }
            
        } catch (Exception e) {
            log.error("🔥 ERROR: Failed to schedule expiration for flash sale {}", flashSaleId, e);
        }
    }
    
    /**
     * Xử lý batch flash sales hết hạn cùng thời điểm
     * Method này được gọi tự động khi đến thời gian endTime
     */
    private void handleBatchFlashSaleExpiration(Long normalizedTime) {
        try {
            Set<Integer> expiredFlashSales = flashSalesByTime.get(normalizedTime);
            if (expiredFlashSales == null || expiredFlashSales.isEmpty()) {
                log.warn("🔥 WARNING: No flash sales found for time {}", normalizedTime);
                return;
            }
            
            log.info("🔥 BATCH EXPIRATION: Processing {} flash sales expiring at {}", 
                expiredFlashSales.size(), 
                LocalDateTime.ofInstant(Instant.ofEpochMilli(normalizedTime), ZoneId.systemDefault()));
            
            // ✅ NEW LOGIC: Update status của FlashSaleItem thay vì set null cart item
            int totalUpdatedItems = 0;
            for (Integer flashSaleId : expiredFlashSales) {
                try {
                    int updatedCount = flashSaleService.autoUpdateFlashSaleItemsStatus(flashSaleId);
                    totalUpdatedItems += updatedCount;
                    log.info("🔥 EXPIRATION: Updated {} flash sale items status for flash sale {}", updatedCount, flashSaleId);
                } catch (Exception e) {
                    log.error("🔥 ERROR: Failed to update status for flash sale {}: {}", flashSaleId, e.getMessage());
                }
            }
            
            if (totalUpdatedItems > 0) {
                log.info("🔥 BATCH EXPIRATION: Updated {} flash sale items status for {} expired flash sales", 
                        totalUpdatedItems, expiredFlashSales.size());
            }
            
            // Cleanup
            flashSalesByTime.remove(normalizedTime);
            scheduledTasksByTime.remove(normalizedTime);
            
            log.info("🔥 BATCH EXPIRATION: Completed processing {} flash sales", expiredFlashSales.size());
            
        } catch (Exception e) {
            log.error("🔥 ERROR: Error processing batch expiration for time {}", normalizedTime, e);
        }
    }
    
    /**
     * Cancel scheduled task cho flash sale (khi admin cancel/update flash sale)
     * Với batch approach, cần remove khỏi group thay vì cancel individual task
     */
    public void cancelScheduledTask(Integer flashSaleId) {
        synchronized (this) {
            // Tìm flash sale trong các groups và remove
            for (var entry : flashSalesByTime.entrySet()) {
                Set<Integer> flashSales = entry.getValue();
                if (flashSales.remove(flashSaleId)) {
                    log.info("🔥 CANCELLED: Removed flash sale {} from batch", flashSaleId);
                    
                    // Nếu group trống, cancel task và remove
                    if (flashSales.isEmpty()) {
                        Long timeKey = entry.getKey();
                        ScheduledFuture<?> task = scheduledTasksByTime.get(timeKey);
                        if (task != null && !task.isDone()) {
                            task.cancel(false);
                            scheduledTasksByTime.remove(timeKey);
                            flashSalesByTime.remove(timeKey);
                            log.info("🔥 CANCELLED: Cancelled empty batch task for time {}", timeKey);
                        }
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Cleanup task chạy hàng ngày để dọn dẹp completed tasks
     * Chỉ để maintenance, không phải core logic
     */
    @Scheduled(cron = "0 0 2 * * *") // 2 AM mỗi ngày
    public void cleanupCompletedTasks() {
        try {
            log.info("🔥 MAINTENANCE: Starting cleanup of completed tasks...");
            
            int removedCount = 0;
            synchronized (this) {
                for (var entry : scheduledTasksByTime.entrySet()) {
                    if (entry.getValue().isDone()) {
                        flashSalesByTime.remove(entry.getKey());
                        scheduledTasksByTime.remove(entry.getKey());
                        removedCount++;
                    }
                }
            }
            
            if (removedCount > 0) {
                log.info("🔥 MAINTENANCE: Cleaned up {} completed tasks", removedCount);
            }
            
            log.info("🔥 MAINTENANCE: Active scheduled tasks: {}, Flash sale groups: {}", 
                scheduledTasksByTime.size(), flashSalesByTime.size());
            
        } catch (Exception e) {
            log.error("🔥 ERROR: Error during cleanup", e);
        }
    }
    
    /**
     * 🔄 SCHEDULED: Kiểm tra và cập nhật status định kỳ dựa trên thời gian hiệu lực
     * Logic: 
     * - Nếu startTime <= currentTime <= endTime → status = 1 (có hiệu lực)
     * - Nếu currentTime < startTime hoặc currentTime > endTime → status = 0 (không hiệu lực)
     * 
     * Chạy mỗi 30 giây để đảm bảo real-time status update
     */
    @Scheduled(fixedDelay = 30000) // Mỗi 30 giây
    public void scheduleStatusValidation() {
        try {
            log.info("🔄 SCHEDULED: Starting status validation for all flash sale items...");
            
            int updatedCount = flashSaleService.autoUpdateFlashSaleItemsStatus();
            
            if (updatedCount > 0) {
                log.info("🔄 SCHEDULED: Updated {} flash sale items status based on validity time", updatedCount);
            } else {
                log.debug("🔄 SCHEDULED: No flash sale items need status update");
            }
            
        } catch (Exception e) {
            log.error("🔄 ERROR: Failed to execute scheduled status validation", e);
        }
    }
}
