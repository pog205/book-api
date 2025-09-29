package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.response.statistics.*;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.StatisticsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private final UserRepository userRepository;
    private final UserRankRepository userRankRepository;
    private final RankRepository rankRepository;
    private final PointRepository pointRepository;
    private final PublisherRepository publisherRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public UserStatisticsResponse getUserStatistics() {
        log.info("Đang tính toán thống kê người dùng...");

        // Thời gian hiện tại
        long currentTime = System.currentTimeMillis();
        long oneWeekAgo = currentTime - (7 * 24 * 60 * 60 * 1000L);
        long oneMonthAgo = currentTime - (30 * 24 * 60 * 60 * 1000L);

        // Tổng số user
        Long totalUsers = userRepository.count();
        
        // Số user hoạt động (status = 1)
        Long activeUsers = userRepository.countActiveUsers();
        
        // Tỷ lệ hoạt động
        Double activityRate = totalUsers > 0 ? (activeUsers.doubleValue() / totalUsers.doubleValue()) * 100 : 0.0;

        // User mới trong tuần và tháng
        Long newUsersThisWeek = userRepository.countByCreatedAtBetween(oneWeekAgo, currentTime);
        Long newUsersThisMonth = userRepository.countByCreatedAtBetween(oneMonthAgo, currentTime);

        // User theo rank
        List<UserStatisticsResponse.UserRankStatistic> usersByRank = getUsersByRank();

        // Top user theo điểm
        List<UserStatisticsResponse.TopUserByPoint> topUsersByPoint = getTopUsersByPoint();

        // User đã mua hàng vs chỉ đăng ký
        Long purchasingUsers = userRepository.countUsersWithOrders();
        Long registeredOnlyUsers = totalUsers - purchasingUsers;
        Double purchaseRate = totalUsers > 0 ? (purchasingUsers.doubleValue() / totalUsers.doubleValue()) * 100 : 0.0;

        return UserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .activityRate(Math.round(activityRate * 100.0) / 100.0)
                .newUsersThisWeek(newUsersThisWeek)
                .newUsersThisMonth(newUsersThisMonth)
                .usersByRank(usersByRank)
                .topUsersByPoint(topUsersByPoint)
                .purchasingUsers(purchasingUsers)
                .registeredOnlyUsers(registeredOnlyUsers)
                .purchaseRate(Math.round(purchaseRate * 100.0) / 100.0)
                .build();
    }

    @Override
    public RankStatisticsResponse getRankStatistics() {
        log.info("Đang tính toán thống kê rank...");

        // Số lượng user ở mỗi rank
        List<RankStatisticsResponse.RankUserCount> rankUserCounts = getRankUserCounts();
        
        // Điểm trung bình để lên rank
        List<RankStatisticsResponse.RankAveragePoints> averagePointsByRank = getAveragePointsByRank();
        
        // Tỷ lệ tăng/giảm theo tháng
        List<RankStatisticsResponse.RankGrowthRate> monthlyGrowthRates = getMonthlyGrowthRates();

        return RankStatisticsResponse.builder()
                .rankUserCounts(rankUserCounts)
                .averagePointsByRank(averagePointsByRank)
                .monthlyGrowthRates(monthlyGrowthRates)
                .build();
    }

    @Override
    public PointStatisticsResponse getPointStatistics() {
        log.info("Đang tính toán thống kê điểm...");

        // Điểm trung bình mỗi user
        Double averagePointsPerUser = userRepository.getAveragePointsPerUser();
        
        // Tổng điểm toàn hệ thống
        Long totalSystemPoints = userRepository.getTotalSystemPoints();
        
        // Điểm kiếm được/tiêu trong tháng
        long oneMonthAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L);
        Long pointsEarnedThisMonth = pointRepository.countPointsEarnedInPeriod(oneMonthAgo, System.currentTimeMillis());
        Long pointsSpentThisMonth = pointRepository.countPointsSpentInPeriod(oneMonthAgo, System.currentTimeMillis());
        
        // Top hoạt động kiếm nhiều điểm nhất
        List<PointStatisticsResponse.TopPointEarner> topPointEarners = getTopPointEarners();

        return PointStatisticsResponse.builder()
                .averagePointsPerUser(averagePointsPerUser != null ? Math.round(averagePointsPerUser * 100.0) / 100.0 : 0.0)
                .totalSystemPoints(totalSystemPoints != null ? totalSystemPoints : 0L)
                .pointsEarnedThisMonth(pointsEarnedThisMonth != null ? pointsEarnedThisMonth : 0L)
                .pointsSpentThisMonth(pointsSpentThisMonth != null ? pointsSpentThisMonth : 0L)
                .topPointEarners(topPointEarners)
                .build();
    }

    @Override
    public PublisherStatisticsResponse getPublisherStatistics() {
        log.info("Đang tính toán thống kê nhà xuất bản...");

        // Thống kê sách
        List<PublisherStatisticsResponse.PublisherBookStatistic> bookStatistics = getPublisherBookStatistics();
        
        // Thống kê doanh thu
        List<PublisherStatisticsResponse.PublisherRevenueStatistic> revenueStatistics = getPublisherRevenueStatistics();
        
        // Top theo doanh thu
        List<PublisherStatisticsResponse.TopPublisherByRevenue> topByRevenue = getTopPublishersByRevenue();
        
        // Top theo số lượng
        List<PublisherStatisticsResponse.TopPublisherByQuantity> topByQuantity = getTopPublishersByQuantity();

        return PublisherStatisticsResponse.builder()
                .bookStatistics(bookStatistics)
                .revenueStatistics(revenueStatistics)
                .topPublishersByRevenue(topByRevenue)
                .topPublishersByQuantity(topByQuantity)
                .build();
    }

    @Override
    public SupplierStatisticsResponse getSupplierStatistics() {
        log.info("Đang tính toán thống kê nhà cung cấp...");

        // Thống kê sách
        List<SupplierStatisticsResponse.SupplierBookStatistic> bookStatistics = getSupplierBookStatistics();
        
        // Thống kê doanh thu
        List<SupplierStatisticsResponse.SupplierRevenueStatistic> revenueStatistics = getSupplierRevenueStatistics();
        
        // Top theo doanh thu
        List<SupplierStatisticsResponse.TopSupplierByRevenue> topByRevenue = getTopSuppliersByRevenue();
        
        // Top theo số lượng
        List<SupplierStatisticsResponse.TopSupplierByQuantity> topByQuantity = getTopSuppliersByQuantity();

        return SupplierStatisticsResponse.builder()
                .bookStatistics(bookStatistics)
                .revenueStatistics(revenueStatistics)
                .topSuppliersByRevenue(topByRevenue)
                .topSuppliersByQuantity(topByQuantity)
                .build();
    }

    // Các phương thức hỗ trợ

    private List<UserStatisticsResponse.UserRankStatistic> getUsersByRank() {
        return userRankRepository.getUserCountByRank().stream()
                .map(result -> UserStatisticsResponse.UserRankStatistic.builder()
                        .rankName((String) result[0])
                        .userCount((Long) result[1])
                        .build())
                .collect(Collectors.toList());
    }

    private List<UserStatisticsResponse.TopUserByPoint> getTopUsersByPoint() {
        try {
            return userRepository.getTopUsersByPoint().stream()
                    .map(result -> UserStatisticsResponse.TopUserByPoint.builder()
                            .fullName((String) result[0])
                            .email((String) result[1])
                            .totalPoint(result[2] != null ? ((Number) result[2]).intValue() : 0)
                            .rankName((String) result[3])
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy top users by point: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<RankStatisticsResponse.RankUserCount> getRankUserCounts() {
        return rankRepository.getRankUserCounts().stream()
                .map(result -> RankStatisticsResponse.RankUserCount.builder()
                        .rankName((String) result[0])
                        .userCount((Long) result[1])
                        .minSpent((BigDecimal) result[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<RankStatisticsResponse.RankAveragePoints> getAveragePointsByRank() {
        return userRankRepository.getAveragePointsByRank().stream()
                .map(result -> RankStatisticsResponse.RankAveragePoints.builder()
                        .rankName((String) result[0])
                        .averagePoints(Math.round((Double) result[1] * 100.0) / 100.0)
                        .minSpent((BigDecimal) result[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<RankStatisticsResponse.RankGrowthRate> getMonthlyGrowthRates() {
        long currentMonth = System.currentTimeMillis();
        long previousMonth = currentMonth - (30 * 24 * 60 * 60 * 1000L);
        long twoMonthsAgo = currentMonth - (60 * 24 * 60 * 60 * 1000L);

        return userRankRepository.getMonthlyGrowthRates(previousMonth, currentMonth, twoMonthsAgo, previousMonth).stream()
                .map(result -> {
                    String rankName = (String) result[0];
                    Long currentCount = (Long) result[1];
                    Long previousCount = (Long) result[2];
                    
                    Double growthRate = 0.0;
                    if (previousCount != null && previousCount > 0) {
                        growthRate = ((currentCount.doubleValue() - previousCount.doubleValue()) / previousCount.doubleValue()) * 100;
                    }
                    
                    return RankStatisticsResponse.RankGrowthRate.builder()
                            .rankName(rankName)
                            .currentMonthUsers(currentCount)
                            .previousMonthUsers(previousCount != null ? previousCount : 0L)
                            .growthRate(Math.round(growthRate * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<PointStatisticsResponse.TopPointEarner> getTopPointEarners() {
        Pageable topTen = PageRequest.of(0, 10);
        return pointRepository.getTopPointEarners(topTen).stream()
                .map(result -> PointStatisticsResponse.TopPointEarner.builder()
                        .fullName((String) result[0])
                        .email((String) result[1])
                        .totalPointsEarned(((Number) result[2]).intValue())
                        .rankName((String) result[3])
                        .build())
                .collect(Collectors.toList());
    }

    private List<PublisherStatisticsResponse.PublisherBookStatistic> getPublisherBookStatistics() {
        long oneMonthAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L);
        
        return publisherRepository.getPublisherBookStatistics(oneMonthAgo).stream()
                .map(result -> PublisherStatisticsResponse.PublisherBookStatistic.builder()
                        .publisherName((String) result[0])
                        .totalBooks((Long) result[1])
                        .newBooksThisMonth((Long) result[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<PublisherStatisticsResponse.PublisherRevenueStatistic> getPublisherRevenueStatistics() {
        return publisherRepository.getPublisherRevenueStatistics().stream()
                .map(result -> PublisherStatisticsResponse.PublisherRevenueStatistic.builder()
                        .publisherName((String) result[0])
                        .totalRevenue((BigDecimal) result[1])
                        .totalQuantitySold((Long) result[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<PublisherStatisticsResponse.TopPublisherByRevenue> getTopPublishersByRevenue() {
        Pageable topTen = PageRequest.of(0, 10);
        return publisherRepository.getTopPublishersByRevenue(topTen).stream()
                .map(result -> PublisherStatisticsResponse.TopPublisherByRevenue.builder()
                        .publisherName((String) result[0])
                        .totalRevenue((BigDecimal) result[1])
                        .totalQuantitySold((Long) result[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<PublisherStatisticsResponse.TopPublisherByQuantity> getTopPublishersByQuantity() {
        Pageable topTen = PageRequest.of(0, 10);
        return publisherRepository.getTopPublishersByQuantity(topTen).stream()
                .map(result -> PublisherStatisticsResponse.TopPublisherByQuantity.builder()
                        .publisherName((String) result[0])
                        .totalQuantitySold((Long) result[1])
                        .totalRevenue((BigDecimal) result[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<SupplierStatisticsResponse.SupplierBookStatistic> getSupplierBookStatistics() {
        return supplierRepository.getSupplierBookStatistics().stream()
                .map(result -> SupplierStatisticsResponse.SupplierBookStatistic.builder()
                        .supplierName((String) result[0])
                        .totalBooks((Long) result[1])
                        .build())
                .collect(Collectors.toList());
    }

    private List<SupplierStatisticsResponse.SupplierRevenueStatistic> getSupplierRevenueStatistics() {
        return supplierRepository.getSupplierRevenueStatistics().stream()
                .map(result -> SupplierStatisticsResponse.SupplierRevenueStatistic.builder()
                        .supplierName((String) result[0])
                        .totalRevenue((BigDecimal) result[1])
                        .totalQuantitySold((Long) result[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<SupplierStatisticsResponse.TopSupplierByRevenue> getTopSuppliersByRevenue() {
        Pageable topTen = PageRequest.of(0, 10);
        return supplierRepository.getTopSuppliersByRevenue(topTen).stream()
                .map(result -> SupplierStatisticsResponse.TopSupplierByRevenue.builder()
                        .supplierName((String) result[0])
                        .totalRevenue((BigDecimal) result[1])
                        .totalQuantitySold((Long) result[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<SupplierStatisticsResponse.TopSupplierByQuantity> getTopSuppliersByQuantity() {
        Pageable topTen = PageRequest.of(0, 10);
        return supplierRepository.getTopSuppliersByQuantity(topTen).stream()
                .map(result -> SupplierStatisticsResponse.TopSupplierByQuantity.builder()
                        .supplierName((String) result[0])
                        .totalQuantitySold((Long) result[1])
                        .totalRevenue((BigDecimal) result[2])
                        .build())
                .collect(Collectors.toList());
    }
}
