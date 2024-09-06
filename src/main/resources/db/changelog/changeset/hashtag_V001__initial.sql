CREATE TABLE hashtag (
    id        bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    name      varchar(255) NOT NULL
);

CREATE TABLE hashtag_post (
    id         bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    post_id    bigint NOT NULL,
    hashtag_id bigint NOT NULL,

    CONSTRAINT fk_post_id FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    CONSTRAINT fk_hashtag_id FOREIGN KEY (hashtag_id) REFERENCES hashtag (id) ON DELETE CASCADE
);