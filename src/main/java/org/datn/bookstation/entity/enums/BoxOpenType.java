package org.datn.bookstation.entity.enums;

/**
 * Enum cho loại mở hộp
 */
public enum BoxOpenType {
    FREE("Miễn phí"),
    POINT("Dùng điểm");

    private final String description;

    BoxOpenType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
