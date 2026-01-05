# EchoTrail

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-24.0-blue.svg)](https://www.docker.com)
[![Kafka](https://img.shields.io/badge/Kafka-3.4-black.svg)](https://kafka.apache.org)

## The Story Behind EchoTrail

EchoTrail was born **not** out of a need for another social app, but out of a curiosity to explore **Distributed Systems**. My goal was to learn and understand the concepts of distributed systems,
and how to build a scalable, event-driven microservices architecture. This is not a project that's
going to be used in production, but it's a fun way to learn and understand the concepts of distributed systems. Because it's not going into production, we can experiment in whatever way we want.

**Is this just an unfinished side project?** Nope! It's _intentionally unfinished with no UI and over-engineered_ for learning purposes. The implementation itself is fairly robust — it has unit tests, integration tests, latency benchmarks, CI checks, docker-compose setup and proper error handling.

**But here's the thing:** no rational startup would launch with this architecture. Postgres + Cassandra + Kafka + Debezium + Eureka + microservices for a simple social app with zero users? That's engineering malpractice. You'd start with a monolith, a single database, and deploy on any platform. You'd only reach for this complexity after you've proven product-market fit and scaled to millions of users.

I contribute to OSS projects and also starting to build my own. In those projects, you can't just throw in Kafka and Cassandra, "for learning" — you have to be pragmatic, keep setup simple, and make decisions that serve real users. Over-engineering is irresponsible when people depend on your software. 

**That's precisely why I build EchoTrail.** It's a sandbox where I can implement distributed systems patterns without the constraints of production responsibility. No users to disappoint, no operational burden on self-hosters, just pure architectural learning.

**So why build it this way?** Because the learning happens in the complexity. Implementing CQRS, the Transactional Outbox Pattern, CDC pipelines, and distributed transactions teaches you patterns that matter at scale.

## Architecture

EchoTrail is designed to handle high concurrency and ensure eventual consistency across a polyglot persistence layer.

**Component Overview:**

- **API Gateway** (:8090) — Single entry point for all client requests
- **Eureka Service Registry** (:8761) — Service discovery; services register themselves dynamically
- **Microservices:**
  - **UserMS** (:8080) — User authentication and identity management
  - **CapsuleMS** (:8081) — Core business logic with CQRS pattern
  - **FriendshipMS** (:8082) — Social graph and relationships
- **Write Model:**
  - **Postgres** — ACID-compliant transactional database for writes
  - **Outbox Table** — Transactional staging for events (in Postgres)
- **CDC Pipeline:**
  - **Debezium** — Change Data Capture connector reading Postgres WAL
  - **Kafka** (:29092) — Distributed event streaming platform
- **Read Model:**
  - **Cassandra** — Denormalized data optimized for high-speed reads

**Request Flow:**

1. Client requests enter through the API Gateway
2. Gateway queries Eureka to discover service locations (no hardcoded URLs)
3. All microservices (UserMS, CapsuleMS, FriendshipMS) register with Eureka
4. Gateway forwards requests to the appropriate service
5. UserMS and FriendshipMS write directly to Postgres
6. CapsuleMS writes to both the domain table and the Outbox table in a single transaction
7. Debezium monitors the Outbox table and publishes changes to Kafka
8. CapsuleMS consumes Kafka events and updates Cassandra for read queries

### 1. The Entry Point: Gateway & Discovery

Every request starts at the **API Gateway**, the guardian of our ecosystem. It doesn't know where the services live, and it doesn't need to. That's the job of the **Service Registry (Eureka)** — the phonebook. Services register themselves dynamically, allowing the Gateway to route traffic intelligently without hardcoded URLs.

### 2. Authentication: JWT at the Gate

_Why authenticate at the Gateway instead of each service?_

Centralizing authentication means downstream services don't need to know about tokens, secrets, or validation logic. They simply trust headers from the Gateway.

**Authentication Flow:**

**Public Endpoints (Login/Register):**

1. Client sends `POST /api/auth/login` to Gateway
2. Gateway forwards the request to UserMS (no JWT validation)
3. UserMS validates credentials and returns a signed JWT token to the client

**Protected Endpoints (e.g., Capsules, Friendships):**

1. Client includes `Authorization: Bearer <token>` header in the request
2. Gateway validates the JWT signature using the shared secret key
3. Gateway extracts the username from the token and queries UserMS to resolve the userId
4. UserMS returns the userId (e.g., `123`)
5. Gateway enriches the request with `X-Username` and `X-UserId` headers
6. Gateway forwards the enriched request to downstream services (CapsuleMS/FriendshipMS)
7. Downstream services trust the injected headers — no re-validation needed
8. Service returns the response to the client via Gateway

This pattern keeps services focused on business logic while the Gateway handles cross-cutting security concerns.

### 3. Core Services

We split the monolith into domain-specific services:

#### User Service (UserMS)

Manages identity and authentication. Stores user credentials in Postgres and issues JWT tokens on login.

#### Friendship Service (FriendshipMS)

Handles the **social graph** — the relationships between users.

_Why a separate service?_

The social graph grows quadratically with users (N users = up to N² relationships). Isolating it allows independent scaling and prevents friend-related queries from impacting core business logic.

**State Machine:**

**Friendship State Transitions:**

- **PENDING** → Created when a user sends a friend request
  - **→ ACCEPTED** if the receiver accepts the request
  - **→ REJECTED** if the receiver declines the request
- **ACCEPTED** → Mutual friendship established
  - **→ (Deleted)** when users unfriend each other
  - **→ BLOCKED** if one user blocks the other
- **BLOCKED** → Can be created from any state; prevents future interactions

> [!NOTE]
> All endpoints use the `X-UserId` header injected by the Gateway — no JWT validation needed at the service level.

#### Capsule Service (CapsuleMS)

The heart of the application. Manages time-locked messages ("Capsules") using CQRS with Postgres for writes and Cassandra for reads.

**O(1) Time Chain Navigation:**

Each capsule can be linked to form a chronological "Time Chain" using a doubly-linked list structure. The `capsule_chain` table in Cassandra stores:

- `previous_capsule_id` — pointer to the previous capsule
- `next_capsule_id` — pointer to the next capsule

This design enables **constant-time traversal** in both directions:

- Navigating to the next capsule: Single lookup by `next_capsule_id`
- Navigating to the previous capsule: Single lookup by `previous_capsule_id`

**Why not use timestamps for ordering?**

Traditional timestamp-based queries (`WHERE timestamp > X ORDER BY timestamp LIMIT N`) require:

- Range scans across potentially millions of records
- Sorting operations at query time
- Inefficient pagination for deep history

With pointer-based chaining, we trade a small amount of write complexity (updating pointers) for guaranteed O(1) read performance, regardless of chain length. This is critical for the "Time Chain" feature where users navigate through their capsule history.

### 4. The Data Dilemma & CQRS

Here is where the story gets interesting.

We needed **Strong Consistency (ACID)** for creating capsules (money/transactions equivalent importance) but **Massive Read Scalability** for the "Time Chain" (the social feed).

Relational databases excel at consistency but struggle at scale. NoSQL (Cassandra) excels at reads but lacks joins.

**The Real Challenge: Distributed Atomicity**

If we used Postgres alone, ensuring atomicity would be trivial — just slap an `@Transactional` annotation on the method and call it a day. But we're using **two databases**: Postgres for writes and Cassandra for reads. There's no native way to wrap both in a single transaction. Interesting problem, right? This is the distributed systems problem that makes CQRS necessary.

**The Solution: CQRS (Command Query Responsibility Segregation)**

| Concern               | Technology | Why?                                                                 |
| --------------------- | ---------- | -------------------------------------------------------------------- |
| **Writes (Commands)** | Postgres   | ACID compliance, referential integrity, mature ecosystem             |
| **Reads (Queries)**   | Cassandra  | Horizontal scalability, high write throughput, sub-millisecond reads |

### 5. The Sync Pipeline

How do we get data from Postgres to Cassandra without dual-writes (which are prone to data inconsistencies)?

We employed the **Transactional Outbox Pattern**:

**Transactional Outbox Pattern Flow:**

**Step 1: Atomic Write (Single Transaction)**

- CapsuleMS executes a single Postgres transaction:
  - Inserts the capsule record into the business table
  - Inserts an event record into the outbox table
- Both inserts commit atomically — either both succeed or both fail

**Step 2: Change Data Capture**

- Debezium continuously monitors the Postgres Write-Ahead Log (WAL)
- Detects the new outbox event and extracts the change
- Publishes the event to the corresponding Kafka topic

**Step 3: Event Consumption & Projection**

- CapsuleMS consumes the event from Kafka
- Transforms the data into the denormalized read model schema
- Writes the projection to Cassandra for high-speed queries

**Guarantees:** If the Postgres transaction commits, the event will eventually reach Cassandra (at-least-once delivery). The outbox table acts as a durable buffer, ensuring no data loss even if Kafka or the consumer is temporarily unavailable.

This pipeline ensures that if the database transaction commits, the event _will_ eventually reach Cassandra. No data lost.

**Why Kafka instead of RabbitMQ or direct CDC to Cassandra?**

Imagine we wanted to add a **NotificationService** to alert friends when a new capsule is created, or an **AnalyticsService** to track user engagement metrics. With Kafka's pub/sub model, both services could consume the _same_ capsule creation events already flowing through the pipeline — no changes to CapsuleMS required.

Currently, only **CapsuleMS** consumes these events to update the Cassandra read model. We're not implementing those additional services, but Kafka's architecture justifies its use: it decouples event producers from consumers, allowing multiple independent services to react to the same events. This is a core principle of event-driven systems and far more flexible than point-to-point alternatives like direct CDC or traditional message queues like RabbitMQ.

## Sync Latency Metrics

A distributed system is only as good as its lag. We measure the **Sync Latency** — the time between the commit in Postgres and the availability of the data in Cassandra.

Our [load tests](CapsuleMS/src/test/java/com/echotrail/capsulems/service/CapsuleE2ELatencyTest.java) prove the efficiency of this pipeline (see [detailed logs](CapsuleMS/src/test/latency_logs)):

| Scenario          | Load                    | Actual Performance |
| :---------------- | :---------------------- | :----------------- |
| **Low Load**      | 50 concurrent requests  | **~300-400ms**     |
| **Regular Load**  | 100 concurrent requests | **~400-500ms**     |
| **Moderate Load** | 200 concurrent requests | **~1200-1400ms**   |

We specifically aim for **sub-second latency** for regular usage, ensuring users effectively perceive the system as "real-time" despite the eventual consistency model.

### Why Sub-Second Matters

- **Debezium** polls WAL every 10ms for near-instant change detection
- **Kafka** provides durable, high-throughput message delivery
- **Cassandra** is optimized for fast writes with tunable consistency
  
## Quick Start

```bash
# Start all infrastructure and services
docker compose up -d

# Register Debezium connector (after services are healthy)
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium-connector-config.json
```

## Project Structure

```
EchoTrail/
├── Gateway/           # API Gateway (Spring Cloud Gateway)
├── ServiceRegistry/   # Eureka Service Discovery
├── UserMS/            # User Management Service
├── FriendshipMS/      # Social Graph Service
├── CapsuleMS/         # Core Business Logic + CQRS
├── docker-compose.yml # Full infrastructure stack
└── debezium-connector-config.json
```

## Conclusion

Built to understand distributed systems, not to demo frameworks. 
If the architecture or trade-offs resonated with you, consider starring ⭐ this repo.
