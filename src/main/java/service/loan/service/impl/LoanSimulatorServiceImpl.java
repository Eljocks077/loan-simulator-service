package service.loan.service.impl;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import service.loan.exception.LoanSimulationException;
import service.loan.model.LoanSimulationRequest;
import service.loan.model.LoanSimulationResponse;
import service.loan.service.LoanSimulatorService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class LoanSimulatorServiceImpl implements LoanSimulatorService {
    private static final Logger log = LoggerFactory.getLogger(LoanSimulatorServiceImpl.class);
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    @CircuitBreaker(name = "loanSimulation")
    @RateLimiter(name = "loanSimulation")
    @Bulkhead(name = "loanSimulation")
    @Retry(name = "loanSimulation")
    @Override
    public LoanSimulationResponse simulateLoan(LoanSimulationRequest request) {
        try {
            this.validateRequest(request);
            CompletableFuture<BigDecimal> annualInterestRateFuture = CompletableFuture.supplyAsync(() -> this.calculateAnnualInterestRate(request.getBirthDate()));
            CompletableFuture<BigDecimal> monthlyInterestRateFuture = annualInterestRateFuture.thenApply((rate) -> rate.divide(BigDecimal.valueOf(12L), 10, ROUNDING_MODE));
            CompletableFuture<BigDecimal> monthlyPaymentFuture = monthlyInterestRateFuture.thenApply((monthlyRate) -> this.calculateMonthlyPayment(request.getLoanAmount(), monthlyRate, request.getPaymentTermInMonths()));
            CompletableFuture<BigDecimal> totalAmountFuture = monthlyPaymentFuture.thenApply((monthlyPaymentx) -> monthlyPaymentx.multiply(BigDecimal.valueOf((long) request.getPaymentTermInMonths())).setScale(2, ROUNDING_MODE));
            CompletableFuture<BigDecimal> totalInterestFuture = totalAmountFuture.thenApply((totalAmountx) -> totalAmountx.subtract(request.getLoanAmount()).setScale(2, ROUNDING_MODE));
            BigDecimal annualInterestRate = annualInterestRateFuture.get();
            BigDecimal monthlyPayment = monthlyPaymentFuture.get();
            BigDecimal totalAmount = totalAmountFuture.get();
            BigDecimal totalInterest = totalInterestFuture.get();
            return LoanSimulationResponse.builder().monthlyPayment(monthlyPayment.setScale(2, ROUNDING_MODE)).totalAmount(totalAmount).totalInterest(totalInterest).annualInterestRate(annualInterestRate.multiply(BigDecimal.valueOf(100L)).setScale(2, ROUNDING_MODE)).build();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error during loan simulation calculation", e);
            Thread.currentThread().interrupt();
            throw new LoanSimulationException("Error calculating loan simulation", e);
        }
    }

    @Override
    public void validateRequest(LoanSimulationRequest request) {
        if (request.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new LoanSimulationException("Loan amount must be greater than zero");
        } else if (request.getPaymentTermInMonths() <= 0) {
            throw new LoanSimulationException("Payment term must be greater than zero");
        } else if (request.getBirthDate().isAfter(LocalDate.now())) {
            throw new LoanSimulationException("Birth date cannot be in the future");
        }
    }

    @Override
    public BigDecimal calculateAnnualInterestRate(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age <= 25) {
            return BigDecimal.valueOf(0.05);
        } else if (age <= 40) {
            return BigDecimal.valueOf(0.03);
        } else {
            return age <= 60 ? BigDecimal.valueOf(0.02) : BigDecimal.valueOf(0.04);
        }
    }

    @Override
    public BigDecimal calculateMonthlyPayment(BigDecimal loanAmount, BigDecimal monthlyInterestRate, int termInMonths) {
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyInterestRate);
        BigDecimal rateFactorPow = onePlusRate.pow(termInMonths);
        BigDecimal numerator = monthlyInterestRate.multiply(rateFactorPow);
        BigDecimal denominator = rateFactorPow.subtract(BigDecimal.ONE);
        return loanAmount.multiply(numerator.divide(denominator, 10, ROUNDING_MODE));
    }

}
