package com.portfolio.backend.controller;

import com.portfolio.backend.dto.request.ContactRequest;
import com.portfolio.backend.dto.response.ApiResponse;
import com.portfolio.backend.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping({"/api/v1/contact", "/api/contact"})
    public ResponseEntity<ApiResponse<Void>> submitContact(@Valid @RequestBody ContactRequest request) {
        contactService.processContactSubmission(request);
        return ResponseEntity.ok(ApiResponse.success("Your message has been sent successfully."));
    }
}

