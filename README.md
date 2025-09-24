# EchoTrail – Time-Capsule Journal & Memory Sharing Platform

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1.2-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.java.com)
[![Maven](https://img.shields.io/badge/Maven-3.8.4-blue.svg)](https://maven.apache.org)
[![Docker](https://img.shields.io/badge/Docker-24.0-blue.svg)](https://www.docker.com)

EchoTrail is a memory capsule platform that allows users to capture thoughts, experiences, and media as digital capsules — with the ability to unlock them in the future or share them publicly. It combines the essence of journaling, blogging, and digital time-travel into a powerful full-stack product.

## Table of Contents

- [About the Project](#about-the-project)
- [Key Features](#key-features)
- [Microservices Architecture](#microservices-architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
- [License](#license)

## About the Project

We live in a fast-paced world where personal reflections, memories, and emotions often get buried. EchoTrail offers:

- A place to record private thoughts and reflections that can be unlocked in the future
- A way to publicly share thoughts in a structured, creative format
- Time-based capsule locking that creates anticipation and value
- A journaling platform that is built for self-reflection, personal growth, and storytelling

Unlike Instagram or blogging platforms, EchoTrail emphasizes time, privacy control, and emotional value.

## Key Features

| Feature                          | Description                                                                 |
|----------------------------------|-----------------------------------------------------------------------------|
| Create Capsules                  | Users can write rich content (Markdown supported) and upload media         |
| Capsule Chaining                 | Capsules can be connected like a series — enabling sequenced storytelling  |
| Lock & Unlock Capsules           | Capsules can be locked until a future date (unlockAt)                      |
| Private, Friend, or Public View  | Capsules can be private, shared with friends, or made public               |
| Friend System                    | Users can send/accept friend requests like Facebook                        |
| Public Feed                      | View capsules shared by others when they are unlocked                      |
| Background Unlocker              | Scheduled task/service unlocks public capsules when unlockAt time is reached |
| Markdown Support                 | Rich content editing using markdown with preview rendering                 |

## Microservices Architecture

- **UserMS** – User registration, login, JWT, profile
- **CapsuleMS** – Create, lock, unlock capsules
- **FriendshipMS** – Friend request, friend list
- **Gateway** – Token validation and routing
- **ServiceRegistry** – Eureka Service Registry for service discovery
- **FeedService** – Public feed aggregation (Maybe using Bloom Filters)
- **SchedulerService** – Unlock public capsules automatically in background
- **UploaderService** – Used to upload images/videos to blob store and get the mediaUrl
- **NotificationService** – To notify users regarding updates from other services

## Tech Stack

| Layer            | Tech                                |
|------------------|-------------------------------------|
| Frontend         | React Native / React.js             |
| Backend          | Spring Boot                         |
| API Gateway      | Spring Cloud Gateway                |
| Service Registry | Eureka                              |
| Database         | PostgreSQL & Cassandra (NoSql use case)|
| Caching          | Redis                               |
| Auth             | JWT & Oauth with Spring Security    |
| Message Queue    | Kafka or RabbitMQ                   |
| Storage          | Supabase / S3 (for media)           |

## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

- Java 17
- Docker
- Docker Compose
- Maven

### Installation

1. **Clone the repo**
   ```sh
   git clone https://github.com/santhosh-kumar-mc/EchoTrail.git
   ```
2. **Build the services**
   ```sh
   cd EchoTrail
   mvn clean install
   ```
3. **Run the services**
   ```sh
   docker-compose up -d
   ```

## Usage

The API Gateway is the single entry point for all the microservices. The default port for the gateway is `8080`. You can find the Swagger UI at `http://localhost:8080/swagger-ui.html`.

## License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.
