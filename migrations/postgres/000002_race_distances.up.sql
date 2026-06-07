CREATE TABLE IF NOT EXISTS race_distances (
    race_distance_id SMALLINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    distance VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO race_distances (distance) VALUES
    ('1 Mile'),
    ('5K'),
    ('10K'),
    ('12K'),
    ('Half Marathon'),
    ('Marathon');
