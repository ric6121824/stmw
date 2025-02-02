USE ad;
CREATE TABLE IF NOT EXISTS spatialLocation(item_id INT PRIMARY KEY, longlat POINT NOT NULL) ENGINE = MyISAM;
DELETE FROM spatialLocation;
INSERT INTO spatialLocation SELECT item_id, POINT(longitude, latitude) FROM item_coordinates;