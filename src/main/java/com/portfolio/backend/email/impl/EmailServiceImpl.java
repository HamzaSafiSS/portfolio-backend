package com.portfolio.backend.email.impl;

import com.portfolio.backend.email.EmailService;
import com.portfolio.backend.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${portfolio.mail.from:}")
    private String mailFrom;

    @Value("${portfolio.mail.receiver:}")
    private String contactReceiverEmail;

    @Override
    public void sendContactNotification(String visitorName, String visitorEmail, String subject, String message) {
        validateConfiguredReceiver();

        String safeName = HtmlUtils.htmlEscape(visitorName != null ? visitorName.trim() : "Anonymous");
        String safeEmail = HtmlUtils.htmlEscape(visitorEmail != null ? visitorEmail.trim() : "No email provided");
        String safeSubject = HtmlUtils.htmlEscape(subject != null ? subject.trim() : "No Subject");
        String safeMessage = HtmlUtils.htmlEscape(message != null ? message.trim() : "");

        String emailSubject = "[Portfolio Inquiry] " + (subject != null && !subject.isBlank() ? subject.trim() : "New Contact Form Message");
        String htmlBody = buildNotificationHtml(safeName, safeEmail, safeSubject, safeMessage);

        sendEmail(contactReceiverEmail, emailSubject, htmlBody, true, visitorEmail);
    }

    @Override
    public void sendConfirmationEmail(String visitorName, String visitorEmail, String originalSubject) {
        if (visitorEmail == null || visitorEmail.isBlank()) {
            throw new EmailSendingException("Cannot send confirmation email: Visitor email address is missing.");
        }

        String safeName = HtmlUtils.htmlEscape(visitorName != null ? visitorName.trim() : "there");
        String safeSubject = HtmlUtils.htmlEscape(originalSubject != null && !originalSubject.isBlank() ? originalSubject.trim() : "your inquiry");

        String emailSubject = "Thank you for reaching out: " + (originalSubject != null && !originalSubject.isBlank() ? originalSubject.trim() : "Message Received");
        String htmlBody = buildConfirmationHtml(safeName, safeSubject);

        sendEmail(visitorEmail, emailSubject, htmlBody, true, null);
    }

    @Override
    public void sendEmail(String to, String subject, String content, boolean isHtml, String replyTo) {
        validateConfiguredSender();

        if (to == null || to.isBlank()) {
            throw new EmailSendingException("Cannot send email: Recipient address is empty.");
        }

        String cleanTo = to.replaceAll("[\\r\\n]", "").trim();
        String cleanSubject = (subject != null ? subject : "Portfolio Notification").replaceAll("[\\r\\n]", " ").trim();
        String cleanReplyTo = (replyTo != null && !replyTo.isBlank()) ? replyTo.replaceAll("[\\r\\n]", "").trim() : null;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());

            try {
                helper.setFrom(new InternetAddress(mailFrom.trim(), "Portfolio Contact"));
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(mailFrom.trim());
            }

            helper.setTo(cleanTo);
            helper.setSubject(cleanSubject);
            helper.setText(content, isHtml);

            if (cleanReplyTo != null && !cleanReplyTo.isBlank()) {
                helper.setReplyTo(cleanReplyTo);
            }

            mailSender.send(mimeMessage);
            log.info("Email successfully dispatched to recipient [{}] with subject [{}]", cleanTo, cleanSubject);

        } catch (MessagingException | MailException e) {
            log.error("SMTP error while delivering email to [{}]. Reason: {}", cleanTo, e.getMessage());
            throw new EmailSendingException("Failed to send email to " + cleanTo + " via SMTP: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during email delivery to [{}]. Reason: {}", cleanTo, e.getMessage());
            throw new EmailSendingException("Unexpected error sending email to " + cleanTo + ": " + e.getMessage(), e);
        }
    }

    private void validateConfiguredSender() {
        if (mailFrom == null || mailFrom.isBlank()) {
            log.error("MAIL_FROM is not configured. Email sending cannot proceed.");
            throw new EmailSendingException("Sender email address (MAIL_FROM) is not configured. Please set the MAIL_FROM environment variable.");
        }
    }

    private void validateConfiguredReceiver() {
        if (contactReceiverEmail == null || contactReceiverEmail.isBlank()) {
            log.error("CONTACT_RECEIVER_EMAIL is not configured. Contact notifications cannot be delivered.");
            throw new EmailSendingException("Contact receiver email (CONTACT_RECEIVER_EMAIL) is not configured. Please set the CONTACT_RECEIVER_EMAIL environment variable.");
        }
    }

    private String buildNotificationHtml(String name, String email, String subject, String message) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <style>
                body { margin: 0; padding: 24px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f1f5f9; color: #1e293b; }
                .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
                .header { background: #0f172a; padding: 24px 32px; color: #ffffff; }
                .header h2 { margin: 0; font-size: 20px; font-weight: 600; letter-spacing: -0.02em; }
                .body { padding: 32px; }
                .meta-table { width: 100%%; border-collapse: collapse; margin-bottom: 24px; }
                .meta-table td { padding: 8px 0; font-size: 14px; vertical-align: top; }
                .meta-table .label { width: 110px; font-weight: 600; color: #64748b; }
                .meta-table .val { color: #0f172a; }
                .meta-table a { color: #2563eb; text-decoration: none; }
                .message-title { font-size: 14px; font-weight: 600; color: #64748b; margin-bottom: 8px; }
                .message-box { background: #f8fafc; border: 1px solid #e2e8f0; border-left: 4px solid #3b82f6; border-radius: 6px; padding: 16px 20px; font-size: 14px; line-height: 1.6; color: #334155; white-space: pre-wrap; }
                .footer { padding: 16px 32px; background: #f8fafc; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8; text-align: center; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h2>New Contact Form Inquiry</h2>
                </div>
                <div class="body">
                  <table class="meta-table">
                    <tr><td class="label">Sender:</td><td class="val"><strong>%s</strong></td></tr>
                    <tr><td class="label">Email:</td><td class="val"><a href="mailto:%s">%s</a></td></tr>
                    <tr><td class="label">Subject:</td><td class="val">%s</td></tr>
                  </table>
                  <div class="message-title">Message Content:</div>
                  <div class="message-box">%s</div>
                </div>
                <div class="footer">
                  This notification was triggered by your portfolio contact form. Hit <em>Reply</em> to respond directly to %s.
                </div>
              </div>
            </body>
            </html>
            """.formatted(name, email, email, subject, message, email);
    }

    private String buildConfirmationHtml(String name, String originalSubject) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <style>
                body { margin: 0; padding: 24px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f1f5f9; color: #1e293b; }
                .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
                .header { background: #0f172a; padding: 28px 32px; color: #ffffff; text-align: center; }
                .header h2 { margin: 0; font-size: 20px; font-weight: 600; }
                .body { padding: 32px; font-size: 15px; line-height: 1.6; color: #334155; }
                .highlight-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 16px 20px; margin: 20px 0; font-size: 14px; color: #475569; }
                .footer { padding: 16px 32px; background: #f8fafc; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8; text-align: center; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h2>Thank You for Getting in Touch!</h2>
                </div>
                <div class="body">
                  <p>Hi <strong>%s</strong>,</p>
                  <p>I have received your message regarding <em>"%s"</em> through my portfolio contact form.</p>
                  <div class="highlight-card">
                    Your inquiry is queued and I typically respond within 24 to 48 hours.
                  </div>
                  <p>If you have any urgent details to add, feel free to reply directly to this email.</p>
                  <p style="margin-top: 24px;">Best regards,<br/><strong>Personal Portfolio</strong></p>
                </div>
                <div class="footer">
                  This is an automated confirmation verifying receipt of your message.
                </div>
              </div>
            </body>
            </html>
            """.formatted(name, originalSubject);
    }
}
