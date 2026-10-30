# Banking Microservice System

A microservices-based banking application built with Java and Spring Boot.

Microservice Design Patterns
Category 1: Service Communication and Discovery
-----
Service Registry (1.a): eureka-server tracks active service instances via heartbeats.
API Gateway (1.b) & Discovery (1.c): External clients hit port 8080. The gateway resolves target host instances using logical IDs (lb://ACCOUNT-SERVICE) via Eureka.

Category 2: Data and Transaction Management (Working on 2.b and 2.c)
Database-per-Service (2.a): account-service queries Oracle SQL (1521), while transaction-service queries PostgreSQL (5432).
Saga Pattern (2.b): Multi-step money transfers use Choreography (Kafka events) or Orchestration to execute local transactions with compensating rollbacks on failure.
CQRS (2.c): Splits command write models from read query projections for fast account statement lookups.
Category 3: Resilience and Fault Handling
Circuit Breaker (3.a): Prevents cascading failures by tripping open when downstream services (e.g., Fraud or Notification) experience high latency.
Bulkhead (3.b): Isolates execution threads so heavy report queries do not starve core fund-transfer threads.
Category 4: Architecture and Integration
BFF (4.a): Custom route rules for Mobile clients versus Web dashboards.
Entity & Aggregate (4.b): Domain-Driven Design (DDD) boundaries where Account acts as the aggregate root.
Strangler Pattern (4.c): Gradual migration of legacy routes from Oracle monolithic paths to new microservices.
Category 5: Event and Communication
Event-Driven Architecture (5.a): Asynchronous communication via Kafka topics (transfer-events).
Sidecar (5.b): Auxiliary monitoring and log containers running alongside core microservices.
Adapter Microservices (5.c): Translation adapters converting internal domain events into external payment gateway formats.

## HLD on following services links

                        [ Mobile / Web Client ]
                                   │
                                   ▼
                       API Gateway (Port 8080)
                   (Category 1.b & 4.a: BFF / Routing)
                                   │
             ┌─────────────────────┴─────────────────────┐
             ▼                                           ▼
  Account Service (Port 8081)               Transaction Service (Port 8082)
    [ Oracle DB - account_db ]                [ PostgreSQL - transaction_db ]
   (Category 2.a: DB-per-Service)            (Category 2.a: DB-per-Service)
             │                                           │
             └─────────────────────┬─────────────────────┘
                                   │
                         Apache Kafka Event Broker
                        (Category 5.a: Event-Driven)
                                   │
         ┌─────────────────────────┼─────────────────────────┐
         ▼                         ▼                         ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│Notification Svc │       │Audit & Ledger   │       │Fraud Detection  │
└─────────────────┘       └─────────────────┘       └─────────────────┘

## Services
- **account-service** — handles account creation and management
- **transaction-service** — handles deposits, withdrawals, and transaction history

## Tech Stack
- Java 25 / Spring Boot 4.0.0
- REST APIs
- Docker & Docker Compose
- Maven

## Run Locally
```bash
docker-compose up
```

## Author
Smita Mishra — Senior Java Backend Engineer
[LinkedIn](https://linkedin.com/in/smita-mishra-3993a7135)
