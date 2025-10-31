USE malstrek;

CREATE TABLE IF NOT EXISTS starters (
    starter_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    race_number INT UNSIGNED NOT NULL,
    timestamp BIGINT NOT NULL,
    deleted TINYINT(1) NOT NULL
);
