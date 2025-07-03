package service.loansimulator.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import service.loan.exception.GlobalExceptionHandler;
import service.loan.exception.LoanSimulationException;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;
    @Mock
    private ConstraintViolation<Object> mockConstraintViolation1;
    @Mock
    private ConstraintViolation<Object> mockConstraintViolation2;
    @Mock
    private Path mockPath1;
    @Mock
    private Path mockPath2;

    @Test
    @DisplayName("Should handle LoanSimulationException")
    void shouldHandleLoanSimulationException() {
        var exception = new LoanSimulationException("Invalid loan parameters");

        var response = exceptionHandler.handleLoanSimulationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .satisfies(errorResponse -> {
                    assertThat(errorResponse.getStatusCode().value()).isEqualTo(400);
                    assertThat(errorResponse.getBody().getDetail()).isEqualTo("Invalid loan parameters");
                });
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void shouldHandleMethodArgumentNotValidException() {
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("object", "field", "Invalid value");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

        var exception = new MethodArgumentNotValidException(null, bindingResult);

        var response = exceptionHandler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .satisfies(errorResponse -> {
                    assertThat(errorResponse.getStatusCode().value()).isEqualTo(400);
                    assertThat(errorResponse.getBody().getDetail()).isEqualTo("field: Invalid value");
                });
    }

    @Test
    @DisplayName("Should handle RequestNotPermitted")
    void shouldHandleRequestNotPermitted() {
        var exception = mock(RequestNotPermitted.class);

        var response = exceptionHandler.handleRequestNotPermitted(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody())
                .satisfies(errorResponse -> {
                    assertThat(errorResponse.getStatusCode().value()).isEqualTo(429);
                    assertThat(errorResponse.getBody().getDetail()).isEqualTo("Too many requests");
                });
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void shouldHandleGenericException() {
        var exception = new RuntimeException("Unexpected error");

        var response = exceptionHandler.handleGenericException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .satisfies(errorResponse -> {
                    assertThat(errorResponse.getStatusCode().value()).isEqualTo(500);
                    assertThat(errorResponse.getBody().getDetail()).isEqualTo("An unexpected error occurred");
                });
    }
} 