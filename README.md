# EchoTrail – Time-Capsule Journal & Memory Sharing Platform

EchoTrail is a memory capsule platform that allows users to capture thoughts, experiences, and media as digital capsules — with the ability to unlock them in the future or share them publicly. It combines the essence of journaling, blogging, and digital time-travel into a powerful full-stack product.

---

## Why EchoTrail?

We live in a fast-paced world where personal reflections, memories, and emotions often get buried. EchoTrail offers:

- A place to record private thoughts and reflections that can be unlocked in the future
- A way to publicly share thoughts in a structured, creative format
- Time-based capsule locking that creates anticipation and value
- A journaling platform that is built for self-reflection, personal growth, and storytelling

Unlike Instagram or blogging platforms, EchoTrail emphasizes time, privacy control, and emotional value.

| Feature/Philosophy           | EchoTrail                                        | Instagram / Facebook / Blogs                        |
|-----------------------------|--------------------------------------------------|-----------------------------------------------------|
| Core Purpose                | Time-based personal reflection & memory capsules | Social sharing, instant gratification, engagement   |
| Time Capsules               | Yes – capsules can be locked until a future date | No – all content is instantly visible               |
| Lock/Unlock Mechanism       | Built-in support for unlockAt datetime           | Not available                                       |
| Fine-grained Privacy        | Private / Friends-only / Public with lock options| Public or friends; no timed visibility              |
| Content Format              | Markdown with preview support                    | Mostly visual content (photos/videos)               |
| Long-form Storytelling      | Yes – ideal for deep thoughts, retrospection     | Limited; typically short captions or posts          |
| Friend-Based Sharing        | Yes – capsule visibility can be friend-restricted| Yes – but no content-level control                  |
| Scheduled Release           | Yes – capsules become visible in future          | No – must post in real-time                         |
| Public Feed                 | Optional – user decides what becomes public      | Default – everything posted is public/friends       |
| Capsule Chaining            | Yes – sequence capsules like journal series      | No – content not inherently linkable in order       |
| Media Storage               | Yes – attach files, media (S3/Supabase support)  | Yes – but locked media or timed visibility isn't native |
| Targeted Audience           | Journaling enthusiasts, reflective users         | General-purpose users, creators, influencers        |
| Mindful Usage               | Encouraged – content is written for self/future  | Addictive scrolling and validation loop             |

---

## Target Audience

- Individuals who love journaling, time-capsules, or reflective writing
- Writers, bloggers, and memory-keepers looking for a unique publishing style
- Users who want to store digital memories for future reflection or sharing
- Social users who want to share moments selectively with friends or the world

---

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
| JWT Auth + Gateway               | Token-based authentication & validation at API Gateway                     |
| Microservices Architecture       | Independent services for scalability and clean domain separation           |

---

## Microservices Structure

- UserMS – User registration, login, JWT, profile
- CapsuleMS – Create, lock, unlock capsules
- FriendshipMS – Friend request, friend list
- Gateway – Token validation and routing
- FeedService – Public feed aggregation (Maybe using Bloom Filters)
- SchedulerService – Unlock public capsules automatically in background
- UploaderService – Used to upload images/videos to blob store and get the mediaUrl
- NotificationService – To notify users regarding updates from other services

---

## Sample Use Cases

1. User Santhosh creates a capsule today and locks it for December 2026.
2. Capsule remains invisible to others until unlock time.
3. Unlocking can be done after unlockAt time by:
   - Only the user (if private)
   - Friends (if shared)
   - Everyone (if public -- unlocked automatically)
4. User browses the feed to see unlocked public capsules / locked public capsules that can be saved for future.
5. All capsules are markdown-based and may contain text + images/videos.

---

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

