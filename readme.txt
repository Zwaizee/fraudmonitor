Key Components & Responsibilities


Controller

TransactionMonitorController: Thin controller; maps requests to services, no repository calls.
FraudAlertController: Thin controller; delegates to FraudAlertService for closing/listing alerts.

Swagger page: http://localhost:8080/swagger-ui/index.html#

Endpoints:

http://localhost:8080/api/transactions?accountId=123589741

http://localhost:8080/api/transactions/1

http://localhost:8080/api/transactions

http://localhost:8080/api/fraud-alerts/1/close

http://localhost:8080/api/fraud-alerts?status=OPEN


Services

FraudDetectionService: Orchestrates rules, persistence, alert creation, and notifications.
FraudAlertService: Owns alert lifecycle (e.g., CLOSE)
NotificationService (NotificationServiceImpl): Builds alert messages; sends email + WhatsApp.



Domain / Rules

FraudRuleEngine: Stateless rule evaluation—amount thresholds, velocity, geo mismatch, odd hours, blacklisted merchants, new device high amount. Null‑safe guards (e.g., eventTime, category) to prevent NPEs.



Persistence

TransactionEntity: Transaction details, fraud flags, reasons.
FraudAlertEntity: Fraud alert.
TransactionRepo, FraudAlertRepo: JPA repositories.



Configuration

application.yml: DB, SMTP, Twilio; environment overrides for Docker/Prod.


4) Data Model (Core Entities)
TransactionEntity (simplified)

id: Long @Id
accountId: String
amount: BigDecimal
category: CategoryEnum (@Enumerated(EnumType.STRING))
channel: ChannelEnum (@Enumerated(EnumType.STRING))
currency: CurrencyEnum (@Enumerated(EnumType.STRING))
merchant, countryCode, deviceId
eventTime: LocalDateTime
fraudulent: boolean
fraudReason: String (comma‑separated)

FraudAlertEntity

id: Long @Id
version: Long @Version ← Optimistic locking
transactionId: Long
accountId: String
reasons: String
status: String (OPEN/CLOSED)
createdAt: LocalDateTime

Indexes: Create indexes on (accountId, eventTime), (status), and (transactionId) for common queries.

Mappers: MapStruct API

MapStructs: translate entities to dto and vice versa

TransactionEvent Request:

{
    "accountId":"123589741",
    "transactionId":"02",
    "country":"USA",
    "amount":12500.00,
    "currency":"USD",
    "category":"ONLINE",
    "channel":"WEB",
    "merchant":"SCAM MART",
    "countryCode":"US",
    "deviceId":"A-34",
    "userEmail":"zwai@gmail.com",
    "userPhone":"+27123456789",
    "fraudulent":true,
    "eventTime":"2025-01-09T12:30:00Z",
    "fraudReason":"Amount exceeds category threshold"
  }

  Technology:
  Springboot
  Mapstruct
  Maven
  Postgress
  H2 in memory
  Jpa
  Docker

docker command: docker build .