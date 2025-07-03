# Loan Simulator Service

Este é um serviço Spring Boot para simulação de empréstimos.

## Estrutura do Projeto

O projeto segue a arquitetura MVC (Model-View-Controller) com a seguinte estrutura de diretórios:

```
src/
  main/
    java/
      service/loansimulator/
        controller/    # Controladores REST
        model/        # Classes de modelo e DTOs
        service/      # Lógica de negócios
        exception/    # Tratamento de exceções
    resources/        # Arquivos de configuração
  test/
    java/
      service/loansimulator/
        controller/   # Testes dos controladores
        service/      # Testes dos serviços
```

## Tecnologias

- Java 21
- Spring Boot 3.2.3
- SpringDoc OpenAPI (Swagger)
- Resilience4j
- Lombok
- Maven

## Como Executar

1. Certifique-se de ter o Java 21 instalado
2. Execute o comando: `mvn spring-boot:run`
3. Acesse a documentação da API: `http://localhost:8080/swagger-ui.html`

## Testes

Para executar os testes:
```bash
mvn test
```

## Requirements

- Java 21
- Maven

## Setup

1. Clone the repository
2. Build the project:
```bash
mvn clean install
```
3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on port 8080.

## API Documentation

The API documentation is available through Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

### Simulate Loan

**Endpoint:** POST `/api/v1/loan-simulator/simulate`

**Request Body:**
```json
{
  "loanAmount": 10000.00,
  "birthDate": "1990-01-01",
  "paymentTermInMonths": 12
}
```

**Response Body:**
```json
{
  "totalAmount": 10150.00,
  "monthlyPayment": 845.83,
  "totalInterest": 150.00,
  "annualInterestRate": 3.00
}
```

### Interest Rates

The interest rates are determined by the client's age:
- Up to 25 years: 5% per year
- 26 to 40 years: 3% per year
- 41 to 60 years: 2% per year
- Above 60 years: 4% per year

## Project Structure

The project follows the MVC (Model-View-Controller) pattern:

- **Model**: Contains the request/response DTOs
- **Controller**: Handles HTTP requests and responses
- **Service**: Contains the business logic for loan simulation
- **Exception**: Contains custom exceptions and global exception handling

## Testing

The project includes comprehensive test coverage using modern testing frameworks:

### Test Stack
- JUnit 5 for test execution and lifecycle management
- AssertJ for fluent assertions
- Mockito for mocking dependencies
- Spring Test for integration testing

### Test Categories
1. **Service Tests** (`LoanSimulatorServiceTest`):
   - Calculation accuracy
   - Interest rate determination
   - Input validation
   - Edge cases

2. **Controller Tests** (`LoanSimulatorControllerTest`):
   - Request validation
   - Response mapping
   - HTTP status codes
   - Error scenarios

3. **Exception Handler Tests** (`GlobalExceptionHandlerTest`):
   - Custom exceptions
   - Validation errors
   - Rate limiting
   - Generic errors

### Running Tests

Run all tests with:
```bash
mvn test
```

Generate test coverage report:
```bash
mvn verify
```

The test coverage report will be available at: `target/site/jacoco/index.html`

## Resilience Features

The application includes several resilience patterns:

1. **Circuit Breaker**: Prevents system overload
2. **Rate Limiting**: Controls request rates
3. **Bulkhead**: Isolates failures
4. **Retry**: Handles transient failures

Monitor these features through actuator endpoints:
```
http://localhost:8080/actuator/health
http://localhost:8080/actuator/metrics
```

## Performance Optimization

The service uses parallel processing for calculations:
- Asynchronous computation of interest rates
- Parallel calculation of payment values
- Non-blocking operations where possible 