package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.BookCategoryRequest;
import org.datn.bookstation.dto.request.BookRequest;
import org.datn.bookstation.dto.request.FlashSaleItemBookRequest;
import org.datn.bookstation.dto.request.TrendingRequest;
import org.datn.bookstation.dto.request.QuantityValidationRequest;
import org.datn.bookstation.dto.request.*;
import org.datn.bookstation.dto.request.BookPriceCalculationRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.BookDetailResponse;
import org.datn.bookstation.dto.response.BookResponse;
import org.datn.bookstation.dto.response.BookSentimentResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.TrendingBookResponse;
import org.datn.bookstation.dto.response.QuantityValidationResponse;
import org.datn.bookstation.dto.response.BookPriceCalculationResponse;
import org.datn.bookstation.dto.response.ProcessingOrderResponse;
import org.datn.bookstation.dto.response.BookStatsOverviewResponse;
import org.datn.bookstation.dto.response.BookComparisonResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.mapper.BookResponseMapper;
import org.datn.bookstation.mapper.BookDetailResponseMapper;
import org.datn.bookstation.service.BookService;
import org.datn.bookstation.service.TrendingCacheService;
import org.datn.bookstation.service.FlashSaleItemService;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.repository.OrderDetailRepository;
import org.datn.bookstation.repository.ReviewRepository;
import org.datn.bookstation.util.DateTimeUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;
import org.datn.bookstation.dto.response.PosBookItemResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final BookResponseMapper bookResponseMapper;
    private final BookDetailResponseMapper bookDetailResponseMapper;
    private final TrendingCacheService trendingCacheService;
    private final FlashSaleItemService flashSaleItemService;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final org.datn.bookstation.service.BookProcessingQuantityService bookProcessingQuantityService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<BookResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(required = false) Integer publisherId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) String bookCode) {

        PaginationResponse<BookResponse> books = bookService.getAllWithPagination(
                page, size, bookName, categoryId, supplierId, publisherId, minPrice, maxPrice, status, bookCode);
        ApiResponse<PaginationResponse<BookResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công",
                books);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/client")
    public ResponseEntity<ApiResponse<PaginationResponse<FlashSaleItemBookRequest>>> getAllClient(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) Integer parentCategoryId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String authorIds, // nhận dạng chuỗi "1,2,3"
            @RequestParam(required = false) Integer publisherId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        List<Integer> authorIdList = new ArrayList<>();
        if (authorIds != null && !authorIds.isEmpty()) {
            authorIdList = Arrays.stream(authorIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }

        PaginationResponse<FlashSaleItemBookRequest> books = bookService.getAllWithPagination(
                page, size, bookName, parentCategoryId, categoryId, authorIdList, publisherId, minPrice, maxPrice);
        ApiResponse<PaginationResponse<FlashSaleItemBookRequest>> response = new ApiResponse<>(HttpStatus.OK.value(),
                "Thành công", books);
        return ResponseEntity.ok(response);
    }

    /**
     * 🔥 API lấy danh sách sản phẩm xu hướng (POST)
     * Hỗ trợ 2 loại: DAILY_TRENDING và HOT_DISCOUNT
     * Tất cả parameters gửi trong request body để URL clean và dễ quản lý
     */
    @PostMapping("/trending")
    public ResponseEntity<ApiResponse<PaginationResponse<TrendingBookResponse>>> getTrendingBooks(
            @Valid @RequestBody TrendingRequest request) {
        // Bỏ toàn bộ filter, chỉ giữ lại type, page, size
        TrendingRequest cleanRequest = new TrendingRequest();
        cleanRequest.setType(request.getType());
        cleanRequest.setPage(request.getPage());
        cleanRequest.setSize(request.getSize());
        // Các trường filter khác sẽ bị bỏ qua

        PaginationResponse<TrendingBookResponse> trendingBooks = bookService.getTrendingBooks(cleanRequest);

        // 🔥 REAL FIX: Thay vì dùng repository query SAI, dùng Book.soldCount từ
        // database như /api/books
        System.out.println("🔥 REAL FIX: Using Book.soldCount from database instead of repository query");
        if (trendingBooks != null && trendingBooks.getContent() != null) {
            for (TrendingBookResponse book : trendingBooks.getContent()) {
                // Lấy Book entity trực tiếp từ database để có soldCount đúng
                try {
                    Book bookEntity = bookService.getById(book.getId());
                    if (bookEntity != null && bookEntity.getSoldCount() != null) {
                        int dbSoldCount = bookEntity.getSoldCount();
                        int currentSoldCount = book.getSoldCount();

                        if (currentSoldCount != dbSoldCount) {
                            System.out.println("🔥 REAL FIX - Book ID " + book.getId() +
                                    ": " + currentSoldCount + " → " + dbSoldCount + " (from Book.soldCount)");
                            book.setSoldCount(dbSoldCount);
                            book.setOrderCount(dbSoldCount);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error getting Book entity for ID " + book.getId() + ": " + e.getMessage());
                }
            }
        }

        String message = cleanRequest.isDailyTrending() ? "Lấy danh sách sản phẩm xu hướng theo ngày thành công"
                : "Lấy danh sách sách hot giảm sốc thành công";
        ApiResponse<PaginationResponse<TrendingBookResponse>> response = new ApiResponse<>(HttpStatus.OK.value(),
                message, trendingBooks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getById(@PathVariable Integer id) {
        Book book = bookService.getById(id);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Không tìm thấy sách", null));
        }

        BookDetailResponse bookDetailResponse = bookDetailResponseMapper.toDetailResponse(book);
        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", bookDetailResponse));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> add(@Valid @RequestBody BookRequest bookRequest) {
        ApiResponse<Book> response = bookService.add(bookRequest);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, response.getMessage(), null));
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, response.getMessage(), null));
        }

        BookResponse bookResponse = bookResponseMapper.toResponse(response.getData());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Tạo sách thành công", bookResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> update(@PathVariable Integer id,
            @Valid @RequestBody BookRequest bookRequest) {
        ApiResponse<Book> response = bookService.update(bookRequest, id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, response.getMessage(), null));
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, response.getMessage(), null));
        }

        BookResponse bookResponse = bookResponseMapper.toResponse(response.getData());
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật sách thành công", bookResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<BookResponse>> toggleStatus(@PathVariable Integer id) {
        ApiResponse<Book> response = bookService.toggleStatus(id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Không tìm thấy sách", null));
        }

        BookResponse bookResponse = bookResponseMapper.toResponse(response.getData());
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái thành công", bookResponse));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownBooks(
            @RequestParam(required = false) String search) {
        // Logic lấy danh sách dropdown đã chuyển sang service với hỗ trợ tìm kiếm
        List<DropdownOptionResponse> dropdown = bookService.getDropdownOptionsWithDetails(search);
        ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(HttpStatus.OK.value(),
                "Lấy danh sách sách thành công", dropdown);
        return ResponseEntity.ok(response);
    }

    /**
     * API validate số lượng sản phẩm khi đặt hàng
     * POST /api/books/validate-quantity
     * Đã cải tiến để hỗ trợ validate flash sale items
     */
    @PostMapping("/validate-quantity")
    public ResponseEntity<ApiResponse<QuantityValidationResponse>> validateQuantity(
            @Valid @RequestBody QuantityValidationRequest request) {

        Book book = bookService.getById(request.getBookId());
        if (book == null) {
            QuantityValidationResponse response = QuantityValidationResponse
                    .failure("Không tìm thấy sách", 0);
            return ResponseEntity.ok(new ApiResponse<>(200, "Validate thất bại", response));
        }

        // Kiểm tra xem sách có đang trong flash sale không
        FlashSaleItem activeFlashSale = flashSaleItemRepository.findActiveFlashSaleByBook(book.getId());

        if (activeFlashSale != null) {
            // Nếu là flash sale, validate theo flash sale stock và giới hạn mua
            int flashSaleStock = activeFlashSale.getStockQuantity();
            Integer maxPurchasePerUser = activeFlashSale.getMaxPurchasePerUser();

            // Validate số lượng không vượt quá stock flash sale
            if (request.getQuantity() > flashSaleStock) {
                QuantityValidationResponse response = QuantityValidationResponse.flashSaleFailure(
                        "Flash sale chỉ còn " + flashSaleStock + " sản phẩm",
                        book.getStockQuantity(), flashSaleStock, maxPurchasePerUser);
                return ResponseEntity.ok(new ApiResponse<>(200, "Validate flash sale thất bại", response));
            }

            // Validate giới hạn mua per user (nếu có)
            if (maxPurchasePerUser != null && request.getQuantity() > maxPurchasePerUser) {
                QuantityValidationResponse response = QuantityValidationResponse.flashSaleFailure(
                        "Mỗi khách hàng chỉ được mua tối đa " + maxPurchasePerUser + " sản phẩm flash sale",
                        book.getStockQuantity(), flashSaleStock, maxPurchasePerUser);
                return ResponseEntity.ok(new ApiResponse<>(200, "Validate giới hạn mua thất bại", response));
            }

            // Flash sale thành công
            QuantityValidationResponse response = QuantityValidationResponse.flashSaleSuccess(
                    book.getStockQuantity(), flashSaleStock, maxPurchasePerUser);
            response.setMessage("Có thể mua " + request.getQuantity() + " sản phẩm với giá flash sale");
            return ResponseEntity.ok(new ApiResponse<>(200, "Validate flash sale thành công", response));

        } else {
            // Không phải flash sale, validate theo stock thông thường
            int availableQuantity = book.getStockQuantity();
            boolean isValid = request.getQuantity() > 0 && request.getQuantity() <= availableQuantity;

            QuantityValidationResponse response = isValid
                    ? QuantityValidationResponse.success(availableQuantity)
                    : QuantityValidationResponse.failure(
                            "Số lượng không hợp lệ, tồn kho hiện tại: " + availableQuantity,
                            availableQuantity);

            return ResponseEntity.ok(new ApiResponse<>(200, "Validate thành công", response));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByCategory(@PathVariable Integer categoryId) {
        List<Book> books = bookService.getBooksByCategory(categoryId);
        List<BookResponse> bookResponses = books.stream()
                .map(bookResponseMapper::toResponse)
                .collect(Collectors.toList());
        ApiResponse<List<BookResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công",
                bookResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksBySupplier(@PathVariable Integer supplierId) {
        List<Book> books = bookService.getBooksBySupplier(supplierId);
        List<BookResponse> bookResponses = books.stream()
                .map(bookResponseMapper::toResponse)
                .collect(Collectors.toList());
        ApiResponse<List<BookResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công",
                bookResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getActiveBooks() {
        List<Book> books = bookService.getActiveBooks();
        List<BookResponse> bookResponses = books.stream()
                .map(bookResponseMapper::toResponse)
                .collect(Collectors.toList());
        ApiResponse<List<BookResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công",
                bookResponses);
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint để kiểm tra việc convert publicationDate
     * GET /api/books/test-publication-date
     */
    @GetMapping("/test-publication-date")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testPublicationDate() {
        Map<String, Object> testData = new HashMap<>();

        // Test convert từ LocalDate sang timestamp
        LocalDate testDate = LocalDate.of(2010, 1, 1);
        Long timestamp = DateTimeUtil.dateToTimestamp(testDate);

        // Test convert từ timestamp về LocalDate
        LocalDate convertedBack = DateTimeUtil.timestampToDate(timestamp);

        testData.put("originalDate", testDate.toString());
        testData.put("timestamp", timestamp);
        testData.put("convertedBack", convertedBack.toString());
        testData.put("isEqual", testDate.equals(convertedBack));
        testData.put("currentTimestamp", DateTimeUtil.nowTimestamp());

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(HttpStatus.OK.value(),
                "Test publicationDate conversion thành công", testData);
        return ResponseEntity.ok(response);
    }

    /**
     * 🔥 ADMIN: Cache management endpoints
     */
    @GetMapping("/admin/cache/trending/stats")
    public ResponseEntity<ApiResponse<String>> getTrendingCacheStats() {
        String stats = trendingCacheService.getCacheStatistics();
        ApiResponse<String> response = new ApiResponse<>(HttpStatus.OK.value(), "Cache statistics", stats);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/cache/trending/invalidate")
    public ResponseEntity<ApiResponse<String>> invalidateTrendingCache() {
        trendingCacheService.invalidateAllTrendingCache();
        ApiResponse<String> response = new ApiResponse<>(HttpStatus.OK.value(), "Cache invalidated successfully",
                "All trending cache has been cleared");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bycategoryid/{id}")
    public ResponseEntity<ApiResponse<List<BookCategoryRequest>>> bookByCategoryId(
            @PathVariable("id") Integer id,
            @RequestParam(name = "text", required = false) String text) {
        if (id == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookService.getBooksByCategoryId(id, text));
    }

    @GetMapping("/flashsale/books")
    public ResponseEntity<ApiResponse<List<FlashSaleItemBookRequest>>> findAllBooksInActiveFlashSale() {
        return ResponseEntity.ok(flashSaleItemService.findAllBooksInActiveFlashSale());
    }

    @GetMapping("/searchbook")
    public ResponseEntity<ApiResponse<List<FlashSaleItemBookRequest>>> findAllBooksByName(
            @RequestParam(name = "text", required = false) String text) {
        return ResponseEntity.ok(bookService.getBookByName(text));
    }

    /**
     * 🔥 API tính giá sách cho Frontend
     * POST /api/books/calculate-price
     */
    @PostMapping("/calculate-price")
    public ResponseEntity<ApiResponse<BookPriceCalculationResponse>> calculateBookPrice(
            @Valid @RequestBody BookPriceCalculationRequest request) {

        Book book = bookService.getById(request.getBookId());
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Không tìm thấy sách", null));
        }

        BookPriceCalculationResponse response = bookService.calculateBookPrice(book, request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Tính giá thành công", response));
    }

    @GetMapping("/active-with-stock")
    public ResponseEntity<ApiResponse<List<BookFlashSalesRequest>>> getActiveBooksWithStock() {

        ApiResponse<List<BookFlashSalesRequest>> response = bookService.findActiveBooksWithStock();

        if (response.getStatus() == 500) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, response.getMessage(), null));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active-for-edit")
    public ResponseEntity<ApiResponse<List<BookFlashSalesRequest>>> getActiveBooksForEdit() {

        ApiResponse<List<BookFlashSalesRequest>> response = bookService.findActiveBooksForEdit();

        if (response.getStatus() == 500) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, response.getMessage(), null));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ THÊM MỚI: API lấy danh sách đơn hàng đang xử lý theo bookId
     * Mục đích: Frontend xem sách và thấy processingQuantity, muốn biết chi tiết những đơn nào đang xử lý
     * GET /api/books/{bookId}/processing-orders
     * 
     * @param bookId ID của sách cần xem
     * @return Danh sách các đơn hàng đang xử lý sách này với thông tin chi tiết
     */
    @GetMapping("/{bookId}/processing-orders")
    public ResponseEntity<ApiResponse<List<ProcessingOrderResponse>>> getProcessingOrdersByBookId(
            @PathVariable Integer bookId) {
        
        ApiResponse<List<ProcessingOrderResponse>> response = bookService.getProcessingOrdersByBookId(bookId);
        
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // BOOK STATISTICS APIs - CHI LIEN QUAN DEN SACH
    // ================================================================

    /**
     * API TONG QUAN THONG KE SACH (CHI LIEN QUAN SACH)
     * GET /api/books/stats/overview
     * Thong ke so luong sach, ton kho, khuyen mai
     */
    @GetMapping("/stats/overview")
    public ResponseEntity<ApiResponse<BookStatsOverviewResponse>> getBookStatsOverview() {
        ApiResponse<BookStatsOverviewResponse> response = bookService.getBookStatsOverview();
        return ResponseEntity.ok(response);
    }

    /**
     * API SO SANH SACH
     * GET /api/books/stats/compare
     * So sanh hieu suat giua 2 sach hoac 1 sach voi tat ca
     */
    @GetMapping("/stats/compare")
    public ResponseEntity<ApiResponse<BookComparisonResponse>> compareBooks(
            @RequestParam Integer book1Id,
            @RequestParam(required = false) Integer book2Id) {
        ApiResponse<BookComparisonResponse> response = bookService.compareBooks(book1Id, book2Id);
        return ResponseEntity.ok(response);
    }

    /**
     * 📊 API THỐNG KÊ TỔNG QUAN - TIER 1 (Summary)
     * GET /api/admin/books/statistics/summary
     * Trả về dữ liệu nhẹ cho chart overview - chỉ tổng số sách bán theo thời gian
     * 
     * @param period day/week/month/year/custom (mặc định day)
     * @param fromDate timestamp bắt đầu (tùy chọn - bắt buộc nếu period=custom)
     * @param toDate timestamp kết thúc (tùy chọn - bắt buộc nếu period=custom)
     */
    @GetMapping("/statistics/summary")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBookStatisticsSummary(
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(required = false) Long fromDate,
            @RequestParam(required = false) Long toDate) {
        
        ApiResponse<List<Map<String, Object>>> response = bookService.getBookStatisticsSummary(period, fromDate, toDate);
        return ResponseEntity.ok(response);
    }

    /**
     * 📊 API THỐNG KÊ CHI TIẾT - TIER 2 (Details)  
     * GET /api/admin/books/statistics/details
     * Trả về top sách chi tiết khi user click vào điểm cụ thể trên chart
     * 
     * @param period day/week/month/year (loại khoảng thời gian)
     * @param date timestamp số đại diện cho khoảng thời gian cần xem
     * @param limit số lượng sách muốn lấy (mặc định 10)
     */
    @GetMapping("/statistics/details")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBookStatisticsDetails(
            @RequestParam String period,
            @RequestParam Long date,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        ApiResponse<List<Map<String, Object>>> response = bookService.getBookStatisticsDetails(period, date, limit);
        return ResponseEntity.ok(response);
    }



    /**
     * 📊 API Dropdown search cho việc chọn sách (dành cho comparison)
     */
    @GetMapping("/search-dropdown")
    public ResponseEntity<ApiResponse<List<org.datn.bookstation.dto.response.BookSearchResponse>>> searchBooksForDropdown(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "50") Integer limit) {
        ApiResponse<List<org.datn.bookstation.dto.response.BookSearchResponse>> response =
            bookService.searchBooksForDropdown(q, limit);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 📊 API lấy danh sách sách có tỉ lệ đánh giá tích cực >= 75% với thông tin sentiment chi tiết
     * GET /api/books/high-positive-rating
     */
    @GetMapping("/debug-week-calculation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugWeekCalculation(
            @RequestParam Long timestamp) {

        Map<String, Object> debug = new HashMap<>();

        // Input timestamp info
        LocalDate inputDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        debug.put("inputTimestamp", timestamp);
        debug.put("inputDate", inputDate.toString());
        debug.put("inputDayOfWeek", inputDate.getDayOfWeek().toString());

        // Calculate week range
        LocalDate weekStart = inputDate.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        long startTime = weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTime = weekEnd.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        debug.put("weekStart", weekStart.toString());
        debug.put("weekEnd", weekEnd.toString());
        debug.put("startTimestamp", startTime);
        debug.put("endTimestamp", endTime);
        debug.put("startInstant", Instant.ofEpochMilli(startTime).toString());
        debug.put("endInstant", Instant.ofEpochMilli(endTime).toString());

        // Test what data is found in this range
        List<Object[]> testData = orderDetailRepository.findTopBooksByDateRange(startTime, endTime, 10);
        debug.put("booksFoundCount", testData.size());

        if (!testData.isEmpty()) {
            List<Map<String, Object>> books = new ArrayList<>();
            for (Object[] row : testData) {
                Map<String, Object> book = new HashMap<>();
                book.put("bookId", row[0]);
                book.put("bookCode", row[1]);
                book.put("bookName", row[2]);
                book.put("quantitySold", row[5]);
                books.add(book);
            }
            debug.put("booksFound", books);
        }

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(200, "Debug info", debug);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/high-positive-rating")
    public ResponseEntity<ApiResponse<PaginationResponse<BookSentimentResponse>>> getBooksWithHighPositiveRating(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ApiResponse<PaginationResponse<BookSentimentResponse>> response = bookService.getBooksWithHighPositiveRating(page, size);
        return ResponseEntity.ok(response);
    }
    
    // ✅ API lấy processing quantity cho một sách
    @GetMapping("/processing-quantity/{bookId}")
    public ResponseEntity<ApiResponse<Integer>> getProcessingQuantity(@PathVariable Integer bookId) {
        try {
            Integer processingQuantity = bookProcessingQuantityService.getProcessingQuantity(bookId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy processing quantity thành công", processingQuantity));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(500, "Lỗi khi lấy processing quantity: " + e.getMessage(), null));
        }
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<ApiResponse<PosBookItemResponse>> getByIsbn(@PathVariable String isbn) {
        ApiResponse<PosBookItemResponse> resp = bookService.getBookByIsbn(isbn);
        HttpStatus status = switch (resp.getStatus()) {
            case 200 -> HttpStatus.OK;
            case 404 -> HttpStatus.NOT_FOUND;
            case 400 -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.OK;
        };
        return ResponseEntity.status(status).body(resp);
    }

}
