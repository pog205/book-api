package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.*;
import org.datn.bookstation.service.BookService;
import org.datn.bookstation.service.OrderService;
import org.datn.bookstation.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final BookService bookService;
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/stats/total-sold")
    public ResponseEntity<ApiResponse<Long>> getTotalSoldBooks() {
        ApiResponse<Long> response = bookService.getTotalSoldBooks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/total-stock")
    public ResponseEntity<ApiResponse<Long>> getTotalStockBooks() {
        ApiResponse<Long> response = bookService.getTotalStockBooks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/total-revenue")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenue() {
        ApiResponse<BigDecimal> response = bookService.getTotalRevenue();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/top-sold")
    public ResponseEntity<ApiResponse<List<TopBookSoldResponse>>> getTopBookSold(
            @RequestParam(defaultValue = "5") int limit) {
        ApiResponse<List<TopBookSoldResponse>> response = bookService.getTopBookSold(limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/stock")
    public ResponseEntity<ApiResponse<List<BookStockResponse>>> getAllBookStock() {
        ApiResponse<List<BookStockResponse>> response = bookService.getAllBookStock();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/revenue")
    public ResponseEntity<ApiResponse<List<RevenueStatsResponse>>> getRevenueStats(
            @RequestParam String type,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String startDate, // yyyy-MM-dd
            @RequestParam(required = false) String endDate // yyyy-MM-dd
    ) {
        ApiResponse<List<RevenueStatsResponse>> response = orderService.getRevenueStats(type, year, month, startDate,
                endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/sold-quantity")
    public ResponseEntity<ApiResponse<List<RevenueStatsResponse>>> getMonthlySoldQuantity() {
        ApiResponse<List<RevenueStatsResponse>> response = orderService.getMonthlySoldQuantity();
        return ResponseEntity.ok(response);

    }

    @GetMapping("/stats/top-spenders")
    public ResponseEntity<ApiResponse<List<TopSpenderResponse>>> getTopSpenders(
            @RequestParam(defaultValue = "5") int limit) {
        ApiResponse<List<TopSpenderResponse>> response = userService.getTopSpenders(limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/total-orders")
    public ResponseEntity<ApiResponse<Long>> getTotalDeliveredOrders() {
        ApiResponse<Long> response = orderService.getTotalDeliveredOrders();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/total-users")
    public ResponseEntity<ApiResponse<Long>> getTotalUsers() {
        ApiResponse<Long> response = userService.getTotalUsers();
        return ResponseEntity.ok(response);
    }
}
