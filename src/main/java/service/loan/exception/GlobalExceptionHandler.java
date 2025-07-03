package service.loan.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({LoanSimulationException.class})
    public ResponseEntity<ErrorResponse> handleLoanSimulationException(LoanSimulationException ex) {
        return this.createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().map((error) -> {
            String var10000 = error.getField();
            return var10000 + ": " + error.getDefaultMessage();
        }).reduce((a, b) -> a + "; " + b).orElse("Validation error");
        return this.createErrorResponse(HttpStatus.BAD_REQUEST, message, ex);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream().map((violation) -> {
            String var10000 = String.valueOf(violation.getPropertyPath());
            return var10000 + ": " + violation.getMessage();
        }).reduce((a, b) -> a + "; " + b).orElse("Validation error");
        return this.createErrorResponse(HttpStatus.BAD_REQUEST, message, ex);
    }

    @ExceptionHandler({RequestNotPermitted.class})
    public ResponseEntity<ErrorResponse> handleRequestNotPermitted(RequestNotPermitted ex) {
        return this.createErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "Too many requests", ex);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return this.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus httpStatus, String message, Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.create(ex, httpStatus, message);
        return new ResponseEntity<>(errorResponse, httpStatus);
    }
}
