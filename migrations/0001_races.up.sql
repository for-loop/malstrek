USE malstrek;

CREATE TABLE IF NOT EXISTS races (
    race_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    race_number INT UNSIGNED NOT NULL UNIQUE,
    race_group_id INT UNSIGNED NOT NULL,
    status ENUM('ON', 'TENTATIVE', 'CANCELED') default 'ON' NOT NULL,
    start_datetime DATETIME NOT NULL,
    end_datetime DATETIME NOT NULL,
    updated_datetime DATETIME NOT NULL,
    PRIMARY KEY (race_id)
);
