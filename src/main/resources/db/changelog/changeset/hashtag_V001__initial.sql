CREATE TABLE hashtag(
    id          BIGINT       PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY UNIQUE,
    content     VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMP    DEFAULT current_timestamp,
    updated_at  TIMESTAMP    DEFAULT current_timestamp
);

CREATE TABLE hashtag_posts(
    hashtag_id BIGINT,
    post_id    BIGINT,
    CONSTRAINT fk_hashtag_posts_hashtag FOREIGN KEY (post_id) REFERENCES post (id)
)