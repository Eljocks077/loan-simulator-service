package service.loan.service;

import service.loan.model.LoanSimulationRequest;
import service.loan.model.LoanSimulationResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface LoanSimulatorService {
    LoanSimulationResponse simulateLoan(LoanSimulationRequest request);

    void validateRequest(LoanSimulationRequest request);

    BigDecimal calculateAnnualInterestRate(LocalDate birthDate);

    BigDecimal calculateMonthlyPayment(BigDecimal loanAmount, BigDecimal monthlyInterestRate, int termInMonths);
}
