USE malstrek;

CREATE TABLE IF NOT EXISTS race_groups (
    race_group_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    race_type_id INT UNSIGNED NOT NULL,
    description TINYTEXT NOT NULL,
    updated_datetime DATETIME NOT NULL,
    PRIMARY KEY (race_group_id)
);
