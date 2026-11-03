package com.portfolio.backend.service;

import com.portfolio.backend.dto.request.ContactRequest;

public interface ContactService {

    void submitContact(ContactRequest request);
} 