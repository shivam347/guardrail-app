# Assignment

Spring Boot backend service for a social-media style assignment with PostgreSQL and Redis.

## Tech Stack

- Java 17+
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Redis
- Docker Compose

## Features Implemented

### Phase 1: Core API

- Create post
- Add comment
- Like post
- JPA entities for:
  - `User`
  - `Bot`
  - `Post`
  - `Comment`
  - `PostLike`

### Phase 2: Redis Virality Engine and Atomic Locks

- Virality score stored in Redis:
  - Bot reply = `+1`
  - Human comment = `+50`
  - Human like = `+20`
- Horizontal cap:
  - max `100` bot replies per post
  - key: `post:{id}:bot_count`
- Vertical cap:
  - bot reply depth cannot exceed `20`
- Cooldown cap:
  - one bot cannot interact with the same human more than once in `10` minutes
  - key: `cooldown:bot_{id}:human_{id}`

### Phase 3: Notification Engine

- If a bot interacts with a user post:
  - immediate notification if user has no recent notification cooldown
  - otherwise store pending notification in Redis list
- Scheduled sweeper runs every `5` minutes for testing
- Summarizes pending notifications and logs them

## Project Structure

```text
src/main/java/com/Andaz/assignment
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

## Database and Redis Setup

Create a `docker-compose.yml` in the project root:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: assignment-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: socialmediadb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: admin
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7
    container_name: assignment-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

Run:

```bash
docker compose up -d
```
```

## Seed Data

Dummy data can be inserted at startup using `CommandLineRunner`.

Example seeded data:

- User:
  - `username = vidhi`
  - `isPremium = true`
- Bot:
  - `name = trendbot`
  - `personaDescription = A casual and engaging bot`

## API Endpoints

### 1. Create Post

`POST /api/posts`

Request:

```json
{
  "authorType": "USER",
  "authorId": 1,
  "content": "This is my first post"
}
```

### 2. Add Comment

`POST /api/posts/{postId}/comments`

Top-level comment:

```json
{
  "authorType": "USER",
  "authorId": 1,
  "content": "This is my first comment",
  "parentCommentId": null
}
```

Bot reply:

```json
{
  "authorType": "BOT",
  "authorId": 1,
  "content": "Bot reply on post",
  "parentCommentId": 1
}
```

### 3. Like Post

`POST /api/posts/{postId}/like`

Request:

```json
{
  "userId": 1
}
```

## Error Handling

Custom exceptions are handled through a global exception handler.

Examples:

- duplicate like:
  - `Same user cannot like the same post more than once`
- post not found
- user not found
- bot reply limit reached
- bot cooldown active

## Important Redis Keys

- `post:{id}:virality_score`
- `post:{id}:bot_count`
- `cooldown:bot_{id}:human_{id}`
- `user:{id}:notif_cooldown`
- `user:{id}:pending_notifs`
- `pending_notif_users`

## How to Check PostgreSQL Data

Open PostgreSQL inside Docker:

```bash
docker exec -it assignment-postgres psql -U postgres -d socialmediadb
```

Useful commands:

```sql
\dt
SELECT * FROM users;
SELECT * FROM bots;
SELECT * FROM posts;
SELECT * FROM comments;
SELECT * FROM post_likes;
```

## How to Check Redis Data

Open Redis CLI:

```bash
docker exec -it assignment-redis redis-cli
```

Useful commands:

```redis
KEYS *
GET post:1:virality_score
GET post:1:bot_count
GET cooldown:bot_1:human_1
TTL cooldown:bot_1:human_1
GET user:1:notif_cooldown
TTL user:1:notif_cooldown
LRANGE user:1:pending_notifs 0 -1
SMEMBERS pending_notif_users
```

To watch Redis live:

```redis
MONITOR
```

## Test Flow

1. Start PostgreSQL and Redis with Docker Compose
2. Start Spring Boot application
3. Seed dummy user and bot
4. Create a post
5. Add a human comment
6. Add a bot reply
7. Like the post
8. Check PostgreSQL tables
9. Check Redis keys and TTL values

## Notes

- PostgreSQL is the source of truth for persisted content
- Redis is used as the guardrail layer and for temporary distributed state
- Notification batching is done through Redis lists and a scheduled sweeper
