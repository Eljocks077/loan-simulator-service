package service.loansimulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import service.loan.exception.LoanSimulationException;
import service.loan.model.LoanSimulationRequest;
import service.loan.model.LoanSimulationResponse;
import service.loan.service.impl.LoanSimulatorServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanSimulatorServiceImplTest {

    @InjectMocks
    private LoanSimulatorServiceImpl loanSimulatorService;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    @Test
    @DisplayName("Should throw LoanSimulationException if validateRequest fails in simulateLoan")
    void simulateLoan_ValidateRequestFails() {
        // Given
        LoanSimulationRequest request = LoanSimulationRequest.builder().loanAmount(BigDecimal.ZERO) // Valor inválido
                .paymentTermInMonths(60).birthDate(LocalDate.of(1990, 1, 1)).build();

        LoanSimulationException exception = assertThrows(LoanSimulationException.class, () -> loanSimulatorService.simulateLoan(request));
        assertEquals("Loan amount must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate request successfully when all fields are valid")
    void validateRequest_Success() {

        LoanSimulationRequest request = LoanSimulationRequest.builder().loanAmount(BigDecimal.valueOf(1000)).paymentTermInMonths(12).birthDate(LocalDate.of(1990, 5, 15)).build();

        loanSimulatorService.validateRequest(request);

        assertDoesNotThrow(() -> loanSimulatorService.validateRequest(request));
    }

    @Test
    @DisplayName("Should throw LoanSimulationException if loan amount is zero")
    void validateRequest_LoanAmountZero_ThrowsException() {
        // Given
        LoanSimulationRequest request = LoanSimulationRequest.builder().loanAmount(BigDecimal.ZERO).paymentTermInMonths(12).birthDate(LocalDate.of(1990, 5, 15)).build();

        LoanSimulationException exception = assertThrows(LoanSimulationException.class, () -> loanSimulatorService.validateRequest(request));
        assertEquals("Loan amount must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw LoanSimulationException if loan amount is negative")
    void validateRequest_LoanAmountNegative_ThrowsException() {
        LoanSimulationRequest request = LoanSimulationRequest.builder().loanAmount(BigDecimal.valueOf(-100)).paymentTermInMonths(12).birthDate(LocalDate.of(1990, 5, 15)).build();

        LoanSimulationException exception = assertThrows(LoanSimulationException.class, () -> loanSimulatorService.validateRequest(request));
        assertEquals("Loan amount must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw LoanSimulationException if payment term is zero")
    void validateRequest_PaymentTermZero_ThrowsException() {
        LoanSimulationRequest request = LoanSimulationRequest.builder().loanAmount(BigDecimal.valueOf(1000)).paymentTermInMonths(0).birthDate(LocalDate.of(1990, 5, 15)).build();

        LoanSimulationException exception = assertThrows(LoanSimulationException.class, () -> loanSimulatorService.validateRequest(request));
        assertEquals("Payment term must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw LoanSimulationException if payment term is negative")
    void validateRequest_PaymentTermNegative_ThrowsException() {
        LoanSimulationRequest request = LoanSimulationRequest.builder().loanAmount(BigDecimal.valueOf(1000)).paymentTermInMonths(-10).birthDate(LocalDate.of(1990, 5, 15)).build();

        LoanSimulationException exception = assertThrows(LoanSimulationException.class, () -> loanSimulatorService.validateRequest(request));
        assertEquals("Payment term must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw LoanSimulationException if birth date is in the future")
    void validateRequest_BirthDateFuture_ThrowsException() {
        LoanSimulationRequest request = LoanSimulationRequest.builder().loanAmount(BigDecimal.valueOf(1000)).paymentTermInMonths(12).birthDate(LocalDate.now().plusDays(1))
                .build();

        LoanSimulationException exception = assertThrows(LoanSimulationException.class, () -> loanSimulatorService.validateRequest(request));
        assertEquals("Birth date cannot be in the future", exception.getMessage());
    }


    @Test
    @DisplayName("Should return 0.05 for age <= 25")
    void calculateAnnualInterestRate_AgeLessThanOrEqualTo25() {
        LocalDate birthDate = LocalDate.now().minusYears(25).plusDays(1);

        BigDecimal annualRate = loanSimulatorService.calculateAnnualInterestRate(birthDate);

        assertEquals(BigDecimal.valueOf(0.05), annualRate);
    }

    @Test
    @DisplayName("Should return 0.03 for age > 25 and <= 40")
    void calculateAnnualInterestRate_AgeGreaterThan25AndLessThanOrEqualTo40() {
        LocalDate birthDate = LocalDate.now().minusYears(30);

        BigDecimal annualRate = loanSimulatorService.calculateAnnualInterestRate(birthDate);

        assertEquals(BigDecimal.valueOf(0.03), annualRate);
    }

    @Test
    @DisplayName("Should return 0.02 for age > 40 and <= 60")
    void calculateAnnualInterestRate_AgeGreaterThan40AndLessThanOrEqualTo60() {
        LocalDate birthDate = LocalDate.now().minusYears(50);

        BigDecimal annualRate = loanSimulatorService.calculateAnnualInterestRate(birthDate);

        assertEquals(BigDecimal.valueOf(0.02), annualRate);
    }

    @Test
    @DisplayName("Should return 0.04 for age > 60")
    void calculateAnnualInterestRate_AgeGreaterThan60() {
        LocalDate birthDate = LocalDate.now().minusYears(65);
        BigDecimal annualRate = loanSimulatorService.calculateAnnualInterestRate(birthDate);

        assertEquals(BigDecimal.valueOf(0.04), annualRate);
    }


    @Test
    @DisplayName("Should calculate monthly payment correctly")
    void calculateMonthlyPayment_Success() {
        BigDecimal loanAmount = BigDecimal.valueOf(10000);
        BigDecimal monthlyInterestRate = BigDecimal.valueOf(0.005);
        int termInMonths = 24;

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyInterestRate);
        BigDecimal rateFactorPow = onePlusRate.pow(termInMonths);
        BigDecimal numerator = monthlyInterestRate.multiply(rateFactorPow);
        BigDecimal denominator = rateFactorPow.subtract(BigDecimal.ONE);
        BigDecimal expectedMonthlyPayment = loanAmount.multiply(numerator.divide(denominator, 10, ROUNDING_MODE));

        BigDecimal actualMonthlyPayment = loanSimulatorService.calculateMonthlyPayment(loanAmount, monthlyInterestRate, termInMonths);

        assertEquals(expectedMonthlyPayment.setScale(2, ROUNDING_MODE), actualMonthlyPayment.setScale(2, ROUNDING_MODE));
    }


}