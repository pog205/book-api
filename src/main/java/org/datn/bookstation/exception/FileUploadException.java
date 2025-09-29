package org.datn.bookstation.exception;

public class FileUploadException extends RuntimeException {
    private final String errorCode;

    public FileUploadException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
