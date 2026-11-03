package com.portfolio.backend.email;

public interface EmailService {

    void sendContactNotification(String visitorName, String visitorEmail, String subject, String message);

    void sendConfirmationEmail(String visitorName, String visitorEmail, String originalSubject);

    void sendEmail(String to, String subject, String content, boolean isHtml, String replyTo);
}
