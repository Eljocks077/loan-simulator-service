package com.example.loansimulatorservice.service;

import com.example.loansimulatorservice.exception.LoanSimulationException;
import com.example.loansimulatorservice.model.LoanSimulationRequest;
import com.example.loansimulatorservice.model.LoanSimulationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Loan Simulator Service Tests")
class LoanSimulatorServiceTest {

    private final LoanSimulatorService service = new LoanSimulatorService();

    @Nested
    @DisplayName("Successful Loan Simulation Tests")
    class SuccessfulLoanSimulationTests {
        
        @Test
        @DisplayName("Should calculate loan simulation with correct values")
        void shouldCalculateLoanSimulationWithCorrectValues() {
            // given
            var request = createValidRequest(
                BigDecimal.valueOf(10000),
                LocalDate.now().minusYears(30),
                12
            );

            // when
            var response = service.simulateLoan(request);

            // then
            assertThat(response)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getAnnualInterestRate()).isEqualTo(BigDecimal.valueOf(3.00));
                    assertThat(r.getMonthlyPayment()).isPositive();
                    assertThat(r.getTotalAmount()).isGreaterThan(request.getLoanAmount());
                    assertThat(r.getTotalInterest()).isPositive();
                });
        }

        @ParameterizedTest(name = "Age {0} should have {1}% interest rate")
        @CsvSource({
            "20, 5.00",
            "30, 3.00",
            "50, 2.00",
            "65, 4.00"
        })
        @DisplayName("Should apply correct interest rate based on age")
        void shouldApplyCorrectInterestRateBasedOnAge(int age, BigDecimal expectedRate) {
            // given
            var request = createValidRequest(
                BigDecimal.valueOf(10000),
                LocalDate.now().minusYears(age),
                12
            );

            // when
            var response = service.simulateLoan(request);

            // then
            assertThat(response.getAnnualInterestRate())
                .isEqualByComparingTo(expectedRate);
        }

        @ParameterizedTest(name = "Payment term of {0} months")
        @ValueSource(ints = {12, 24, 36, 48, 60})
        @DisplayName("Should calculate correct values for different payment terms")
        void shouldCalculateCorrectValuesForDifferentPaymentTerms(int paymentTerm) {
            // given
            var request = createValidRequest(
                BigDecimal.valueOf(10000),
                LocalDate.now().minusYears(30),
                paymentTerm
            );

            // when
            var response = service.simulateLoan(request);

            // then
            assertThat(response)
                .satisfies(r -> {
                    assertThat(r.getMonthlyPayment().multiply(BigDecimal.valueOf(paymentTerm)))
                        .isEqualByComparingTo(r.getTotalAmount());
                    assertThat(r.getTotalAmount().subtract(request.getLoanAmount()))
                        .isEqualByComparingTo(r.getTotalInterest());
                });
        }
    }

    @Nested
    @DisplayName("Validation Error Tests")
    class ValidationErrorTests {

        @Test
        @DisplayName("Should throw exception when loan amount is zero")
        void shouldThrowExceptionWhenLoanAmountIsZero() {
            // given
            var request = createValidRequest(
                BigDecimal.ZERO,
                LocalDate.now().minusYears(30),
                12
            );

            // when/then
            assertThatThrownBy(() -> service.simulateLoan(request))
                .isInstanceOf(LoanSimulationException.class)
                .hasMessage("Loan amount must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when loan amount is negative")
        void shouldThrowExceptionWhenLoanAmountIsNegative() {
            // given
            var request = createValidRequest(
                BigDecimal.valueOf(-1000),
                LocalDate.now().minusYears(30),
                12
            );

            // when/then
            assertThatThrownBy(() -> service.simulateLoan(request))
                .isInstanceOf(LoanSimulationException.class)
                .hasMessage("Loan amount must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when payment term is zero")
        void shouldThrowExceptionWhenPaymentTermIsZero() {
            // given
            var request = createValidRequest(
                BigDecimal.valueOf(10000),
                LocalDate.now().minusYears(30),
                0
            );

            // when/then
            assertThatThrownBy(() -> service.simulateLoan(request))
                .isInstanceOf(LoanSimulationException.class)
                .hasMessage("Payment term must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when birth date is in the future")
        void shouldThrowExceptionWhenBirthDateIsInFuture() {
            // given
            var request = createValidRequest(
                BigDecimal.valueOf(10000),
                LocalDate.now().plusDays(1),
                12
            );

            // when/then
            assertThatThrownBy(() -> service.simulateLoan(request))
                .isInstanceOf(LoanSimulationException.class)
                .hasMessage("Birth date cannot be in the future");
        }
    }

    private LoanSimulationRequest createValidRequest(BigDecimal amount, LocalDate birthDate, int term) {
        var request = new LoanSimulationRequest();
        request.setLoanAmount(amount);
        request.setBirthDate(birthDate);
        request.setPaymentTermInMonths(term);
        return request;
    }
} 