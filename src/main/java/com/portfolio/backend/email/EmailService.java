package com.portfolio.backend.email;

/**
 * Service interface for email delivery operations.
 */
public interface EmailService {

    /**
     * Sends a contact notification email to the configured portfolio owner.
     *
     * @param visitorName  The name of the visitor who submitted the form.
     * @param visitorEmail The email of the visitor (used as Reply-To).
     * @param subject      The inquiry subject.
     * @param message      The inquiry content.
     */
    void sendContactNotification(String visitorName, String visitorEmail, String subject, String message);

    /**
     * Sends an automated confirmation email to the visitor acknowledging receipt of their message.
     *
     * @param visitorName     The name of the visitor.
     * @param visitorEmail    The recipient email of the visitor.
     * @param originalSubject The original subject of the visitor's inquiry.
     */
    void sendConfirmationEmail(String visitorName, String visitorEmail, String originalSubject);

    /**
     * Sends a generic email message.
     *
     * @param to       The recipient's email address.
     * @param subject  The email subject.
     * @param content  The email body content.
     * @param isHtml   Whether the content is HTML.
     * @param replyTo  The optional Reply-To email address (can be null).
     */
    void sendEmail(String to, String subject, String content, boolean isHtml, String replyTo);
}

