package com.portfolio.backend.exception;

/**
 * Exception thrown when an email cannot be prepared, formatted, or delivered via SMTP.
 */
public class EmailSendingException extends RuntimeException {

    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}

