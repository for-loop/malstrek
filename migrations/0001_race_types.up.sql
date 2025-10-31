USE malstrek;

CREATE TABLE IF NOT EXISTS race_types (
    race_type_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    updated_datetime DATETIME NOT NULL
);
