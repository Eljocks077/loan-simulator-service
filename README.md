# Sistema de Simulação de Empréstimos - Documentação Técnica

## Visão Geral da Arquitetura

O sistema foi projetado utilizando uma arquitetura em camadas (layered architecture) com padrões de microsserviços, garantindo escalabilidade e facilidade de manutenção.

## 1. Desenho da Arquitetura

![Arquitetura do Sistema](docs/images/architecture.png)

### i. Arquitetura do Sistema

#### Componentes e Interações:

##### Frontend Layer (React App)
- Interface do usuário
- Validação inicial de dados
- Exibição de resultados
- Envia requisições para o API Gateway
- Realiza polling para simulações assíncronas
- Recebe notificações do SNS

##### API Layer (AWS API Gateway)
- Ponto de entrada único
- Roteamento, autenticação, rate limiting
- Encaminha requisições para a camada de orquestração

##### Orchestration Layer
- Smart Router + Rate Limiter
- Rate Limiter: Controla o fluxo de requisições baseado em limites configuráveis
- Smart Router: Escolhe entre processamento síncrono ou assíncrono baseado na carga atual
- Monitora métricas de sistema (CPU, memória, fila SQS)
- Implementa estratégias de balanceamento de carga

##### Processing Layer (Java 21 + Spring Boot)
- Fluxo Síncrono: Processa simulações de baixa carga diretamente
- Fluxo Assíncrono: Envia para AWS SQS quando excede limites do rate limiter
- Consome mensagens da fila para simulação assíncrona
- Notifica conclusão via SNS

##### Data Layer
- PostgreSQL: dados transacionais e histórico de simulações
- Redis: cache para simulações e sessões de usuário
- S3: configurações de taxas, políticas e backups

##### Notification Layer (AWS SNS)
- Envia notificações sobre finalização de simulação assíncrona
- Integração com frontend para atualização em tempo real

### ii. Justificativas Tecnológicas

#### Frontend - React
- Virtual DOM e componentização para manutenção
- TypeScript para evitar bugs e melhor developer experience

#### API Gateway - AWS
- Gerenciamento de segurança e versionamento centralizados
- Escalabilidade e monitoramento nativos
- Rate limiting nativo integrado

#### Rate Limiter Strategy
- Algoritmo Token Bucket: Permite rajadas controladas
- Métricas dinâmicas: Ajuste baseado em carga do sistema

#### PostgreSQL
- Suporte a ACID, consultas complexas e alta confiabilidade
- Particionamento para grandes volumes de dados

#### Redis
- Alta performance com latência sub-milissegundo
- Suporte a estruturas de dados complexas para cache

#### AWS SQS
- Garantia de entrega e escalabilidade automática
- Dead Letter Queue para tratamento de falhas

#### AWS SNS
- Notificações multi-canal e assíncronas
- Integração com múltiplos protocolos

## 2. Padrões de Projeto e Boas Práticas

### i. Padrões Arquiteturais
- Rate Limiter Pattern: Controla fluxo de requisições e previne sobrecarga
- BFF (Backend for Frontend): Agrega dados de múltiplos serviços
- CQRS Simplificado: Comando e leitura separados (PostgreSQL e Redis)
- Repository Pattern: Abstração do acesso aos dados
- Observer Pattern: Para notificações assíncronas

### ii. Estratégia de Rate Limiting

#### Configuração do Rate Limiter:
- Limite por usuário: 10 requisições/minuto
- Limite global: 1000 requisições/minuto
- Threshold para processamento assíncrono: 80% da capacidade
- Algoritmo: Token Bucket com refill rate configurável

#### Lógica de Decisão:
```java
if (current_load < sync_threshold && tokens_available) {
    process_synchronously()
} else {
    queue_for_async_processing()
}
```

### iii. Autenticação e Autorização
- JWT para autenticação
- RBAC (Role-Based Access Control) para permissões
- Integração com AWS Cognito ou OAuth
- Rate limiting por nível de usuário (premium vs standard)

## 3. Considerações de Escalabilidade
- Rate Limiter distribuído usando Redis para coordenação
- SQS para desacoplar processos e absorver picos de carga
- Auto scaling nos serviços de backend baseado em métricas
- Cache em Redis para reduzir acessos ao banco
- Horizontal scaling com load balancers
- Database sharding para grandes volumes

## 4. API Design

### i. Endpoints

#### Simulação Síncrona/Assíncrona:
```http
POST /api/v1/loan/simulate
Content-Type: application/json

{
  "amount": 10000,
  "birthDate": "1990-01-01",
  "term": 12,
  "preferAsync": false
}
```

#### Resposta Síncrona:
```json
{
  "requestId": "uuid-123",
  "type": "sync",
  "result": {
    "monthlyPayment": 858.75,
    "totalPayment": 10305.0,
    "totalInterest": 305.0,
    "interestRate": "3%"
  }
}
```

#### Resposta Assíncrona:
```json
{
  "requestId": "uuid-456",
  "type": "async",
  "status": "queued",
  "estimatedCompletion": "2025-07-03T10:30:00Z",
  "message": "Simulação em processamento. Você será notificado quando concluída."
}
```

#### Consulta de Status:
```http
GET /api/v1/loan/simulate/{requestId}/status
``` 