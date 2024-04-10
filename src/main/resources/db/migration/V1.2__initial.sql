CREATE TABLE "confirmation_tokens" (
     id         BIGINT NOT NULL PRIMARY KEY,
     token      VARCHAR(255),
     user_id    BIGINT,
     FOREIGN KEY (user_id) REFERENCES users(id),
     revoked    BOOLEAN DEFAULT FALSE,
     expired    BOOLEAN DEFAULT FALSE,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);