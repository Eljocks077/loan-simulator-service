package service.loan.exception;

public class LoanSimulationException extends RuntimeException {
    public LoanSimulationException(String message) {
        super(message);
    }

    public LoanSimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}
