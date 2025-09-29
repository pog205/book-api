package org.datn.bookstation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.constants.UploadModule;
import org.datn.bookstation.configuration.UploadProperties;
import org.datn.bookstation.exception.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final UploadProperties uploadProperties;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MIN_WIDTH = 200;
    private static final int MIN_HEIGHT = 200;
    private static final int MAX_FILES = 5;

    private final SecureRandom random = new SecureRandom();

    public List<String> saveImages(MultipartFile[] files, String module) {
        if (files == null || files.length == 0) {
            throw new FileUploadException("No files provided", "NO_FILES");
        }

        if (files.length > MAX_FILES) {
            throw new FileUploadException("Too many files. Maximum " + MAX_FILES + " files allowed", "TOO_MANY_FILES");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String url = saveImage(file, module);
                urls.add(url);
            }
        }

        return urls;
    }

    public String saveImage(MultipartFile file, String module) {
        validateImage(file);
        validateModule(module);

        try {
            // Create directory structure: uploads/{module}/2025/06/
            LocalDateTime now = LocalDateTime.now();
            String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
            String month = now.format(DateTimeFormatter.ofPattern("MM"));
            
            String relativePath = module + "/" + year + "/" + month + "/";
            Path uploadDir = Paths.get(uploadProperties.getPath(), relativePath);// nối đường dẫn ==> đối tượng Path(vị  trí của file ảnh)
            
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generate unique filename: image{timestamp}_{randomId}.{extension}
            String originalFilename = file.getOriginalFilename();//tên file gốc từ client
            String extension = getFileExtension(originalFilename);
            String filename = generateFilename(extension);// tạo tên file mới với định dạng: image{timestamp}_{randomId}.{extension}
            
            Path filePath = uploadDir.resolve(filename);//nối thêm tên file.
            
            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return URL
            String url = uploadProperties.getBaseUrl() + relativePath + filename;
            log.info("File uploaded successfully for module {}: {}", module, url);
            
            return url;
            
        } catch (IOException e) {
            log.error("Error saving file for module {}: {}", module, e.getMessage());
            throw new FileUploadException("Failed to save file", "SAVE_ERROR");
        }
    }

    // Backward compatibility methods for events
    public List<String> saveEventImages(MultipartFile[] files) {
        return saveImages(files, "events");
    }

    public String saveEventImage(MultipartFile file) {
        return saveImage(file, "events");
    }

    // ✅ THÊM MỚI: Video upload methods
    public List<String> saveVideos(MultipartFile[] files, String module) {
        if (files == null || files.length == 0) {
            throw new FileUploadException("No videos provided", "NO_FILES");
        }

        if (files.length > MAX_FILES) {
            throw new FileUploadException("Too many videos. Maximum " + MAX_FILES + " videos allowed", "TOO_MANY_FILES");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String url = saveVideo(file, module);
                urls.add(url);
            }
        }

        return urls;
    }

    public String saveVideo(MultipartFile file, String module) {
        validateVideo(file);
        validateModule(module);

        try {
            // Create directory structure: uploads/{module}/2025/06/
            LocalDateTime now = LocalDateTime.now();
            String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
            String month = now.format(DateTimeFormatter.ofPattern("MM"));
            
            String relativePath = module + "/" + year + "/" + month + "/";
            Path uploadDir = Paths.get(uploadProperties.getPath(), relativePath);
            
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generate unique filename: video{timestamp}_{randomId}.{extension}
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = generateVideoFilename(extension);
            
            Path filePath = uploadDir.resolve(filename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return URL
            String url = uploadProperties.getBaseUrl() + relativePath + filename;
            log.info("Video uploaded successfully for module {}: {}", module, url);
            
            return url;
            
        } catch (IOException e) {
            log.error("Error saving video for module {}: {}", module, e.getMessage());
            throw new FileUploadException("Failed to save video", "SAVE_ERROR");
        }
    }

    public boolean deleteImage(String imageUrl) {
        try {
            // Extract relative path from URL
            String baseUrl = uploadProperties.getBaseUrl();
            if (!imageUrl.startsWith(baseUrl)) {
                throw new FileUploadException("Invalid image URL", "INVALID_URL");
            }
            
            String relativePath = imageUrl.substring(baseUrl.length());
            Path filePath = Paths.get(uploadProperties.getPath(), relativePath);//vị trí file trên server 
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", imageUrl);
                return true;
            } else {
                log.warn("File not found for deletion: {}", imageUrl);
                return false;
            }
            
        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage());
            throw new FileUploadException("Failed to delete file", "DELETE_ERROR");
        }
    }

    private void validateImage(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new FileUploadException("File is empty", "EMPTY_FILE");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size too large. Maximum size is 5MB.", "FILE_TOO_LARGE");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new FileUploadException("File type not allowed. Please upload JPG, PNG, GIF, or WebP images.", "INVALID_FILE_TYPE");
        }

        // Check image dimensions
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new FileUploadException("Invalid image file", "INVALID_IMAGE");
            }
            
            if (image.getWidth() < MIN_WIDTH || image.getHeight() < MIN_HEIGHT) {
                throw new FileUploadException("Image dimensions too small. Minimum size is 200x200px.", "INVALID_DIMENSIONS");
            }
            
        } catch (IOException e) {
            throw new FileUploadException("Unable to read image file", "INVALID_IMAGE");
        }
    }

    // ✅ THÊM MỚI: Video validation
    private void validateVideo(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new FileUploadException("Video file is empty", "EMPTY_FILE");
        }

        // Check file size (50MB limit for video)
        long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new FileUploadException("Video file too large. Maximum size is 50MB.", "FILE_TOO_LARGE");
        }

        // Check content type
        String contentType = file.getContentType();
        List<String> allowedVideoTypes = List.of("video/mp4", "video/avi", "video/mov", "video/wmv", "video/mkv");
        if (contentType == null || !allowedVideoTypes.contains(contentType.toLowerCase())) {
            throw new FileUploadException("Video type not allowed. Please upload MP4, AVI, MOV, WMV, or MKV videos.", "INVALID_FILE_TYPE");
        }
    }

    private void validateModule(String module) {
        if (module == null || module.trim().isEmpty()) {
            throw new FileUploadException("Module name is required", "INVALID_MODULE");
        }
        
        if (!UploadModule.isValidModule(module)) {
            throw new FileUploadException("Invalid module. Allowed modules: " + String.join(", ", UploadModule.getAllowedModules()), "INVALID_MODULE");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg"; // default extension
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String generateFilename(String extension) {
        long timestamp = System.currentTimeMillis();
        String randomId = generateRandomString(12);
        return "image" + timestamp + "_" + randomId + "." + extension;
    }

    // ✅ THÊM MỚI: Generate video filename
    private String generateVideoFilename(String extension) {
        long timestamp = System.currentTimeMillis();
        String randomId = generateRandomString(12);
        return "video" + timestamp + "_" + randomId + "." + extension;
    }

    private String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
