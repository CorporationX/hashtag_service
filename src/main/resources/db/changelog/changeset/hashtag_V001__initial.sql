CREATE TABLE hashtag(
    id          BIGINT       PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY UNIQUE,
    content     VARCHAR(255) NOT NULL UNIQUE,
    mentions    BIGINT       NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    DEFAULT current_timestamp,
    updated_at  TIMESTAMP    DEFAULT current_timestamp
);