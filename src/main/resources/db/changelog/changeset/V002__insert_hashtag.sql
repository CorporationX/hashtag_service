INSERT INTO hashtags (name, created_at, user_id)
VALUES
    ('corporationX', CURRENT_TIMESTAMP, 0),
    ('devEasterEgg', CURRENT_TIMESTAMP, 0)
ON CONFLICT (name) DO NOTHING;
