USE malstrek;

CREATE TABLE IF NOT EXISTS races (
    race_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    race_group_id INT UNSIGNED NOT NULL,
    race_number INT UNSIGNED NOT NULL UNIQUE,
    status ENUM('ON', 'TENTATIVE', 'CANCELED') default 'ON' NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_races_race_groups_race_group_id
        FOREIGN KEY (race_group_id) 
        REFERENCES race_groups (race_group_id)
        ON DELETE CASCADE 
        ON UPDATE RESTRICT
);
