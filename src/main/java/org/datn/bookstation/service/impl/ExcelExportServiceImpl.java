package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.ExcelDataResponse;
import org.datn.bookstation.dto.response.ExcelFieldsResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.ReviewStatus;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    @Autowired
    private RankRepository rankRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private AuthorRepository authorRepository;
    
    @Autowired
    private PublisherRepository publisherRepository;
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @Autowired
    private PointRepository pointRepository;
    
    @Autowired
    private FlashSaleRepository flashSaleRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "";
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DATETIME_FORMATTER);
    }
    
    private String formatDateTimestamp(Long timestamp) {
        if (timestamp == null) return "";
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DATE_FORMATTER);
    }

    // ==================== DATA EXPORT METHODS ====================

    @Override
    public ApiResponse<ExcelDataResponse> getRanksForExport() {
        try {
            List<Rank> ranks = rankRepository.findAll();

            List<Map<String, Object>> data = ranks.stream().map(this::mapRankToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getRankFieldsMap())
                    .fileName("danh-sach-hang-thanh-vien")
                    .sheetName("Hạng Thành Viên")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu hạng thành viên thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu hạng thành viên: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ExcelDataResponse> getBooksForExport() {
        try {
            List<Book> books = bookRepository.findAll();

            List<Map<String, Object>> data = books.stream().map(this::mapBookToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getBookFieldsMap())
                    .fileName("danh-sach-sach")
                    .sheetName("Sách")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu sách thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu sách: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ExcelDataResponse> getUsersForExport() {
        try {
            List<User> users = userRepository.findAll();

            List<Map<String, Object>> data = users.stream().map(this::mapUserToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getUserFieldsMap())
                    .fileName("danh-sach-nguoi-dung")
                    .sheetName("Người Dùng")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu người dùng thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu người dùng: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ExcelDataResponse> getOrdersForExport() {
        try {
            List<Order> orders = orderRepository.findAll();

            List<Map<String, Object>> data = orders.stream().map(this::mapOrderToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getOrderFieldsMap())
                    .fileName("danh-sach-don-hang")
                    .sheetName("Đơn Hàng")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu đơn hàng thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu đơn hàng: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ExcelDataResponse> getReviewsForExport() {
        try {
            List<Review> reviews = reviewRepository.findAll();

            List<Map<String, Object>> data = reviews.stream().map(this::mapReviewToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getReviewFieldsMap())
                    .fileName("danh-sach-danh-gia")
                    .sheetName("Đánh Giá")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu đánh giá thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu đánh giá: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ExcelDataResponse> getCategoriesForExport() {
        try {
            List<Category> categories = categoryRepository.findAll();

            List<Map<String, Object>> data = categories.stream().map(this::mapCategoryToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getCategoryFieldsMap())
                    .fileName("danh-sach-the-loai")
                    .sheetName("Thể Loại")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu thể loại thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu thể loại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ExcelDataResponse> getAuthorsForExport() {
        try {
            List<Author> authors = authorRepository.findAll();

            List<Map<String, Object>> data = authors.stream().map(this::mapAuthorToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getAuthorFieldsMap())
                    .fileName("danh-sach-tac-gia")
                    .sheetName("Tác Giả")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu tác giả thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu tác giả: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ExcelDataResponse> getPublishersForExport() {
        try {
            List<Publisher> publishers = publisherRepository.findAll();

            List<Map<String, Object>> data = publishers.stream().map(this::mapPublisherToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getPublisherFieldsMap())
                    .fileName("danh-sach-nha-xuat-ban")
                    .sheetName("Nhà Xuất Bản")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu nhà xuất bản thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu nhà xuất bản: " + e.getMessage(), null);
        }
    }

        @Override
    public ApiResponse<ExcelDataResponse> getVouchersForExport() {
        try {
            List<Voucher> vouchers = voucherRepository.findAll();
            
            List<Map<String, Object>> data = vouchers.stream().map(this::mapVoucherToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getVoucherFieldsMap())
                    .fileName("danh-sach-voucher")
                    .sheetName("Voucher")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu voucher thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu voucher: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ExcelDataResponse> getSuppliersForExport() {
        try {
            List<Supplier> suppliers = supplierRepository.findAll();
            
            List<Map<String, Object>> data = suppliers.stream().map(this::mapSupplierToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getSupplierFieldsMap())
                    .fileName("danh-sach-nha-cung-cap")
                    .sheetName("Nhà Cung Cấp")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu nhà cung cấp thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu nhà cung cấp: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ExcelDataResponse> getPointsForExport() {
        try {
            List<Point> points = pointRepository.findAll();
            
            List<Map<String, Object>> data = points.stream().map(this::mapPointToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getPointFieldsMap())
                    .fileName("danh-sach-diem")
                    .sheetName("Điểm")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu điểm thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu điểm: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ExcelDataResponse> getFlashSalesForExport() {
        try {
            List<FlashSale> flashSales = flashSaleRepository.findAll();
            
            List<Map<String, Object>> data = flashSales.stream().map(this::mapFlashSaleToExcelData).collect(Collectors.toList());
            
            ExcelDataResponse response = ExcelDataResponse.builder()
                    .data(data)
                    .fields(getFlashSaleFieldsMap())
                    .fileName("danh-sach-giam-gia-nhanh")
                    .sheetName("Giảm Giá Nhanh")
                    .totalRecords((long) data.size())
                    .build();

            return new ApiResponse<>(200, "Lấy dữ liệu giảm giá nhanh thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy dữ liệu giảm giá nhanh: " + e.getMessage(), null);
        }
    }

    // ==================== FIELDS MAPPING METHODS ====================

    @Override
    public ApiResponse<ExcelFieldsResponse> getRankFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getRankFieldsMap())
                .entityType("ranks")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields ranks thành công", response);
    }

    @Override
    public ApiResponse<ExcelFieldsResponse> getBookFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getBookFieldsMap())
                .entityType("books")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields books thành công", response);
    }

    @Override
    public ApiResponse<ExcelFieldsResponse> getUserFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getUserFieldsMap())
                .entityType("users")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields users thành công", response);
    }

    @Override
    public ApiResponse<ExcelFieldsResponse> getOrderFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getOrderFieldsMap())
                .entityType("orders")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields orders thành công", response);
    }

    @Override
    public ApiResponse<ExcelFieldsResponse> getReviewFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getReviewFieldsMap())
                .entityType("reviews")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields reviews thành công", response);
    }

    @Override
    public ApiResponse<ExcelFieldsResponse> getCategoryFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getCategoryFieldsMap())
                .entityType("categories")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields categories thành công", response);
    }

    @Override
    public ApiResponse<ExcelFieldsResponse> getAuthorFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getAuthorFieldsMap())
                .entityType("authors")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields authors thành công", response);
    }

    @Override
    public ApiResponse<ExcelFieldsResponse> getPublisherFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getPublisherFieldsMap())
                .entityType("publishers")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields publishers thành công", response);
    }

    @Override
    public ApiResponse<ExcelFieldsResponse> getVoucherFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getVoucherFieldsMap())
                .entityType("vouchers")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields vouchers thành công", response);
    }

    @Override
    public ApiResponse<ExcelFieldsResponse> getSupplierFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getSupplierFieldsMap())
                .entityType("suppliers")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields suppliers thành công", response);
    }

    @Override
    public ApiResponse<ExcelFieldsResponse> getPointFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getPointFieldsMap())
                .entityType("points")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields points thành công", response);
    }

    @Override
    public ApiResponse<ExcelFieldsResponse> getFlashSaleFieldsMapping() {
        ExcelFieldsResponse response = ExcelFieldsResponse.builder()
                .fields(getFlashSaleFieldsMap())
                .entityType("flashsales")
                .build();
        return new ApiResponse<>(200, "Lấy mapping fields flashsales thành công", response);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Map<String, Object> mapRankToExcelData(Rank rank) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", rank.getId());
        data.put("rankName", rank.getRankName());
        data.put("minSpent", rank.getMinSpent());
        data.put("pointMultiplier", rank.getPointMultiplier());
        data.put("status", rank.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
        data.put("createdAt", formatTimestamp(rank.getCreatedAt()));
        data.put("updatedAt", formatTimestamp(rank.getUpdatedAt()));
        return data;
    }

    private Map<String, Object> mapBookToExcelData(Book book) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", book.getId());
        data.put("bookName", book.getBookName());
        data.put("author", getAuthorNames(book));
        data.put("category", book.getCategory() != null ? book.getCategory().getCategoryName() : "");
        data.put("publisher", book.getPublisher() != null ? book.getPublisher().getPublisherName() : "");
        data.put("price", book.getPrice());
        data.put("effectivePrice", book.getEffectivePrice());
        data.put("stockQuantity", book.getStockQuantity());
        data.put("format", book.getBookFormat() != null ? book.getBookFormat().name() : "");
        data.put("pageCount", book.getPageCount());
        data.put("language", book.getLanguage());
        data.put("isbn", book.getIsbn());
        data.put("publicationDate", formatDateTimestamp(book.getPublicationDate()));
        data.put("status", book.getStatus() == 1 ? "Còn hàng" : "Hết hàng");
        data.put("createdAt", formatTimestamp(book.getCreatedAt()));
        return data;
    }

    private Map<String, Object> mapUserToExcelData(User user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("fullName", user.getFullName());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhoneNumber());
        data.put("totalPoint", user.getTotalPoint());
        data.put("totalSpent", user.getTotalSpent());
        data.put("status", user.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
        data.put("createdAt", formatTimestamp(user.getCreatedAt()));
        return data;
    }

    private Map<String, Object> mapOrderToExcelData(Order order) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", order.getId());
        data.put("code", order.getCode());
        data.put("customerName", order.getUser() != null ? order.getUser().getFullName() : order.getRecipientName());
        data.put("customerEmail", order.getUser() != null ? order.getUser().getEmail() : "");
        data.put("customerPhone", order.getUser() != null ? order.getUser().getPhoneNumber() : order.getPhoneNumber());
        data.put("subtotal", order.getSubtotal());
        data.put("totalAmount", order.getTotalAmount());
        data.put("shippingFee", order.getShippingFee());
        data.put("discountAmount", order.getDiscountAmount());
        data.put("orderType", order.getOrderType());
        data.put("status", getOrderStatusInVietnamese(order.getOrderStatus()));
        data.put("paymentMethod", order.getPaymentMethod());
        data.put("orderDate", formatTimestamp(order.getOrderDate()));
        data.put("notes", order.getNotes());
        return data;
    }

    private Map<String, Object> mapReviewToExcelData(Review review) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", review.getId());
        data.put("bookName", review.getBook() != null ? review.getBook().getBookName() : "");
        data.put("userName", review.getUser() != null ? review.getUser().getFullName() : "");
        data.put("userEmail", review.getUser() != null ? review.getUser().getEmail() : "");
        data.put("rating", review.getRating());
        data.put("comment", review.getComment());
        data.put("isPositive", review.getIsPositive() != null ? 
                               (review.getIsPositive() ? "Tích cực" : "Tiêu cực") : "Không xác định");
        data.put("reviewStatus", getReviewStatusInVietnamese(review.getReviewStatus()));
        data.put("reviewDate", formatTimestamp(review.getReviewDate()));
        data.put("createdAt", formatTimestamp(review.getCreatedAt()));
        return data;
    }

    private Map<String, Object> mapCategoryToExcelData(Category category) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", category.getId());
        data.put("name", category.getCategoryName());
        data.put("description", category.getDescription());
        data.put("status", category.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
        data.put("createdAt", formatTimestamp(category.getCreatedAt()));
        data.put("updatedAt", formatTimestamp(category.getUpdatedAt()));
        return data;
    }

    private Map<String, Object> mapAuthorToExcelData(Author author) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", author.getId());
        data.put("name", author.getAuthorName());
        data.put("biography", author.getBiography());
        data.put("birthDate", author.getBirthDate() != null ? author.getBirthDate().format(DATE_FORMATTER) : "");
        data.put("status", author.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
        data.put("createdAt", formatTimestamp(author.getCreatedAt()));
        data.put("updatedAt", formatTimestamp(author.getUpdatedAt()));
        return data;
    }

    private Map<String, Object> mapPublisherToExcelData(Publisher publisher) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", publisher.getId());
        data.put("name", publisher.getPublisherName());
        data.put("address", publisher.getAddress());
        data.put("email", publisher.getEmail());
        data.put("website", publisher.getWebsite());
        data.put("status", publisher.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
        data.put("createdAt", formatTimestamp(publisher.getCreatedAt()));
        data.put("updatedAt", formatTimestamp(publisher.getUpdatedAt()));
        return data;
    }

    private Map<String, Object> mapVoucherToExcelData(Voucher voucher) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", voucher.getId());
        data.put("code", voucher.getCode());
        data.put("name", voucher.getName());
        data.put("description", voucher.getDescription());
        data.put("voucherCategory", voucher.getVoucherCategory() != null ? voucher.getVoucherCategory().name() : "");
        data.put("discountType", voucher.getDiscountType() != null ? voucher.getDiscountType().name() : "");
        data.put("discountPercentage", voucher.getDiscountPercentage());
        data.put("discountAmount", voucher.getDiscountAmount());
        data.put("minOrderValue", voucher.getMinOrderValue());
        data.put("maxDiscountValue", voucher.getMaxDiscountValue());
        data.put("usageLimit", voucher.getUsageLimit());
        data.put("usedCount", voucher.getUsedCount());
        data.put("startTime", formatTimestamp(voucher.getStartTime()));
        data.put("endTime", formatTimestamp(voucher.getEndTime()));
        data.put("status", voucher.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
        data.put("createdAt", formatTimestamp(voucher.getCreatedAt()));
        return data;
    }

    private Map<String, Object> mapSupplierToExcelData(Supplier supplier) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", supplier.getId());
        data.put("supplierName", supplier.getSupplierName());
        data.put("contactName", supplier.getContactName());
        data.put("phoneNumber", supplier.getPhoneNumber());
        data.put("email", supplier.getEmail());
        data.put("address", supplier.getAddress());
        data.put("status", supplier.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
        data.put("createdAt", formatTimestamp(supplier.getCreatedAt()));
        data.put("updatedAt", formatTimestamp(supplier.getUpdatedAt()));
        return data;
    }

    private Map<String, Object> mapPointToExcelData(Point point) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", point.getId());
        data.put("userName", point.getUser() != null ? point.getUser().getFullName() : "");
        data.put("userEmail", point.getUser() != null ? point.getUser().getEmail() : "");
        data.put("orderCode", point.getOrder() != null ? point.getOrder().getCode() : "");
        data.put("pointEarned", point.getPointEarned());
        data.put("minSpent", point.getMinSpent());
        data.put("pointSpent", point.getPointSpent());
        data.put("description", point.getDescription());
        data.put("status", point.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
        data.put("createdAt", formatTimestamp(point.getCreatedAt()));
        data.put("updatedAt", formatTimestamp(point.getUpdatedAt()));
        return data;
    }

    private Map<String, Object> mapFlashSaleToExcelData(FlashSale flashSale) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", flashSale.getId());
        data.put("name", flashSale.getName());
        data.put("startTime", formatTimestamp(flashSale.getStartTime()));
        data.put("endTime", formatTimestamp(flashSale.getEndTime()));
        data.put("status", flashSale.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
        data.put("createdAt", formatTimestamp(flashSale.getCreatedAt()));
        data.put("updatedAt", formatTimestamp(flashSale.getUpdatedAt()));
        return data;
    }

    // Fields mapping methods
    private Map<String, String> getRankFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Tên Hạng", "rankName");
        fields.put("Chi Tiêu Tối Thiểu", "minSpent");
        fields.put("Hệ Số Điểm", "pointMultiplier");
        fields.put("Trạng Thái", "status");
        fields.put("Ngày Tạo", "createdAt");
        fields.put("Ngày Cập Nhật", "updatedAt");
        return fields;
    }

    private Map<String, String> getBookFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Tên Sách", "bookName");
        fields.put("Tác Giả", "author");
        fields.put("Thể Loại", "category");
        fields.put("Nhà Xuất Bản", "publisher");
        fields.put("Giá Gốc", "price");
        fields.put("Giá Thực Tế", "effectivePrice");
        fields.put("Số Lượng Tồn Kho", "stockQuantity");
        fields.put("Định Dạng", "format");
        fields.put("Số Trang", "pageCount");
        fields.put("Ngôn Ngữ", "language");
        fields.put("ISBN", "isbn");
        fields.put("Ngày Xuất Bản", "publicationDate");
        fields.put("Trạng Thái", "status");
        fields.put("Ngày Tạo", "createdAt");
        return fields;
    }

    private Map<String, String> getUserFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Họ Tên", "fullName");
        fields.put("Email", "email");
        fields.put("Số Điện Thoại", "phone");
        fields.put("Điểm Tích Lũy", "totalPoint");
        fields.put("Tổng Chi Tiêu", "totalSpent");
        fields.put("Trạng Thái", "status");
        fields.put("Ngày Đăng Ký", "createdAt");
        return fields;
    }

    private Map<String, String> getOrderFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Mã Đơn Hàng", "code");
        fields.put("Tên Khách Hàng", "customerName");
        fields.put("Email Khách Hàng", "customerEmail");
        fields.put("Số Điện Thoại", "customerPhone");
        fields.put("Tổng Tiền Hàng", "subtotal");
        fields.put("Tổng Tiền", "totalAmount");
        fields.put("Phí Vận Chuyển", "shippingFee");
        fields.put("Giảm Giá", "discountAmount");
        fields.put("Loại Đơn Hàng", "orderType");
        fields.put("Trạng Thái", "status");
        fields.put("Phương Thức Thanh Toán", "paymentMethod");
        fields.put("Ngày Đặt Hàng", "orderDate");
        fields.put("Ghi Chú", "notes");
        return fields;
    }

    private Map<String, String> getReviewFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Tên Sách", "bookName");
        fields.put("Tên Người Dùng", "userName");
        fields.put("Email Người Dùng", "userEmail");
        fields.put("Số Sao", "rating");
        fields.put("Bình Luận", "comment");
        fields.put("Tính Chất", "isPositive");
        fields.put("Trạng Thái", "reviewStatus");
        fields.put("Ngày Đánh Giá", "reviewDate");
        fields.put("Ngày Tạo", "createdAt");
        return fields;
    }

    private Map<String, String> getCategoryFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Tên Thể Loại", "name");
        fields.put("Mô Tả", "description");
        fields.put("Trạng Thái", "status");
        fields.put("Ngày Tạo", "createdAt");
        fields.put("Ngày Cập Nhật", "updatedAt");
        return fields;
    }

    private Map<String, String> getAuthorFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Tên Tác Giả", "name");
        fields.put("Tiểu Sử", "biography");
        fields.put("Ngày Sinh", "birthDate");
        fields.put("Trạng Thái", "status");
        fields.put("Ngày Tạo", "createdAt");
        fields.put("Ngày Cập Nhật", "updatedAt");
        return fields;
    }

    private Map<String, String> getPublisherFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Tên Nhà Xuất Bản", "name");
        fields.put("Địa Chỉ", "address");
        fields.put("Email", "email");
        fields.put("Website", "website");
        fields.put("Trạng Thái", "status");
        fields.put("Ngày Tạo", "createdAt");
        fields.put("Ngày Cập Nhật", "updatedAt");
        return fields;
    }

    private Map<String, String> getVoucherFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Mã Voucher", "code");
        fields.put("Tên Voucher", "name");
        fields.put("Mô Tả", "description");
        fields.put("Loại Voucher", "voucherCategory");
        fields.put("Kiểu Giảm Giá", "discountType");
        fields.put("Phần Trăm Giảm", "discountPercentage");
        fields.put("Số Tiền Giảm", "discountAmount");
        fields.put("Đơn Hàng Tối Thiểu", "minOrderValue");
        fields.put("Giảm Tối Đa", "maxDiscountValue");
        fields.put("Giới Hạn Sử Dụng", "usageLimit");
        fields.put("Đã Sử Dụng", "usedCount");
        fields.put("Thời Gian Bắt Đầu", "startTime");
        fields.put("Thời Gian Kết Thúc", "endTime");
        fields.put("Trạng Thái", "status");
        fields.put("Ngày Tạo", "createdAt");
        return fields;
    }

    private Map<String, String> getSupplierFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Tên Nhà Cung Cấp", "supplierName");
        fields.put("Tên Liên Hệ", "contactName");
        fields.put("Số Điện Thoại", "phoneNumber");
        fields.put("Email", "email");
        fields.put("Địa Chỉ", "address");
        fields.put("Trạng Thái", "status");
        fields.put("Ngày Tạo", "createdAt");
        fields.put("Ngày Cập Nhật", "updatedAt");
        return fields;
    }

    private Map<String, String> getPointFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Tên Người Dùng", "userName");
        fields.put("Email Người Dùng", "userEmail");
        fields.put("Mã Đơn Hàng", "orderCode");
        fields.put("Điểm Kiếm Được", "pointEarned");
        fields.put("Chi Tiêu Tối Thiểu", "minSpent");
        fields.put("Điểm Đã Tiêu", "pointSpent");
        fields.put("Mô Tả", "description");
        fields.put("Trạng Thái", "status");
        fields.put("Ngày Tạo", "createdAt");
        fields.put("Ngày Cập Nhật", "updatedAt");
        return fields;
    }

    private Map<String, String> getFlashSaleFieldsMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "id");
        fields.put("Tên Flash Sale", "name");
        fields.put("Thời Gian Bắt Đầu", "startTime");
        fields.put("Thời Gian Kết Thúc", "endTime");
        fields.put("Trạng Thái", "status");
        fields.put("Ngày Tạo", "createdAt");
        fields.put("Ngày Cập Nhật", "updatedAt");
        return fields;
    }

    // Helper methods
    private String getAuthorNames(Book book) {
        if (book.getAuthorBooks() != null && !book.getAuthorBooks().isEmpty()) {
            return book.getAuthorBooks().stream()
                    .map(authorBook -> authorBook.getAuthor().getAuthorName())
                    .collect(Collectors.joining(", "));
        }
        return "";
    }

    private String getOrderStatusInVietnamese(org.datn.bookstation.entity.enums.OrderStatus status) {
        if (status == null) return "";
        switch (status) {
            case PENDING: return "Chờ xử lý";
            case CONFIRMED: return "Đã xác nhận";
            case SHIPPED: return "Đang giao hàng";
            case DELIVERED: return "Đã giao hàng";
            case CANCELED: return "Đã hủy";
            case REFUNDED: return "Đã hoàn tiền";
            default: return status.name();
        }
    }

    private String getReviewStatusInVietnamese(ReviewStatus status) {
        if (status == null) return "";
        switch (status) {
            case PENDING: return "Chờ duyệt";
            case APPROVED: return "Đã duyệt";
            case HIDDEN: return "Đã ẩn";
            case EDITED: return "Đã chỉnh sửa";
            default: return status.name();
        }
    }
}
