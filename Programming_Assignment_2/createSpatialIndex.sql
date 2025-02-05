USE ad;
CREATE TABLE IF NOT EXISTS spatialLocation(Item_Id INT PRIMARY KEY, longlat POINT NOT NULL) ENGINE = MyISAM;
DELETE FROM spatialLocation;
INSERT INTO spatialLocation SELECT Item_Id, POINT(longitude, latitude) FROM ItemLatLon;