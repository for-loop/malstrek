USE malstrek;

CREATE TABLE IF NOT EXISTS race_distances (
    race_distance_id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    distance VARCHAR(100) NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP 
);

INSERT INTO race_distances 
    (distance) 
VALUES
    ('1 Mile'),
    ('5K'),
    ('10K'),
    ('12K'),
    ('Half Marathon'),
    ('Marathon');
