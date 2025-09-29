package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Publisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PublisherRepository extends JpaRepository<Publisher, Integer>, JpaSpecificationExecutor<Publisher> {
    
    List<Publisher> findByStatus(Byte status);
    
    Optional<Publisher> findByPublisherNameIgnoreCase(String publisherName);
    
    boolean existsByPublisherNameIgnoreCaseAndIdNot(String publisherName, Integer id);
    
    boolean existsByPublisherNameIgnoreCase(String publisherName);
    
    // Methods for validation
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Integer id);
    Optional<Publisher> findByEmail(String email);
    Optional<Publisher> findByPhoneNumber(String phoneNumber);

    //  THÊM MỚI: Thống kê sách theo nhà xuất bản
    @Query("""
            SELECT p.publisherName,
                   COUNT(b.id) as totalBooks,
                   (SELECT COUNT(b2.id) FROM Book b2 
                    WHERE b2.publisher.id = p.id AND b2.createdAt >= :oneMonthAgo) as newBooksThisMonth
            FROM Publisher p 
            LEFT JOIN Book b ON b.publisher.id = p.id 
            WHERE p.status = 1 
            GROUP BY p.id, p.publisherName
            ORDER BY totalBooks DESC
            """)
    List<Object[]> getPublisherBookStatistics(@Param("oneMonthAgo") long oneMonthAgo);

    //  THÊM MỚI: Doanh thu theo nhà xuất bản
    @Query("""
            SELECT p.publisherName,
                   COALESCE(SUM(od.unitPrice * od.quantity), 0) as totalRevenue,
                   COALESCE(SUM(od.quantity), 0) as totalQuantity
            FROM Publisher p 
            LEFT JOIN Book b ON b.publisher.id = p.id 
            LEFT JOIN OrderDetail od ON od.book.id = b.id 
            WHERE p.status = 1 
            GROUP BY p.id, p.publisherName
            ORDER BY totalRevenue DESC
            """)
    List<Object[]> getPublisherRevenueStatistics();

    //  THÊM MỚI: Top nhà xuất bản theo doanh thu
    @Query("""
            SELECT p.publisherName,
                   COALESCE(SUM(od.unitPrice * od.quantity), 0) as totalRevenue,
                   COALESCE(SUM(od.quantity), 0) as totalQuantity
            FROM Publisher p 
            LEFT JOIN Book b ON b.publisher.id = p.id 
            LEFT JOIN OrderDetail od ON od.book.id = b.id 
            WHERE p.status = 1 
            GROUP BY p.id, p.publisherName
            ORDER BY totalRevenue DESC
            """)
    List<Object[]> getTopPublishersByRevenue(Pageable pageable);

    //  THÊM MỚI: Top nhà xuất bản theo số lượng
    @Query("""
            SELECT p.publisherName,
                   COALESCE(SUM(od.quantity), 0) as totalQuantity,
                   COALESCE(SUM(od.unitPrice * od.quantity), 0) as totalRevenue
            FROM Publisher p 
            LEFT JOIN Book b ON b.publisher.id = p.id 
            LEFT JOIN OrderDetail od ON od.book.id = b.id 
            WHERE p.status = 1 
            GROUP BY p.id, p.publisherName
            ORDER BY totalQuantity DESC
            """)
    List<Object[]> getTopPublishersByQuantity(Pageable pageable);
}
