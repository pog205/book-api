package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.PointManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PointManagementServiceImpl implements PointManagementService {
    
    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final UserRankRepository userRankRepository;
    
    @Override
    public void earnPointsFromOrder(Order order, User user) {
        if (order == null || user == null || order.getTotalAmount() == null) {
            log.warn("Cannot earn points: invalid order or user");
            return;
        }
        
        // ✅ BỎ CHECK TRẠNG THÁI VÌ HÀM NÀY ĐƯỢC GỌI KHI CHUYỂN SANG DELIVERED
        // Hàm này sẽ được gọi từ handlePointImpact khi newStatus = DELIVERED
        
        // Kiểm tra xem đã tích điểm cho đơn hàng này chưa
        List<Point> existingPoints = pointRepository.findAll().stream()
            .filter(p -> p.getOrder() != null && p.getOrder().getId().equals(order.getId()) 
                        && p.getPointEarned() != null && p.getPointEarned() > 0)
            .toList();
        
        if (!existingPoints.isEmpty()) {
            log.info("Points already earned for order ID: {}", order.getId());
            return;
        }
        
        // Tính điểm được tích
        int earnedPoints = calculateEarnedPoints(order.getTotalAmount(), user);
        
        if (earnedPoints <= 0) {
            log.info("No points to earn for order ID: {}", order.getId());
            return;
        }
        
        // Tạo record tích điểm
        Point point = new Point();
        point.setUser(user);
        point.setOrder(order);
        point.setPointEarned(earnedPoints);
        point.setMinSpent(order.getTotalAmount());
        point.setDescription("Tích điểm từ đơn hàng " + order.getCode());
        point.setCreatedAt(System.currentTimeMillis());
        point.setStatus((byte) 1);
        pointRepository.save(point);
        
        // Cập nhật tổng điểm và tổng chi tiêu của user
        int currentTotalPoints = user.getTotalPoint() != null ? user.getTotalPoint() : 0;
        BigDecimal currentTotalSpent = user.getTotalSpent() != null ? user.getTotalSpent() : BigDecimal.ZERO;
        
        user.setTotalPoint(currentTotalPoints + earnedPoints);
        user.setTotalSpent(currentTotalSpent.add(order.getTotalAmount()));
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
        
        // Cập nhật rank
        updateUserRank(user);
        
        log.info("Earned {} points for user {} from order {}", earnedPoints, user.getEmail(), order.getCode());
    }
    
    @Override
    public void deductPointsFromCancelledOrder(Order order, User user) {
        if (order == null || user == null) {
            log.warn("Cannot deduct points: invalid order or user");
            return;
        }
        
        // Tìm record tích điểm từ đơn hàng này
        List<Point> earnedPoints = pointRepository.findAll().stream()
            .filter(p -> p.getOrder() != null && p.getOrder().getId().equals(order.getId()) 
                        && p.getPointEarned() != null && p.getPointEarned() > 0)
            .toList();
        
        if (earnedPoints.isEmpty()) {
            log.info("No earned points found for cancelled order ID: {}", order.getId());
            return;
        }
        
        int totalPointsToDeduct = 0;
        BigDecimal totalSpentToDeduct = BigDecimal.ZERO;
        
        for (Point point : earnedPoints) {
            totalPointsToDeduct += point.getPointEarned();
            if (point.getMinSpent() != null) {
                totalSpentToDeduct = totalSpentToDeduct.add(point.getMinSpent());
            }
            
            // Tạo record trừ điểm
            Point deductPoint = new Point();
            deductPoint.setUser(user);
            deductPoint.setOrder(order);
            deductPoint.setPointSpent(point.getPointEarned()); // Số điểm trừ = số điểm đã tích
            deductPoint.setDescription("Trừ điểm do hủy đơn hàng " + order.getCode());
            deductPoint.setCreatedAt(System.currentTimeMillis());
            deductPoint.setStatus((byte) 1);
            pointRepository.save(deductPoint);
        }
        
        // Cập nhật tổng điểm và tổng chi tiêu của user
        int currentTotalPoints = user.getTotalPoint() != null ? user.getTotalPoint() : 0;
        BigDecimal currentTotalSpent = user.getTotalSpent() != null ? user.getTotalSpent() : BigDecimal.ZERO;
        
        // Đảm bảo không bị âm
        int newTotalPoints = Math.max(0, currentTotalPoints - totalPointsToDeduct);
        BigDecimal newTotalSpent = currentTotalSpent.subtract(totalSpentToDeduct);
        if (newTotalSpent.compareTo(BigDecimal.ZERO) < 0) {
            newTotalSpent = BigDecimal.ZERO;
        }
        
        user.setTotalPoint(newTotalPoints);
        user.setTotalSpent(newTotalSpent);
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
        
        // Cập nhật rank
        updateUserRank(user);
        
        log.info("Deducted {} points from user {} for cancelled order {}", 
                 totalPointsToDeduct, user.getEmail(), order.getCode());
    }
    
    @Override
    public void refundPointsFromReturnedOrder(Order order, User user) {
        // Logic tương tự deductPointsFromCancelledOrder
        deductPointsFromCancelledOrder(order, user);
        log.info("Refunded points for returned order {}", order.getCode());
    }
    
    @Override
    public void deductPointsFromPartialRefund(BigDecimal refundAmount, Order order, User user) {
        try {
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Invalid refund amount for partial refund: {}", refundAmount);
                return;
            }
            
            // ✅ Tính điểm cần trừ dựa trên số tiền hoàn trả
            int pointsToDeduct = calculateEarnedPoints(refundAmount, user);
            
            if (pointsToDeduct <= 0) {
                log.info("No points to deduct for partial refund amount: {}", refundAmount);
                return;
            }
            
            // Tạo Point record để trừ điểm (sử dụng pointSpent)
            Point pointDeduction = new Point();
            pointDeduction.setUser(user);
            pointDeduction.setOrder(order);
            pointDeduction.setPointSpent(pointsToDeduct); // Điểm bị trừ
            pointDeduction.setMinSpent(refundAmount); // Số tiền tương ứng
            pointDeduction.setDescription(String.format("Trừ điểm do hoàn trả một phần đơn hàng %s (%.0f VND)", 
                                                       order.getCode(), refundAmount));
            pointDeduction.setCreatedAt(System.currentTimeMillis());
            pointDeduction.setStatus((byte) 1); // Active
            pointRepository.save(pointDeduction);
            
            // Cập nhật tổng điểm user
            int currentPoints = user.getTotalPoint() != null ? user.getTotalPoint() : 0;
            user.setTotalPoint(Math.max(0, currentPoints - pointsToDeduct)); // Không cho phép âm
            userRepository.save(user);
            
            log.info("✅ Deducted {} points for partial refund {} VND from order {}", 
                    pointsToDeduct, refundAmount, order.getCode());
                    
        } catch (Exception e) {
            log.error("Error deducting points for partial refund order {}: {}", order.getCode(), e.getMessage(), e);
        }
    }
    
    @Override
    public int calculateEarnedPoints(BigDecimal totalAmount, User user) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        
        // Lấy rank hiện tại của user
        BigDecimal multiplier = getUserRankMultiplier(user);
        
        // Tính điểm: 1 điểm cho mỗi 1000 VND, nhân với multiplier của rank
        BigDecimal basePoints = totalAmount.divide(new BigDecimal("1000"), 0, RoundingMode.DOWN);
        BigDecimal earnedPoints = basePoints.multiply(multiplier);
        
        return earnedPoints.intValue();
    }
    
    @Override
    public void updateUserRank(User user) {
        if (user == null || user.getTotalSpent() == null) {
            return;
        }
        
        // Tìm rank phù hợp nhất với tổng chi tiêu hiện tại
        List<Rank> activeRanks = rankRepository.findAll().stream()
            .filter(rank -> rank.getStatus() != null && rank.getStatus() == 1)
            .filter(rank -> rank.getMinSpent() != null)
            .filter(rank -> user.getTotalSpent().compareTo(rank.getMinSpent()) >= 0)
            .sorted((r1, r2) -> r2.getMinSpent().compareTo(r1.getMinSpent())) // Sắp xếp giảm dần
            .toList();
        
        if (activeRanks.isEmpty()) {
            log.info("No suitable rank found for user {} with total spent: {}", 
                     user.getEmail(), user.getTotalSpent());
            return;
        }
        
        Rank newRank = activeRanks.get(0); // Rank cao nhất phù hợp
        
        // Kiểm tra rank hiện tại của user
        List<UserRank> currentActiveRanks = userRankRepository.findAll().stream()
            .filter(ur -> ur.getUser() != null && ur.getUser().getId().equals(user.getId()) 
                         && ur.getStatus() != null && ur.getStatus() == 1)
            .toList();
        
        // Nếu đã có rank này rồi thì không cần cập nhật
        boolean alreadyHasThisRank = currentActiveRanks.stream()
            .anyMatch(ur -> ur.getRank() != null && ur.getRank().getId().equals(newRank.getId()));
        
        if (alreadyHasThisRank) {
            log.info("User {} already has rank {}", user.getEmail(), newRank.getRankName());
            return;
        }
        
        // Deactivate tất cả rank hiện tại
        for (UserRank currentRank : currentActiveRanks) {
            currentRank.setStatus((byte) 0);
            currentRank.setUpdatedAt(System.currentTimeMillis());
            userRankRepository.save(currentRank);
        }
        
        // Tạo hoặc activate rank mới
        List<UserRank> existingUserRanks = userRankRepository.findAll().stream()
            .filter(ur -> ur.getUser() != null && ur.getUser().getId().equals(user.getId()) 
                         && ur.getRank() != null && ur.getRank().getId().equals(newRank.getId()))
            .toList();
        
        if (!existingUserRanks.isEmpty()) {
            // Activate rank đã tồn tại
            UserRank existingUserRank = existingUserRanks.get(0);
            existingUserRank.setStatus((byte) 1);
            existingUserRank.setUpdatedAt(System.currentTimeMillis());
            userRankRepository.save(existingUserRank);
        } else {
            // Tạo mới UserRank
            UserRank newUserRank = new UserRank();
            newUserRank.setUser(user);
            newUserRank.setRank(newRank);
            newUserRank.setStatus((byte) 1);
            newUserRank.setCreatedAt(System.currentTimeMillis());
            userRankRepository.save(newUserRank);
        }
        
        log.info("Updated rank for user {} to {}", user.getEmail(), newRank.getRankName());
    }
    
    @Override
    public void checkAndUpdateUserRank(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            updateUserRank(user);
        }
    }
    
    /**
     * Lấy hệ số nhân điểm từ rank của user
     */
    private BigDecimal getUserRankMultiplier(User user) {
        if (user == null) {
            return BigDecimal.ONE; // Mặc định 1.0
        }
        
        // Tìm rank hiện tại của user
        List<UserRank> activeUserRanks = userRankRepository.findAll().stream()
            .filter(ur -> ur.getUser() != null && ur.getUser().getId().equals(user.getId()) 
                         && ur.getStatus() != null && ur.getStatus() == 1)
            .toList();
        
        if (activeUserRanks.isEmpty()) {
            return BigDecimal.ONE; // Mặc định 1.0 nếu không có rank
        }
        
        UserRank userRank = activeUserRanks.get(0);
        if (userRank.getRank() != null && userRank.getRank().getPointMultiplier() != null) {
            return userRank.getRank().getPointMultiplier();
        }
        
        return BigDecimal.ONE; // Mặc định 1.0
    }
}
