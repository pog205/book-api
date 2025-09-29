package org.datn.bookstation.exception;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Global Exception Handler để xử lý các lỗi chung cho toàn bộ ứng dụng
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Xử lý lỗi validation từ @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ValidationErrorResponse>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ValidationErrorResponse> errors = new ArrayList<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            errors.add(new ValidationErrorResponse(fieldName, errorMessage, rejectedValue));
        });
        
        ApiResponse<List<ValidationErrorResponse>> response = new ApiResponse<>(
            400, 
            "Dữ liệu không hợp lệ", 
            errors
        );
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Xử lý lỗi JSON không thể parse (malformed JSON)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleJSONParseError(HttpMessageNotReadableException ex) {
        String message = "Dữ liệu JSON không hợp lệ";
        
        // Phân tích lỗi cụ thể
        if (ex.getMessage().contains("JSON parse error")) {
            message = "Cú pháp JSON không đúng";
        } else if (ex.getMessage().contains("Cannot deserialize")) {
            // Kiểm tra lỗi enum
            if (ex.getMessage().contains("EventType")) {
                message = "Giá trị eventType không hợp lệ. Các giá trị cho phép: BOOK_LAUNCH, AUTHOR_MEET, READING_CHALLENGE, BOOK_FAIR, SEASONAL_EVENT, PROMOTION, CONTEST, WORKSHOP, DAILY_CHECKIN, LOYALTY_PROGRAM, POINT_EARNING, OTHER";
            } else if (ex.getMessage().contains("EventStatus")) {
                message = "Giá trị status không hợp lệ. Các giá trị cho phép: DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED";
            } else {
                message = "Không thể chuyển đổi dữ liệu JSON";
            }
        } else if (ex.getMessage().contains("Required request body is missing")) {
            message = "Thiếu dữ liệu trong request body";
        }
        
        ApiResponse<Object> response = new ApiResponse<>(400, message, null);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Xử lý lỗi file upload quá lớn
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        ApiResponse<Object> response = new ApiResponse<>(
            400,
            "File quá lớn. Kích thước tối đa là 5MB cho mỗi file.",
            null
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Xử lý lỗi file upload
     */
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileUploadException(FileUploadException ex) {
        ApiResponse<Object> response = new ApiResponse<>(400, ex.getMessage(), null);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Xử lý lỗi business logic
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        ApiResponse<Object> response = new ApiResponse<>(400, ex.getMessage(), null);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Xử lý lỗi chung không xác định
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        // In stack-trace ra terminal và ghi log lỗi
        log.error("Unhandled exception", ex);
        ex.printStackTrace();
        ApiResponse<Object> response = new ApiResponse<>(
            500, 
            "Lỗi hệ thống: " + ex.getMessage(), 
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
