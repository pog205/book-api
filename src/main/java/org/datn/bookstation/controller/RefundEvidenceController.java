package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller xử lý upload ảnh và video cho việc hoàn hàng
 */
@RestController
@RequestMapping("/api/refund-evidence")
@AllArgsConstructor
public class RefundEvidenceController {
    
    private final FileUploadService fileUploadService;

    /**
     * Upload ảnh minh chứng cho việc hoàn hàng
     * @param files Danh sách file ảnh (tối đa 10 ảnh)
     * @return Danh sách đường dẫn ảnh đã upload
     */
    @PostMapping("/images")
    public ResponseEntity<ApiResponse<List<String>>> uploadRefundImages(
            @RequestParam("files") MultipartFile[] files) {
        
        // Validation số lượng file
        if (files.length > 10) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                    "Chỉ được upload tối đa 10 ảnh minh chứng", null));
        }
        
        List<String> uploadedPaths = new ArrayList<>();
        
        try {
            for (MultipartFile file : files) {
                // Validation loại file
                if (!isImageFile(file)) {
                    return ResponseEntity.badRequest().body(
                        new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                            "File " + file.getOriginalFilename() + " không phải là ảnh hợp lệ", null));
                }
                
                // Validation kích thước file (tối đa 5MB)
                if (file.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body(
                        new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                            "Kích thước ảnh " + file.getOriginalFilename() + " vượt quá 5MB", null));
                }
                
                // Upload file với folder riêng cho refund evidence
                String uploadPath = fileUploadService.saveImage(file, "refund-evidence");
                uploadedPaths.add(uploadPath);
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), 
                    "Upload ảnh minh chứng thành công", uploadedPaths));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Lỗi khi upload ảnh: " + e.getMessage(), null));
        }
    }

    /**
     * Upload video minh chứng cho việc hoàn hàng
     * @param files Danh sách file video (tối đa 3 video)
     * @return Danh sách đường dẫn video đã upload
     */
    @PostMapping("/videos")
    public ResponseEntity<ApiResponse<List<String>>> uploadRefundVideos(
            @RequestParam("files") MultipartFile[] files) {
        
        // Validation số lượng file
        if (files.length > 3) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                    "Chỉ được upload tối đa 3 video minh chứng", null));
        }
        
        List<String> uploadedPaths = new ArrayList<>();
        
        try {
            for (MultipartFile file : files) {
                // Validation loại file
                if (!isVideoFile(file)) {
                    return ResponseEntity.badRequest().body(
                        new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                            "File " + file.getOriginalFilename() + " không phải là video hợp lệ", null));
                }
                
                // Validation kích thước file (tối đa 50MB)
                if (file.getSize() > 50 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body(
                        new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                            "Kích thước video " + file.getOriginalFilename() + " vượt quá 50MB", null));
                }
                
                // Upload file với folder riêng cho refund evidence
                String uploadPath = fileUploadService.saveVideo(file, "refund-evidence");
                uploadedPaths.add(uploadPath);
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), 
                    "Upload video minh chứng thành công", uploadedPaths));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Lỗi khi upload video: " + e.getMessage(), null));
        }
    }

    /**
     * Upload cả ảnh và video cùng lúc
     * @param images Danh sách file ảnh
     * @param videos Danh sách file video
     * @return Danh sách đường dẫn đã upload
     */
    @PostMapping("/mixed")
    public ResponseEntity<ApiResponse<RefundEvidenceUploadResponse>> uploadRefundEvidence(
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "videos", required = false) MultipartFile[] videos) {
        
        RefundEvidenceUploadResponse response = new RefundEvidenceUploadResponse();
        
        try {
            // Upload ảnh nếu có
            if (images != null && images.length > 0) {
                if (images.length > 10) {
                    return ResponseEntity.badRequest().body(
                        new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                            "Chỉ được upload tối đa 10 ảnh minh chứng", null));
                }
                
                List<String> imagePaths = new ArrayList<>();
                for (MultipartFile file : images) {
                    if (!isImageFile(file)) {
                        return ResponseEntity.badRequest().body(
                            new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                                "File " + file.getOriginalFilename() + " không phải là ảnh hợp lệ", null));
                    }
                    if (file.getSize() > 5 * 1024 * 1024) {
                        return ResponseEntity.badRequest().body(
                            new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                                "Kích thước ảnh " + file.getOriginalFilename() + " vượt quá 5MB", null));
                    }
                    String uploadPath = fileUploadService.saveImage(file, "refund-evidence");
                    imagePaths.add(uploadPath);
                }
                response.setImagePaths(imagePaths);
            }
            
            // Upload video nếu có
            if (videos != null && videos.length > 0) {
                if (videos.length > 3) {
                    return ResponseEntity.badRequest().body(
                        new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                            "Chỉ được upload tối đa 3 video minh chứng", null));
                }
                
                List<String> videoPaths = new ArrayList<>();
                for (MultipartFile file : videos) {
                    if (!isVideoFile(file)) {
                        return ResponseEntity.badRequest().body(
                            new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                                "File " + file.getOriginalFilename() + " không phải là video hợp lệ", null));
                    }
                    if (file.getSize() > 50 * 1024 * 1024) {
                        return ResponseEntity.badRequest().body(
                            new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), 
                                "Kích thước video " + file.getOriginalFilename() + " vượt quá 50MB", null));
                    }
                    String uploadPath = fileUploadService.saveVideo(file, "refund-evidence");
                    videoPaths.add(uploadPath);
                }
                response.setVideoPaths(videoPaths);
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), 
                    "Upload minh chứng hoàn hàng thành công", response));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Lỗi khi upload minh chứng: " + e.getMessage(), null));
        }
    }

    // ===================== HELPER METHODS =====================

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;
        return contentType.startsWith("image/") && 
               (contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp"));
    }

    private boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;
        return contentType.startsWith("video/") && 
               (contentType.equals("video/mp4") ||
                contentType.equals("video/avi") ||
                contentType.equals("video/mov") ||
                contentType.equals("video/wmv") ||
                contentType.equals("video/webm"));
    }

    // ===================== INNER CLASS =====================

    public static class RefundEvidenceUploadResponse {
        private List<String> imagePaths = new ArrayList<>();
        private List<String> videoPaths = new ArrayList<>();

        public List<String> getImagePaths() { return imagePaths; }
        public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }
        public List<String> getVideoPaths() { return videoPaths; }
        public void setVideoPaths(List<String> videoPaths) { this.videoPaths = videoPaths; }
    }
}
