package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.PdfResponse;

public interface PdfService {
    /**
     * Tạo PDF hóa đơn từ orderId
     */
    byte[] generateInvoicePdf(Integer orderId) throws Exception;

    /**
     * Tạo PDF hóa đơn từ dữ liệu có sẵn
     */
    byte[] generateInvoicePdf(PdfResponse invoiceData) throws Exception;

    /**
     * Lấy dữ liệu hóa đơn để tạo PDF
     */
    PdfResponse getInvoiceData(Integer orderId);
}