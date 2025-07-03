package service.loan.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LoanSimulationResponse {
    private BigDecimal totalAmount;
    private BigDecimal monthlyPayment;
    private BigDecimal totalInterest;
    private BigDecimal annualInterestRate;
} 