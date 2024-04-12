CREATE TABLE confirmation_tokens (
     id BIGSERIAL PRIMARY KEY,
     token VARCHAR(255),
     revoked BOOLEAN,
     expired BOOLEAN,
     user_id BIGINT,
     created_at TIMESTAMP,
     assignment VARCHAR(255),
     FOREIGN KEY (user_id) REFERENCES users(id)
);