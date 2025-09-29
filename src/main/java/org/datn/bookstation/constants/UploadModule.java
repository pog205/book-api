package org.datn.bookstation.constants;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum defining all allowed upload modules in the system
 */
public enum UploadModule {
    EVENTS("events"),
    USERS("users"),
    PRODUCTS("products"),
    CATEGORIES("categories"),
    ORDERS("orders"),
    REVIEWS("reviews"),
    REFUND_EVIDENCE("refund-evidence");

    private final String value;

    UploadModule(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get all allowed module values as a Set
     */
    public static Set<String> getAllowedModules() {
        return Arrays.stream(UploadModule.values())
                .map(UploadModule::getValue)
                .collect(Collectors.toSet());
    }

    /**
     * Check if a module is valid
     */
    public static boolean isValidModule(String module) {
        return getAllowedModules().contains(module.toLowerCase());
    }

    /**
     * Get module enum from string value
     */
    public static UploadModule fromValue(String value) {
        return Arrays.stream(UploadModule.values())
                .filter(module -> module.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid module: " + value));
    }

    @Override
    public String toString() {
        return value;
    }
}
