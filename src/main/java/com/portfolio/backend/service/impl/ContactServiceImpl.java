package com.portfolio.backend.service.impl;

import com.portfolio.backend.dto.request.ContactRequest;
import com.portfolio.backend.email.EmailService;
import com.portfolio.backend.exception.EmailSendingException;
import com.portfolio.backend.exception.SpamDetectedException;
import com.portfolio.backend.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final EmailService emailService;

    @Override
    public void submitContact(ContactRequest request) {
        Objects.requireNonNull(request, "Contact request must not be null");

        if (request.getHoneypot() != null && !request.getHoneypot().isBlank()) {
            log.warn("Spam submission blocked by honeypot field from email [{}]", request.getEmail());
            throw new SpamDetectedException("Automated submission detected");
        }

        String name = sanitizeHeader(request.getName());
        String email = sanitizeHeader(request.getEmail());
        String subject = sanitizeHeader(request.getSubject());
        String message = request.getMessage() != null ? request.getMessage().trim() : "";

        log.info("Processing contact submission from [{}] with subject [{}]", email, subject);

        emailService.sendContactNotification(name, email, subject, message);
        log.info("Contact notification successfully sent to owner for inquiry from [{}]", email);

        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendConfirmationEmail(name, email, subject);
                log.info("Confirmation email successfully sent to visitor [{}]", email);
            } catch (EmailSendingException ex) {
                log.warn("Owner notified, but failed to send confirmation receipt to visitor [{}]. Reason: {}",
                        email, ex.getMessage());
            }
        });
    }

    private String sanitizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[\\r\\n]", " ").trim();
    }
}
