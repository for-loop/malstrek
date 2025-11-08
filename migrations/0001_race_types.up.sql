USE malstrek;

CREATE TABLE IF NOT EXISTS race_types (
    race_type_id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP 
);

INSERT INTO race_types 
    (type) 
VALUES
    ('Standard road'),
    ('Trail Runs'),
    ('Fun Runs / Novelty Races'),
    ('Track Events'),
    ('Cross Country');
