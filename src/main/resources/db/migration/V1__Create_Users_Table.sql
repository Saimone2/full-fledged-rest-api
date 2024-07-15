CREATE TABLE users (
   id BIGSERIAL PRIMARY KEY,
   email VARCHAR(255) UNIQUE NOT NULL,
   password VARCHAR(255) NOT NULL,
   role VARCHAR(255),
   enabled BOOLEAN NOT NULL,
   created_at TIMESTAMP,
   updated_at TIMESTAMP
);