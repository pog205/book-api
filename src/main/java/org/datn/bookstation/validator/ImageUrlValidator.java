package org.datn.bookstation.validator;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImageUrlValidator {
    private static final int MAX_IMAGES = 5;
    private static final int MAX_URL_LENGTH = 500;
    private static final int MAX_TOTAL_LENGTH = 2000;
    
    public void validate(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return; // OK - no images
        }
        
        // Check số lượng ảnh
        if (imageUrls.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images allowed");
        }
        
        // Check độ dài mỗi URL
        for (String url : imageUrls) {
            if (url != null && url.length() > MAX_URL_LENGTH) {
                throw new IllegalArgumentException("URL too long: " + url);
            }
        }
        
        // Check tổng độ dài khi join
        String combined = String.join(",", imageUrls);
        if (combined.length() > MAX_TOTAL_LENGTH) {
            throw new IllegalArgumentException("Total URLs length too long");
        }
    }
}
