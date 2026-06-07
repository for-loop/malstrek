CREATE TABLE IF NOT EXISTS races (
    race_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    race_group_id INT NOT NULL,
    race_number INT NOT NULL UNIQUE,
    timezone_id SMALLINT NOT NULL,
    status VARCHAR(50) DEFAULT 'ON' NOT NULL CHECK (status IN ('ON', 'TENTATIVE', 'CANCELED')),
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_races_race_groups_race_group_id
        FOREIGN KEY (race_group_id)
        REFERENCES race_groups (race_group_id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,
    CONSTRAINT fk_races_timezones_timezone_id
        FOREIGN KEY (timezone_id)
        REFERENCES timezones (timezone_id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
);
