package com.example.loansimulatorservice.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("Should handle LoanSimulationException")
    void shouldHandleLoanSimulationException() {
        // given
        var exception = new LoanSimulationException("Invalid loan parameters");

        // when
        var response = exceptionHandler.handleLoanSimulationException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
            .satisfies(errorResponse -> {
                assertThat(errorResponse.getStatus()).isEqualTo(400);
                assertThat(errorResponse.getMessage()).isEqualTo("Invalid loan parameters");
                assertThat(errorResponse.getError()).isEqualTo("Bad Request");
            });
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void shouldHandleMethodArgumentNotValidException() {
        // given
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("object", "field", "Invalid value");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        
        var exception = new MethodArgumentNotValidException(null, bindingResult);

        // when
        var response = exceptionHandler.handleValidationException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
            .satisfies(errorResponse -> {
                assertThat(errorResponse.getStatus()).isEqualTo(400);
                assertThat(errorResponse.getMessage()).isEqualTo("field: Invalid value");
                assertThat(errorResponse.getError()).isEqualTo("Bad Request");
            });
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException")
    void shouldHandleConstraintViolationException() {
        // given
        var violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getMessage()).thenReturn("Invalid value");
        
        var exception = new ConstraintViolationException("Validation failed", Set.of(violation));

        // when
        var response = exceptionHandler.handleConstraintViolationException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
            .satisfies(errorResponse -> {
                assertThat(errorResponse.getStatus()).isEqualTo(400);
                assertThat(errorResponse.getMessage()).contains("Invalid value");
                assertThat(errorResponse.getError()).isEqualTo("Bad Request");
            });
    }

    @Test
    @DisplayName("Should handle RequestNotPermitted")
    void shouldHandleRequestNotPermitted() {
        // given
        var exception = mock(RequestNotPermitted.class);

        // when
        var response = exceptionHandler.handleRequestNotPermitted(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody())
            .satisfies(errorResponse -> {
                assertThat(errorResponse.getStatus()).isEqualTo(429);
                assertThat(errorResponse.getMessage()).isEqualTo("Too many requests");
                assertThat(errorResponse.getError()).isEqualTo("Too Many Requests");
            });
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void shouldHandleGenericException() {
        // given
        var exception = new RuntimeException("Unexpected error");

        // when
        var response = exceptionHandler.handleGenericException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
            .satisfies(errorResponse -> {
                assertThat(errorResponse.getStatus()).isEqualTo(500);
                assertThat(errorResponse.getMessage()).isEqualTo("An unexpected error occurred");
                assertThat(errorResponse.getError()).isEqualTo("Internal Server Error");
            });
    }
} 