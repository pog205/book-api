package org.datn.bookstation;

import org.datn.bookstation.configuration.UploadProperties;
import org.datn.bookstation.configuration.VnPayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({UploadProperties.class, VnPayProperties.class})
public class BookStationApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookStationApplication.class, args);
        System.out.println("BookStation Application is running...");
    }

}
