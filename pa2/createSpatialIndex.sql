-- Step 1: create a new table including ItemId and Point (Geographic coordination)
CREATE TABLE IF NOT EXISTS GeoItems (
    ItemId INT NOT NULL PRIMARY KEY,
    Location POINT NOT NULL,
    SPATIAL INDEX(Location)  -- Create Spatial Index
) ENGINE=InnoDB;

-- Step 2: Convert the LatLong datas in `ItemLatLon` into `POINT`
INSERT INTO GeoItems (ItemId, Location)
SELECT ItemId, ST_GeomFromText(CONCAT('POINT(', Longitude, ' ', Latitude, ')'))
FROM ItemLatLon
WHERE Latitude IS NOT NULL AND Longitude IS NOT NULL;
