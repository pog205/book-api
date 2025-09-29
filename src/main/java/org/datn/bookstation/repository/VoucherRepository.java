package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer>, JpaSpecificationExecutor<Voucher> {
    Page<Voucher> findAll(Specification<Voucher> spec, Pageable pageable);

    /**
     * Tìm voucher theo mã code
     */
    Optional<Voucher> findByCode(String code);
    
    /**
     * Kiểm tra voucher có tồn tại theo mã code không
     */
    boolean existsByCode(String code);

    // Đếm tổng số voucher
    @Query("SELECT COUNT(v) FROM Voucher v")
    Long countTotalVouchers();

    // Đếm voucher chưa được sử dụng (usedCount = 0)
    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.usedCount = 0 OR v.usedCount IS NULL")
    Long countUnusedVouchers();

    // Tính tổng lượt sử dụng voucher
    @Query("SELECT COALESCE(SUM(v.usedCount), 0) FROM Voucher v")
    Long sumTotalUsageCount();

    // Tìm voucher được sử dụng nhiều nhất
    @Query("SELECT v.code FROM Voucher v WHERE v.usedCount = (SELECT MAX(v2.usedCount) FROM Voucher v2) ORDER BY v.id ASC")
    List<String> findMostPopularVoucherCode();
}