package org.datn.bookstation.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Test class để verify việc chuyển đổi publicationDate từ LocalDate sang Long timestamp
 */
public class PublicationDateConversionTest {
    
    @Test
    public void testDateToTimestampConversion() {
        // Test conversion từ LocalDate sang Long timestamp
        LocalDate testDate = LocalDate.of(2010, 1, 1);
        long expectedTimestamp = testDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        
        // Verify timestamp = 1262304000000L (2010-01-01 00:00:00 UTC)
        assertEquals(1262304000000L, expectedTimestamp);
        
        System.out.println("✅ Date conversion test passed!");
        System.out.println("LocalDate.of(2010, 1, 1) = " + expectedTimestamp + "L");
    }
    
    @Test
    public void testTimestampToDateConversion() {
        // Test conversion từ Long timestamp về LocalDate
        long timestamp = 1262304000000L; // 2010-01-01
        LocalDate convertedDate = LocalDate.ofEpochDay(timestamp / (24 * 60 * 60 * 1000));
        
        assertEquals(LocalDate.of(2010, 1, 1), convertedDate);
        
        System.out.println("✅ Timestamp conversion test passed!");
        System.out.println("Timestamp " + timestamp + "L = " + convertedDate);
    }
    
    @Test
    public void testAllPublicationDatesUsedInDataInit() {
        System.out.println("📚 Publication dates used in DataInitializationService:");
        System.out.println("2010-01-01 = " + getTimestamp(2010, 1, 1) + "L");
        System.out.println("1941-01-01 = " + getTimestamp(1941, 1, 1) + "L");
        System.out.println("1987-01-01 = " + getTimestamp(1987, 1, 1) + "L");
        System.out.println("1934-01-01 = " + getTimestamp(1934, 1, 1) + "L");
        System.out.println("1997-06-26 = " + getTimestamp(1997, 6, 26) + "L");
        System.out.println("1936-01-01 = " + getTimestamp(1936, 1, 1) + "L");
        System.out.println("1937-01-01 = " + getTimestamp(1937, 1, 1) + "L");
        System.out.println("2020-01-01 = " + getTimestamp(2020, 1, 1) + "L");
        System.out.println("2018-01-01 = " + getTimestamp(2018, 1, 1) + "L");
        System.out.println("1970-01-01 = " + getTimestamp(1970, 1, 1) + "L");
        System.out.println("2017-01-01 = " + getTimestamp(2017, 1, 1) + "L");
    }
    
    private long getTimestamp(int year, int month, int day) {
        return LocalDate.of(year, month, day)
                       .atStartOfDay(ZoneOffset.UTC)
                       .toInstant()
                       .toEpochMilli();
    }
}
