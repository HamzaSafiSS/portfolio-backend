package com.portfolio.backend.controller;

import com.portfolio.backend.dto.request.ContactRequest;
import com.portfolio.backend.dto.response.ApiResponse;
import com.portfolio.backend.exception.RateLimitExceededException;
import com.portfolio.backend.service.ContactService;
import com.portfolio.backend.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final RateLimiterService rateLimiterService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Void>> root() {
        return ResponseEntity.ok(ApiResponse.success("Portfolio Contact API is running."));
    }

    @PostMapping({"/api/v1/contact", "/api/contact"})
    public ResponseEntity<ApiResponse<Void>> submitContact(
            @Valid @RequestBody ContactRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = resolveClientIp(httpRequest);
        if (!rateLimiterService.tryAcquire(clientIp)) {
            throw new RateLimitExceededException("Too many requests. Please try again later.");
        }

        contactService.submitContact(request);
        return ResponseEntity.ok(ApiResponse.success("Your message has been sent successfully."));
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }
}
