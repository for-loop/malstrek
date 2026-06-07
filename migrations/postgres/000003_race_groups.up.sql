CREATE TABLE IF NOT EXISTS race_groups (
    race_group_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    race_type_id SMALLINT NOT NULL,
    race_distance_id SMALLINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
