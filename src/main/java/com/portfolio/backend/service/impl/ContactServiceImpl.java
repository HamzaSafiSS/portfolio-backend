package com.portfolio.backend.service.impl;

import com.portfolio.backend.dto.request.ContactRequest;
import com.portfolio.backend.email.EmailService;
import com.portfolio.backend.exception.EmailSendingException;
import com.portfolio.backend.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final EmailService emailService;

    @Override
    public void processContactSubmission(ContactRequest request) {
        Objects.requireNonNull(request, "Contact request must not be null");

        String name = request.getName() != null ? request.getName().trim() : "";
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String subject = request.getSubject() != null ? request.getSubject().trim() : "";
        String message = request.getMessage() != null ? request.getMessage().trim() : "";

        log.info("Processing contact submission from [{}] with subject [{}]", email, subject);

        emailService.sendContactNotification(name, email, subject, message);
        log.info("Contact notification successfully sent to owner for inquiry from [{}]", email);

        try {
            emailService.sendConfirmationEmail(name, email, subject);
            log.info("Confirmation email successfully sent to visitor [{}]", email);
        } catch (EmailSendingException ex) {
            log.warn("Owner notified, but failed to send confirmation receipt to visitor [{}]. Reason: {}",
                    email, ex.getMessage());
        }
    }
}
