package com.portfolio.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.backend.dto.request.ContactRequest;
import com.portfolio.backend.exception.EmailSendingException;
import com.portfolio.backend.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContactService contactService;

    @Test
    void submitContact_Success_Returns200() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .subject("Project Inquiry")
                .message("I would like to discuss a project.")
                .build();

        doNothing().when(contactService).processContactSubmission(any(ContactRequest.class));

        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Your message has been sent successfully."));

        verify(contactService).processContactSubmission(any(ContactRequest.class));
    }

    @Test
    void submitContact_EndpointAlias_Returns200() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .subject("Freelance Work")
                .message("Let us collaborate on a new website.")
                .build();

        doNothing().when(contactService).processContactSubmission(any(ContactRequest.class));

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Your message has been sent successfully."));

        verify(contactService).processContactSubmission(any(ContactRequest.class));
    }

    @Test
    void submitContact_BlankName_Returns400() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .name("")
                .email("john@example.com")
                .subject("Project Inquiry")
                .message("I would like to discuss a project.")
                .build();

        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(contactService, never()).processContactSubmission(any());
    }

    @Test
    void submitContact_InvalidEmail_Returns400() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .name("John Doe")
                .email("not-a-valid-email")
                .subject("Project Inquiry")
                .message("I would like to discuss a project.")
                .build();

        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(contactService, never()).processContactSubmission(any());
    }

    @Test
    void submitContact_ShortSubject_Returns400() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .subject("Hi")
                .message("I would like to discuss a project.")
                .build();

        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(contactService, never()).processContactSubmission(any());
    }

    @Test
    void submitContact_ShortMessage_Returns400() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .subject("Project Inquiry")
                .message("Too short")
                .build();

        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(contactService, never()).processContactSubmission(any());
    }

    @Test
    void submitContact_EmailSendingFailure_Returns500() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .subject("Project Inquiry")
                .message("I would like to discuss a project.")
                .build();

        doThrow(new EmailSendingException("SMTP connection error"))
                .when(contactService).processContactSubmission(any(ContactRequest.class));

        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to send your message. Please try again later."));
    }

    @Test
    void submitContact_UnexpectedError_Returns500() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .subject("Project Inquiry")
                .message("I would like to discuss a project.")
                .build();

        doThrow(new RuntimeException("Database or system crash"))
                .when(contactService).processContactSubmission(any(ContactRequest.class));

        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."));
    }
}

