DROP TABLE "user";

CREATE TABLE IF NOT EXISTS "users" (
     id BIGINT PRIMARY KEY,
     email VARCHAR(255) UNIQUE NOT NULL,
     password VARCHAR(255) NOT NULL,
     role VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "tokens" (
     id BIGINT PRIMARY KEY,
     token VARCHAR(255),
     token_type VARCHAR(50),
     revoked BOOLEAN DEFAULT FALSE,
     expired BOOLEAN DEFAULT FALSE,
     user_id BIGINT,
     FOREIGN KEY (user_id) REFERENCES users(id)
);