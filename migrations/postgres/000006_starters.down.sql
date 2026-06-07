-- Remove compression policy before dropping table
SELECT remove_compression_policy('starters', if_exists => TRUE);

DROP TABLE IF EXISTS starters;
