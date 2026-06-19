# Dashboard

## First time

Follow instructions to start Metabase

## Minimal set up

Create a question named "Race Numbers"—this will be used as a dropdown filter on the dashboard

1. Click New button and select "Question"
2. Select `starters` as Data
2. Group by `race_number`
3. Save as "Race Numbers"

Create a question named "All Finishers"

1. Click New button and select "SQL query"
2. Paste this query
    ```sql
    SELECT
        DENSE_RANK() OVER (PARTITION BY race_number ORDER BY d.duration ASC) AS finishers,
        finisher_id,
        race_number,
        bib_number,
        TO_CHAR(d.duration, 'HH24:MI:SS') AS duration,
        CAST(EXTRACT(EPOCH FROM d.duration) / 60.0 AS NUMERIC) AS minutes -- Explicitly cast to numeric so Metabase can finger-print it
    FROM finishers AS f
    INNER JOIN (
        -- Isolates exactly one row per race_number using the earliest timestamp
        SELECT DISTINCT ON (race_number) 
            race_number, 
            timestamp
        FROM starters
        WHERE NOT deleted
        ORDER BY race_number, timestamp ASC
    ) AS s USING (race_number)
    CROSS JOIN LATERAL (
        SELECT f.timestamp - s.timestamp AS duration
    ) AS d
    WHERE NOT f.deleted;
    ```
3. Click on Gear icon next to Visualization button
4. Next to `duration`, click `...`  
    a. Under "Show the time", click on `HH:MM:SS`  
    b. Under "Time style", choose "24-hour clock"  
5. Next to `race_number`, click `...`  
    a. Under Separator style, choose "100000.00"  
    b. repeat for `finisher_id` and `bib_number`  
6. Save as "All Finishers"

Create a question named "All Finishers Descending"

1. Click on New button and select "Question"
2. Select "All Finishers" as Data
3. Sort descending on `finishers`
4. Save as "All Finishers Descending"

Create a dashboard named "Malstrek Dashboard"

1. Click on New button and select Dashboard
2. Add "All Finishers Descending"
3. Click on Filter icon and create a race number filter
    a. Label: "Race Number"  
    b. Filter or parameter type: "Number"  
    c. Dropdown list  
        i. From another model or question  
        ii. Model or question to supply the values: "Race Numbers"  
        iii. Column to supply the values: "Race Number"  
    d. Set a Default value  
    e. Enable "Always require a value"  
4. Click Done button
5. Save as "Malstrek Dashboard"
