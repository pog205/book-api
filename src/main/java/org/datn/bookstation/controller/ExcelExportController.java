package org.datn.bookstation.controller;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.ExcelDataResponse;
import org.datn.bookstation.dto.response.ExcelFieldsResponse;
import org.datn.bookstation.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/excel")
public class ExcelExportController {

    @Autowired
    private ExcelExportService excelExportService;

    @GetMapping("/ranks")
    public ApiResponse<ExcelDataResponse> exportRanks() {
        return excelExportService.getRanksForExport();
    }

    @GetMapping("/books")
    public ApiResponse<ExcelDataResponse> exportBooks() {
        return excelExportService.getBooksForExport();
    }

    @GetMapping("/users")
    public ApiResponse<ExcelDataResponse> exportUsers() {
        return excelExportService.getUsersForExport();
    }

    @GetMapping("/orders")
    public ApiResponse<ExcelDataResponse> exportOrders() {
        return excelExportService.getOrdersForExport();
    }

    @GetMapping("/reviews")
    public ApiResponse<ExcelDataResponse> exportReviews() {
        return excelExportService.getReviewsForExport();
    }

    @GetMapping("/categories")
    public ApiResponse<ExcelDataResponse> exportCategories() {
        return excelExportService.getCategoriesForExport();
    }

    @GetMapping("/authors")
    public ApiResponse<ExcelDataResponse> exportAuthors() {
        return excelExportService.getAuthorsForExport();
    }

    @GetMapping("/publishers")
    public ApiResponse<ExcelDataResponse> exportPublishers() {
        return excelExportService.getPublishersForExport();
    }

    @GetMapping("/vouchers")
    public ApiResponse<ExcelDataResponse> exportVouchers() {
        return excelExportService.getVouchersForExport();
    }

    @GetMapping("/suppliers")
    public ApiResponse<ExcelDataResponse> exportSuppliers() {
        return excelExportService.getSuppliersForExport();
    }

    @GetMapping("/points")
    public ApiResponse<ExcelDataResponse> exportPoints() {
        return excelExportService.getPointsForExport();
    }

    @GetMapping("/flashsales")
    public ApiResponse<ExcelDataResponse> exportFlashSales() {
        return excelExportService.getFlashSalesForExport();
    }

    @GetMapping("/fields/ranks")
    public ApiResponse<ExcelFieldsResponse> getRankFields() {
        return excelExportService.getRankFieldsMapping();
    }

    @GetMapping("/fields/books")
    public ApiResponse<ExcelFieldsResponse> getBookFields() {
        return excelExportService.getBookFieldsMapping();
    }

    @GetMapping("/fields/users")
    public ApiResponse<ExcelFieldsResponse> getUserFields() {
        return excelExportService.getUserFieldsMapping();
    }

    @GetMapping("/fields/orders")
    public ApiResponse<ExcelFieldsResponse> getOrderFields() {
        return excelExportService.getOrderFieldsMapping();
    }

    @GetMapping("/fields/reviews")
    public ApiResponse<ExcelFieldsResponse> getReviewFields() {
        return excelExportService.getReviewFieldsMapping();
    }

    @GetMapping("/fields/categories")
    public ApiResponse<ExcelFieldsResponse> getCategoryFields() {
        return excelExportService.getCategoryFieldsMapping();
    }

    @GetMapping("/fields/authors")
    public ApiResponse<ExcelFieldsResponse> getAuthorFields() {
        return excelExportService.getAuthorFieldsMapping();
    }

    @GetMapping("/fields/publishers")
    public ApiResponse<ExcelFieldsResponse> getPublisherFields() {
        return excelExportService.getPublisherFieldsMapping();
    }

    @GetMapping("/fields/vouchers")
    public ApiResponse<ExcelFieldsResponse> getVoucherFields() {
        return excelExportService.getVoucherFieldsMapping();
    }

    @GetMapping("/fields/suppliers")
    public ApiResponse<ExcelFieldsResponse> getSupplierFields() {
        return excelExportService.getSupplierFieldsMapping();
    }

    @GetMapping("/fields/points")
    public ApiResponse<ExcelFieldsResponse> getPointFields() {
        return excelExportService.getPointFieldsMapping();
    }

    @GetMapping("/fields/flashsales")
    public ApiResponse<ExcelFieldsResponse> getFlashSaleFields() {
        return excelExportService.getFlashSaleFieldsMapping();
    }
}
