CREATE TABLE IF NOT EXISTS hashtags (
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    name varchar(128) UNIQUE NOT NULL,
    user_id bigint NOT NULL default 0,
    created_at timestamptz DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS post_hashtags (
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    post_id bigint NOT NULL,
    hashtag_id bigint NOT NULL,

    CONSTRAINT fk_hashtag_id FOREIGN KEY (hashtag_id) REFERENCES hashtags (id)
);

CREATE INDEX IF NOT EXISTS idx_hashtags_name ON hashtags (name);
CREATE INDEX IF NOT EXISTS idx_post_hashtags_hashtag_id ON post_hashtags (hashtag_id);
CREATE INDEX IF NOT EXISTS idx_post_hashtags_post_id ON post_hashtags (post_id);
