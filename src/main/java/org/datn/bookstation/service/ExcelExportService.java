package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.ExcelDataResponse;
import org.datn.bookstation.dto.response.ExcelFieldsResponse;

public interface ExcelExportService {
    
    // Data export methods
    ApiResponse<ExcelDataResponse> getRanksForExport();
    ApiResponse<ExcelDataResponse> getBooksForExport();
    ApiResponse<ExcelDataResponse> getUsersForExport();
    ApiResponse<ExcelDataResponse> getOrdersForExport();
    ApiResponse<ExcelDataResponse> getReviewsForExport();
    ApiResponse<ExcelDataResponse> getCategoriesForExport();
    ApiResponse<ExcelDataResponse> getAuthorsForExport();
    ApiResponse<ExcelDataResponse> getPublishersForExport();
    ApiResponse<ExcelDataResponse> getVouchersForExport();
    ApiResponse<ExcelDataResponse> getSuppliersForExport();
    ApiResponse<ExcelDataResponse> getPointsForExport();
    ApiResponse<ExcelDataResponse> getFlashSalesForExport();
    
    // Fields mapping methods
    ApiResponse<ExcelFieldsResponse> getRankFieldsMapping();
    ApiResponse<ExcelFieldsResponse> getBookFieldsMapping();
    ApiResponse<ExcelFieldsResponse> getUserFieldsMapping();
    ApiResponse<ExcelFieldsResponse> getOrderFieldsMapping();
    ApiResponse<ExcelFieldsResponse> getReviewFieldsMapping();
    ApiResponse<ExcelFieldsResponse> getCategoryFieldsMapping();
    ApiResponse<ExcelFieldsResponse> getAuthorFieldsMapping();
    ApiResponse<ExcelFieldsResponse> getPublisherFieldsMapping();
    ApiResponse<ExcelFieldsResponse> getVoucherFieldsMapping();
    ApiResponse<ExcelFieldsResponse> getSupplierFieldsMapping();
    ApiResponse<ExcelFieldsResponse> getPointFieldsMapping();
    ApiResponse<ExcelFieldsResponse> getFlashSaleFieldsMapping();
}
