package service.loan.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LoanSimulationRequest {
    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be positive")
    private BigDecimal loanAmount;

    @NotNull(message = "Birth date is required")
    private LocalDate birthDate;

    @NotNull(message = "Payment term is required")
    @Positive(message = "Payment term must be positive")
    private Integer paymentTermInMonths;
}
