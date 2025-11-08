USE malstrek;

CREATE TABLE IF NOT EXISTS race_groups (
    race_group_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    race_type_id TINYINT UNSIGNED NOT NULL,
    race_distance_id TINYINT UNSIGNED NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TINYTEXT NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_race_groups_race_types_race_type_id
        FOREIGN KEY (race_type_id) 
        REFERENCES race_types (race_type_id)
        ON DELETE CASCADE 
        ON UPDATE RESTRICT,
    CONSTRAINT fk_race_groups_race_distances_race_distance_id
        FOREIGN KEY (race_distance_id) 
        REFERENCES race_distances (race_distance_id)
        ON DELETE CASCADE 
        ON UPDATE RESTRICT
);
