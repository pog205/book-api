package org.datn.bookstation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.DeleteImageRequest;
import org.datn.bookstation.dto.response.DeleteResponse;
import org.datn.bookstation.dto.response.MultipleUploadResponse;
import org.datn.bookstation.dto.response.SingleUploadResponse;
import org.datn.bookstation.exception.FileUploadException;
import org.datn.bookstation.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:5173")
public class UploadController {

    private final FileUploadService fileUploadService;

    // Generic endpoints for all modules
    @PostMapping("/images/{module}")
    public ResponseEntity<?> uploadImages(
            @PathVariable String module,
            @RequestParam("images") MultipartFile[] files) {
        try {
            List<String> urls = fileUploadService.saveImages(files, module);
            MultipleUploadResponse response = new MultipleUploadResponse(true, urls, "Upload successful");
            return ResponseEntity.ok(response);
            
        } catch (FileUploadException e) {
            log.error("Upload error for module {}: {}", module, e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), e.getErrorCode()));
        } catch (Exception e) {
            log.error("Unexpected error during upload for module {}: {}", module, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
    }

    // ✅ THÊM MỚI: Upload video cho refund evidence
    @PostMapping("/videos/{module}")
    public ResponseEntity<?> uploadVideos(
            @PathVariable String module,
            @RequestParam("videos") MultipartFile[] files) {
        try {
            List<String> urls = fileUploadService.saveVideos(files, module);
            MultipleUploadResponse response = new MultipleUploadResponse(true, urls, "Video upload successful");
            return ResponseEntity.ok(response);
            
        } catch (FileUploadException e) {
            log.error("Video upload error for module {}: {}", module, e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), e.getErrorCode()));
        } catch (Exception e) {
            log.error("Unexpected error during video upload for module {}: {}", module, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/video/{module}")
    public ResponseEntity<?> uploadVideo(
            @PathVariable String module,
            @RequestParam("video") MultipartFile file) {
        try {
            String url = fileUploadService.saveVideo(file, module);
            SingleUploadResponse response = new SingleUploadResponse(true, url, "Video upload successful");
            return ResponseEntity.ok(response);
            
        } catch (FileUploadException e) {
            log.error("Video upload error for module {}: {}", module, e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), e.getErrorCode()));
        } catch (Exception e) {
            log.error("Unexpected error during video upload for module {}: {}", module, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/image/{module}")
    public ResponseEntity<?> uploadImage(
            @PathVariable String module,
            @RequestParam("image") MultipartFile file) {
        try {
            String url = fileUploadService.saveImage(file, module);
            SingleUploadResponse response = new SingleUploadResponse(true, url, "Upload successful");
            return ResponseEntity.ok(response);
            
        } catch (FileUploadException e) {
            log.error("Upload error for module {}: {}", module, e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), e.getErrorCode()));
        } catch (Exception e) {
            log.error("Unexpected error during upload for module {}: {}", module, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
    }

    @DeleteMapping("/image")
    public ResponseEntity<?> deleteImage(@RequestBody DeleteImageRequest request) {
        try {
            boolean deleted = fileUploadService.deleteImage(request.getImageUrl());
            if (deleted) {
                DeleteResponse response = new DeleteResponse(true, "Image deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                DeleteResponse response = new DeleteResponse(false, "Image not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (FileUploadException e) {
            log.error("Delete error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), e.getErrorCode()));
        } catch (Exception e) {
            log.error("Unexpected error during delete: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
    }

    // Backward compatibility endpoints for events (deprecated but kept for existing clients)
    @PostMapping("/event-images")
    @Deprecated
    public ResponseEntity<?> uploadEventImages(@RequestParam("images") MultipartFile[] files) {
        return uploadImages("events", files);
    }

    @PostMapping("/event-image")
    @Deprecated
    public ResponseEntity<?> uploadEventImage(@RequestParam("image") MultipartFile file) {
        return uploadImage("events", file);
    }

    @DeleteMapping("/event-image")
    @Deprecated
    public ResponseEntity<?> deleteEventImage(@RequestBody DeleteImageRequest request) {
        return deleteImage(request);
    }

    // Inner class for error responses
    private static class ErrorResponse {
        private final String error;
        private final String code;

        public ErrorResponse(String error, String code) {
            this.error = error;
            this.code = code;
        }

        public String getError() {
            return error;
        }

        public String getCode() {
            return code;
        }
    }
}
