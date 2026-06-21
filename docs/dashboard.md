# Dashboard

## First time

Follow instructions to start Metabase

http://localhost:3000

## Minimal set up

### Race Numbers

This will be used as a dropdown filter on the dashboard

1. Click New button and select "Question"
1. Select "Races" as Data
1. Group by "Race Number"
1. Save as "Race Numbers"

### Race Finishers View

1. Click New button and select "Question"
1. Select "V Race Finishers"
1. Click on Gear icon next to Visualization button
1. (optional) Next to "Finisher Time", click `...`  
    * Under "Show the time", click on `HH:MM:SS`  
    * Under "Time style", choose "24-hour clock"  
1. Next to "Race Number", click `...`  
    * Under Separator style, choose "100000.00"  
    * repeat for "Finisher ID" and "Bib Number"
1. Save as "Race Finishers View"

### All Finishers Descending

1. Click on New button and select "Question"
1. Select "Race Finishers View" as Data
1. Sort descending on "Finishers"
1. Save as "All Finishers Descending"

### Histogram

1. Click on New button and select "Question"
1. Select "Race Finishers View" as Data
1. Summarize "Count" by "Finisher Time:Minute"
1. Save as "Histogram"

### Duplicated bib numbers

1. Click on New button and select "Question"
1. Select "Race Finishers View" as Data
1. Summarize "Count" by "Bib Number"
1. Click on the Filter below it and select "Count is greater than 1"
1. Save as "Duplicated bib numbers"

### Malstrek Dashboard

1. Click on New button and select Dashboard
1. Add "All Finishers Descending"
1. Click on Filter icon and create a race number filter
    * Label: "Race Number"  
    * Filter or parameter type: "Number"  
    * Dropdown list  
        * From another model or question  
        * Model or question to supply the values: "Race Numbers"  
        * Column to supply the values: "Race Number"  
    * Set a Default value  
    * Enable "Always require a value"  
1. Click Done button
1. Add "Histogram" and "Duplicated bib numbers"
  * Click on "Edit visualization" icon on the chart
  * Click Settings
  * Flip "Hide this card if there are no results"
  * Click Save button
  * Set the Field Filter by "Race Number"
1. Save as "Malstrek Dashboard"

### Refresh 10 s

Append `#refresh=10` to the browser URL

### Full screen

1. Click `...` below the New button on the dashboard
1. Select "Enter full screen"