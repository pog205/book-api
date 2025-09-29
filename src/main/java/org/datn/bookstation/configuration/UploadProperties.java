package org.datn.bookstation.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {
    private String path = "uploads/";
    private String baseUrl = "http://localhost:8080/uploads/";
}
