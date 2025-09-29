package org.datn.bookstation.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Utility class để convert giữa LocalDate và Long timestamp
 * Được sử dụng cho trường publicationDate
 */
public class DateTimeUtil {
    
    /**
     * Convert LocalDate sang Long timestamp (milliseconds)
     * @param date LocalDate cần convert
     * @return timestamp in milliseconds, null nếu date là null
     */
    public static Long dateToTimestamp(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }
    
    /**
     * Convert Long timestamp (milliseconds) sang LocalDate
     * @param timestamp timestamp in milliseconds
     * @return LocalDate, null nếu timestamp là null
     */
    public static LocalDate timestampToDate(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC).toLocalDate();
    }
    
    /**
     * Tạo timestamp cho ngày hiện tại
     * @return timestamp của ngày hiện tại
     */
    public static Long nowTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * Tạo timestamp cho ngày cụ thể
     * @param year năm
     * @param month tháng (1-12)
     * @param day ngày
     * @return timestamp
     */
    public static Long createTimestamp(int year, int month, int day) {
        return dateToTimestamp(LocalDate.of(year, month, day));
    }
}
