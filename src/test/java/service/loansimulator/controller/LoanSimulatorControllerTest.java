package com.example.loansimulatorservice.controller;

import com.example.loansimulatorservice.exception.LoanSimulationException;
import com.example.loansimulatorservice.model.LoanSimulationRequest;
import com.example.loansimulatorservice.model.LoanSimulationResponse;
import com.example.loansimulatorservice.service.LoanSimulatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanSimulatorController.class)
@DisplayName("Loan Simulator Controller Tests")
class LoanSimulatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanSimulatorService loanSimulatorService;

    @Test
    @DisplayName("Should return 200 and correct response when simulation is successful")
    void shouldReturnSuccessfulSimulation() throws Exception {
        // given
        var request = createValidRequest();
        var response = createSuccessfulResponse();

        given(loanSimulatorService.simulateLoan(any(LoanSimulationRequest.class)))
            .willReturn(response);

        // when/then
        mockMvc.perform(post("/api/v1/loan-simulator/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalAmount").value(10150.00))
            .andExpect(jsonPath("$.monthlyPayment").value(845.83))
            .andExpect(jsonPath("$.totalInterest").value(150.00))
            .andExpect(jsonPath("$.annualInterestRate").value(3.00));

        verify(loanSimulatorService).simulateLoan(any(LoanSimulationRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when request is invalid")
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        // given
        var invalidRequest = new LoanSimulationRequest(); // Empty request

        // when/then
        mockMvc.perform(post("/api/v1/loan-simulator/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("Should return 400 when service throws LoanSimulationException")
    void shouldReturnBadRequestWhenServiceThrowsException() throws Exception {
        // given
        var request = createValidRequest();
        given(loanSimulatorService.simulateLoan(any(LoanSimulationRequest.class)))
            .willThrow(new LoanSimulationException("Invalid loan parameters"));

        // when/then
        mockMvc.perform(post("/api/v1/loan-simulator/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Invalid loan parameters"));
    }

    @Test
    @DisplayName("Should return 500 when unexpected error occurs")
    void shouldReturnInternalErrorWhenUnexpectedErrorOccurs() throws Exception {
        // given
        var request = createValidRequest();
        given(loanSimulatorService.simulateLoan(any(LoanSimulationRequest.class)))
            .willThrow(new RuntimeException("Unexpected error"));

        // when/then
        mockMvc.perform(post("/api/v1/loan-simulator/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    private LoanSimulationRequest createValidRequest() {
        var request = new LoanSimulationRequest();
        request.setLoanAmount(BigDecimal.valueOf(10000));
        request.setBirthDate(LocalDate.now().minusYears(30));
        request.setPaymentTermInMonths(12);
        return request;
    }

    private LoanSimulationResponse createSuccessfulResponse() {
        return LoanSimulationResponse.builder()
            .totalAmount(BigDecimal.valueOf(10150.00))
            .monthlyPayment(BigDecimal.valueOf(845.83))
            .totalInterest(BigDecimal.valueOf(150.00))
            .annualInterestRate(BigDecimal.valueOf(3.00))
            .build();
    }
} 