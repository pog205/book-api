package org.datn.bookstation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.statistics.*;
import org.datn.bookstation.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> getUserStatistics() {
        try {
            log.info("Yêu cầu lấy thống kê người dùng");
            UserStatisticsResponse statistics = statisticsService.getUserStatistics();
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thống kê người dùng thành công", statistics));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê người dùng: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(500, "Có lỗi xảy ra khi lấy thống kê người dùng: " + e.getMessage(), null));
        }
    }

    @GetMapping("/ranks")
    public ResponseEntity<ApiResponse<RankStatisticsResponse>> getRankStatistics() {
        try {
            log.info("Yêu cầu lấy thống kê xếp hạng");
            RankStatisticsResponse statistics = statisticsService.getRankStatistics();
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thống kê xếp hạng thành công", statistics));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê xếp hạng: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(500, "Có lỗi xảy ra khi lấy thống kê xếp hạng: " + e.getMessage(), null));
        }
    }

    @GetMapping("/points")
    public ResponseEntity<ApiResponse<PointStatisticsResponse>> getPointStatistics() {
        try {
            log.info("Yêu cầu lấy thống kê điểm");
            PointStatisticsResponse statistics = statisticsService.getPointStatistics();
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thống kê điểm thành công", statistics));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê điểm: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(500, "Có lỗi xảy ra khi lấy thống kê điểm: " + e.getMessage(), null));
        }
    }

    @GetMapping("/publishers")
    public ResponseEntity<ApiResponse<PublisherStatisticsResponse>> getPublisherStatistics() {
        try {
            log.info("Yêu cầu lấy thống kê nhà xuất bản");
            PublisherStatisticsResponse statistics = statisticsService.getPublisherStatistics();
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thống kê nhà xuất bản thành công", statistics));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê nhà xuất bản: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(500, "Có lỗi xảy ra khi lấy thống kê nhà xuất bản: " + e.getMessage(), null));
        }
    }

    @GetMapping("/suppliers")
    public ResponseEntity<ApiResponse<SupplierStatisticsResponse>> getSupplierStatistics() {
        try {
            log.info("Yêu cầu lấy thống kê nhà cung cấp");
            SupplierStatisticsResponse statistics = statisticsService.getSupplierStatistics();
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thống kê nhà cung cấp thành công", statistics));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê nhà cung cấp: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(500, "Có lỗi xảy ra khi lấy thống kê nhà cung cấp: " + e.getMessage(), null));
        }
    }
}
