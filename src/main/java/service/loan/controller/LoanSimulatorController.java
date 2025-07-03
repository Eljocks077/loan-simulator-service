package service.loan.controller;

import service.loan.model.LoanSimulationRequest;
import service.loan.model.LoanSimulationResponse;
import service.loan.service.LoanSimulatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loan-simulator")
@RequiredArgsConstructor
public class LoanSimulatorController {

    private final LoanSimulatorService loanSimulatorService;

    @PostMapping("/simulate")
    public ResponseEntity<LoanSimulationResponse> simulateLoan(@Valid @RequestBody LoanSimulationRequest request) {
        return ResponseEntity.ok(loanSimulatorService.simulateLoan(request));
    }
} 