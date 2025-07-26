USE malstrek;

CREATE TABLE IF NOT EXISTS finishers (
    finisher_id INT NOT NULL AUTO_INCREMENT,
    race_number INT UNSIGNED NOT NULL,
    timestamp BIGINT NOT NULL,
    bib_number INT,
    deleted TINYINT(1) NOT NULL,
    PRIMARY KEY (finisher_id)
);
