-- Ensure clean creation of the view
DROP VIEW IF EXISTS v_race_finishers;

CREATE VIEW v_race_finishers AS 
SELECT
    -- Generates unique rank per finisher relative to their own race group
    DENSE_RANK() OVER (PARTITION BY race_number ORDER BY d.duration ASC) AS finishers,
    finisher_id,
    race_number,
    bib_number,
    TO_CHAR(d.duration, 'HH24:MI:SS') AS duration, 
    CAST(EXTRACT(EPOCH FROM d.duration) / 60.0 AS NUMERIC) AS minutes, 
    d.duration AS precise_duration,
    f.timestamp AS finisher_time
FROM finishers AS f
INNER JOIN (
    -- Isolates the single earliest valid row for races with multi-start records
    SELECT DISTINCT ON (race_number) 
        race_number, 
        timestamp
    FROM starters
    WHERE NOT deleted
    ORDER BY race_number, timestamp ASC
) AS s USING (race_number)
CROSS JOIN LATERAL (
    -- Evaluates interval math safely once per row to ensure optimal query flattening
    SELECT f.timestamp - s.timestamp AS duration
) AS d
WHERE NOT f.deleted;
