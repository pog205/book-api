package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.*;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.BookResponse;
import org.datn.bookstation.dto.response.BookSentimentResponse;
import org.datn.bookstation.dto.response.BookStockResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.PosBookItemResponse;
import org.datn.bookstation.dto.response.TopBookSoldResponse;
import org.datn.bookstation.dto.response.TrendingBookResponse;
import org.datn.bookstation.dto.response.BookPriceCalculationResponse;
import org.datn.bookstation.entity.Book;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

public interface BookService {
    PaginationResponse<BookResponse> getAllWithPagination(int page, int size, String bookName,
                                                          Integer categoryId, Integer supplierId, Integer publisherId,
                                                          BigDecimal minPrice, BigDecimal maxPrice,
                                                          Byte status, String bookCode);

    PaginationResponse<FlashSaleItemBookRequest> getAllWithPagination(int page, int size, String bookName,
                                                                      Integer parentId, Integer categoryId, List<Integer> authorId, Integer publisherId,
                                                                      BigDecimal minPrice,
                                                                      BigDecimal maxPrice);

    List<Book> getAll();

    List<Book> getActiveBooks();

    List<Book> getBooksByCategory(Integer categoryId);

    List<Book> getBooksBySupplier(Integer supplierId);

    List<Book> getBooksByPublisher(Integer publisherId);

    Book getById(Integer id);

    ApiResponse<Book> add(BookRequest request);

    ApiResponse<Book> update(BookRequest request, Integer id);

    void delete(Integer id);

    ApiResponse<Book> toggleStatus(Integer id);

    //  Trending books API chỉ nhận TrendingRequest (type, page, size) ok
    PaginationResponse<TrendingBookResponse> getTrendingBooks(TrendingRequest request);

    ApiResponse<List<BookCategoryRequest>> getBooksByCategoryId(Integer id, String text);

    ApiResponse<List<FlashSaleItemBookRequest>> getBookByName(String text);

    /**
     * API tính giá sách cho Frontend
     * Tính giá sách sau khi áp dụng discount và so sánh với flash sale
     */
    BookPriceCalculationResponse calculateBookPrice(Book book, BookPriceCalculationRequest request);

    ApiResponse<List<BookFlashSalesRequest>> findActiveBooksWithStock();

    ApiResponse<List<BookFlashSalesRequest>> findActiveBooksForEdit();

    /**
     * Trả về danh sách sách cho dropdown với đầy đủ thông tin bổ sung
     */
    List<org.datn.bookstation.dto.response.DropdownOptionResponse> getDropdownOptionsWithDetails();

    /**
     * Trả về danh sách sách cho dropdown với tìm kiếm theo tên hoặc mã
     */
    List<org.datn.bookstation.dto.response.DropdownOptionResponse> getDropdownOptionsWithDetails(String search);

    ApiResponse<Long> getTotalSoldBooks();

    ApiResponse<Long> getTotalStockBooks();

    ApiResponse<BigDecimal> getTotalRevenue();

    ApiResponse<List<TopBookSoldResponse>> getTopBookSold(int limit);

    ApiResponse<List<BookStockResponse>> getAllBookStock();

    ApiResponse<PosBookItemResponse> getBookByIsbn(String isbn);

    /**
     * THÊM MỚI: Lấy danh sách đơn hàng đang xử lý theo bookId
     *
     * @param bookId ID của sách
     * @return Danh sách đơn hàng đang xử lý sách này
     */
    ApiResponse<List<org.datn.bookstation.dto.response.ProcessingOrderResponse>> getProcessingOrdersByBookId(Integer bookId);

    //  NEW BOOK STATISTICS APIs - Chỉ tập trung vào entity Book

    /**
     * Tổng quan thống kê sách - Overview cơ bản
     */
    ApiResponse<org.datn.bookstation.dto.response.BookStatsOverviewResponse> getBookStatsOverview();

    /**
     * API search/dropdown để lấy book ID cho comparison
     */
    ApiResponse<List<org.datn.bookstation.dto.response.BookSearchResponse>> searchBooksForDropdown(String searchQuery, Integer limit);

    /**
     * So sánh hiệu suất giữa 2 sách hoặc sách vs trung bình
     */
    ApiResponse<org.datn.bookstation.dto.response.BookComparisonResponse> compareBooks(Integer bookId1, Integer bookId2);

    /**
     *  Biểu đồ doanh số bán hàng theo thời gian
     */
    /**
     * API Thống kê sách đơn giản mới - FINAL API
     * Trả về list sách với thông tin cơ bản + doanh thu + tăng trưởng
     */
    org.datn.bookstation.dto.response.BookStatsResponse getBookStats(String chartType, Long fromDate, Long toDate);

    /**
     * API THỐNG KÊ TỔNG QUAN - TIER 1 (Summary)
     * Trả về dữ liệu nhẹ cho chart overview - chỉ tổng số sách bán theo thời gian
     *
     * @param period   day/week/month/year/custom
     * @param fromDate timestamp bắt đầu (tùy chọn)
     * @param toDate   timestamp kết thúc (tùy chọn)
     * @return Danh sách điểm dữ liệu theo thời gian với tổng số sách bán
     */
    ApiResponse<List<java.util.Map<String, Object>>> getBookStatisticsSummary(String period, Long fromDate, Long toDate);

    /**
     * API THỐNG KÊ CHI TIẾT - TIER 2 (Details)
     * Trả về top sách chi tiết khi user click vào điểm cụ thể trên chart
     *
     * @param period day/week/month/year (loại khoảng thời gian)
     * @param date   timestamp số đại diện cho khoảng thời gian
     * @param limit  số lượng sách muốn lấy
     * @return Top sách với thông tin chi tiết + growth comparison
     */
    ApiResponse<List<java.util.Map<String, Object>>> getBookStatisticsDetails(String period, Long date, Integer limit);

    /**
     * API lấy danh sách sách có tỉ lệ đánh giá tích cực >= 75% với thông tin sentiment chi tiết
     *
     * @param page trang (mặc định 0)
     * @param size kích thước trang (mặc định 10)
     * @return Danh sách sách có đánh giá tích cực tốt kèm tỉ lệ chính xác và điểm sao trung bình
     */
    ApiResponse<PaginationResponse<BookSentimentResponse>> getBooksWithHighPositiveRating(int page, int size);
}
