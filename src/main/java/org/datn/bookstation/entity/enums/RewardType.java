package org.datn.bookstation.entity.enums;

/**
 * Enum cho loại phần thưởng trong minigame mở hộp
 */
public enum RewardType {
    VOUCHER("Voucher"),
    POINTS("Điểm"), 
    NONE("Không trúng");

    private final String description;

    RewardType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
