-- Remove compression policy before dropping table
SELECT remove_compression_policy('finishers', if_exists => TRUE);

DROP TABLE IF EXISTS finishers;
