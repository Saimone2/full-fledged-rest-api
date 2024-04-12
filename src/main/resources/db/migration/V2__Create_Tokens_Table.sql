CREATE TABLE tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255),
    token_type VARCHAR(255),
    revoked BOOLEAN,
    expired BOOLEAN,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);