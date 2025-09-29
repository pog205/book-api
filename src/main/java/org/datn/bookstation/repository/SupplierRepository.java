package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    Page<Supplier> findAll(Specification<Supplier> spec, Pageable pageable);
    List<Supplier> findByStatus(Byte status);
    
    // Methods for validation
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Integer id);
    Optional<Supplier> findByEmail(String email);
    Optional<Supplier> findByPhoneNumber(String phoneNumber);

    //  THÊM MỚI: Thống kê sách theo nhà cung cấp
    @Query("""
            SELECT s.supplierName,
                   COUNT(b.id) as totalBooks
            FROM Supplier s 
            LEFT JOIN Book b ON b.supplier.id = s.id 
            WHERE s.status = 1 
            GROUP BY s.id, s.supplierName
            ORDER BY totalBooks DESC
            """)
    List<Object[]> getSupplierBookStatistics();

    //  THÊM MỚI: Doanh thu theo nhà cung cấp
    @Query("""
            SELECT s.supplierName,
                   COALESCE(SUM(od.unitPrice * od.quantity), 0) as totalRevenue,
                   COALESCE(SUM(od.quantity), 0) as totalQuantity
            FROM Supplier s 
            LEFT JOIN Book b ON b.supplier.id = s.id 
            LEFT JOIN OrderDetail od ON od.book.id = b.id 
            WHERE s.status = 1 
            GROUP BY s.id, s.supplierName
            ORDER BY totalRevenue DESC
            """)
    List<Object[]> getSupplierRevenueStatistics();

    //  THÊM MỚI: Top nhà cung cấp theo doanh thu
    @Query("""
            SELECT s.supplierName,
                   COALESCE(SUM(od.unitPrice * od.quantity), 0) as totalRevenue,
                   COALESCE(SUM(od.quantity), 0) as totalQuantity
            FROM Supplier s 
            LEFT JOIN Book b ON b.supplier.id = s.id 
            LEFT JOIN OrderDetail od ON od.book.id = b.id 
            WHERE s.status = 1 
            GROUP BY s.id, s.supplierName
            ORDER BY totalRevenue DESC
            """)
    List<Object[]> getTopSuppliersByRevenue(Pageable pageable);

    //  THÊM MỚI: Top nhà cung cấp theo số lượng
    @Query("""
            SELECT s.supplierName,
                   COALESCE(SUM(od.quantity), 0) as totalQuantity,
                   COALESCE(SUM(od.unitPrice * od.quantity), 0) as totalRevenue
            FROM Supplier s 
            LEFT JOIN Book b ON b.supplier.id = s.id 
            LEFT JOIN OrderDetail od ON od.book.id = b.id 
            WHERE s.status = 1 
            GROUP BY s.id, s.supplierName
            ORDER BY totalQuantity DESC
            """)
    List<Object[]> getTopSuppliersByQuantity(Pageable pageable);
}